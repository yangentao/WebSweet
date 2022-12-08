package dev.entao.web.page.ext

import dev.entao.web.base.Label
import dev.entao.web.core.HttpAction
import dev.entao.web.core.OnHttpContext
import dev.entao.web.core.actionName
import dev.entao.web.sql.SQLBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2017/6/20.
 */

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class UploadImage(val listWidth: Int = 64, val gridWidth: Int = 320, val largeWidth: Int = 0)

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Password

//@RefModel(Person::class, "id", "name")
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RefUpload(val limitSizeM: Int = 0)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DialogPop(val value: String = "static")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ClientAction(val value: String = "")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SortParams(val col: String = "sortColumn", val dir: String = "sortDirect")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PageParams(val name: String = "p", val size: Int = 50)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NavGroup(val label: String = "", val desc: String = "", val icon: String = "")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NavItem(val order: Int = 1, val label: String = "", val desc: String = "", val icon: String = "")

//	@OptionList("0:男", "1:女")
//	@OptionList("男", "女")
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class OptionList(vararg val options: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfirmMessage(val value: String)

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class FormText(val value: String)

//step属性
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class StepValue(val value: String)

//@ColumnWidth("50px");,
//@ColumnWidth("30%");,
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ColumnWidth(val value: String)

fun OptionList.toMap(): Map<String, String> {
    val map = LinkedHashMap<String, String>()
    this.options.map {
        if (":" in it) {
            val p = it.split(':')
            map[p[0].trim()] = p[1].trim()
        } else {
            map[it] = it
        }
    }
    return map
}

fun OptionList.display(v: Any): String {
    return this.toMap()[v.toString()] ?: ""
}

val KClass<*>.navLabel: String
    get() {
        findAnnotation<NavGroup>()?.also {
            if (it.label.isNotEmpty()) return it.label
        }
        findAnnotation<NavItem>()?.also {
            if (it.label.isNotEmpty()) return it.label
        }
        findAnnotation<Label>()?.also {
            if (it.value.isNotEmpty()) return it.value
        }
        return this.simpleName!!
    }

val KFunction<*>.navLabel: String
    get() {
        findAnnotation<NavGroup>()?.also {
            if (it.label.isNotEmpty()) return it.label
        }
        findAnnotation<NavItem>()?.also {
            if (it.label.isNotEmpty()) return it.label
        }
        findAnnotation<Label>()?.also {
            if (it.value.isNotEmpty()) return it.value
        }
        return this.actionName
    }

val KFunction<*>.pageSize: Int get() = this.findAnnotation<PageParams>()?.size ?: 50

val KFunction<*>.pageParam: String?
    get() {
        this.findAnnotation<PageParams>()?.also {
            if (it.name.isNotEmpty()) {
                return it.name
            }
        }
        return null
    }
context (OnHttpContext)
val KFunction<*>.currentPage: Int
    get() {
        val params = this.pageParam ?: return 0
        if (params.isEmpty()) return 0
        return context[params]?.toIntOrNull() ?: 0
    }

context (OnHttpContext)
fun SQLBuilder.limitPage(action: HttpAction) {
    this.limit(action.pageSize, action.currentPage * action.pageSize)
}

context (OnHttpContext)
fun SQLBuilder.orderPage(action: HttpAction) {
    val sp = action.sortParams ?: return
    val col = context[sp.col] ?: return
    val v = context[sp.dir] ?: return
    this.orderBy(col.ORDER(v.isEmpty() || v == "0"))
}

val KFunction<*>.sortParams: SortParams? get() = this.findAnnotation()
