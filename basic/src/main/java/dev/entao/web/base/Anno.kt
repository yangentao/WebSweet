package dev.entao.web.base

import java.text.DecimalFormat
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation




@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class NumberFormat(val pattern: String)

//字段长度--字符串
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MaxLength(val value: Int = 255, val msg: String = "")

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class FixLength(val value: Int)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class MinLength(val value: Int)

//是否忽略
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Exclude


//表或字段(属性)的名字
//路由时, controller名字或action名字
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Name(val value: String)

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Label(val value: String, val desc: String = "")

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Comment(val value: String)

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class NullValue(val value: String)


@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HideClient


//字符串长度限制, 也可用于数组或JsonArray
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Trim

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LengthRange(val minValue: Int, val maxValue: Int, val msg: String = "")


//参数或属性的最小值Int
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class MinValue(val value: String, val msg: String = "")

//参数或属性的最大值Int
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class MaxValue(val value: String, val msg: String = "")

//参数或属性的最大值Int
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValueRange(val minVal: String, val maxVal: String, val msg: String = "")

//字符串非空, 也可以用于集合
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotEmpty

//trim后的字符串非空
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotBlank

//字符串长度限制, 也可用于数组或JsonArray
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Match(val value: String, val msg: String = "")

//java.util.Date
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DatePattern(val format: String)

fun DatePattern.display(v: Any): String {
    return dateDisplay(v, this.format)
}


val KProperty<*>.fullName: String
    get() {
        val tabName = this.ownerClass!!.userName
        val fname = this.findAnnotation<Name>()?.value ?: this.name
        return "$tabName.$fname"
    }
val KClass<*>.userName: String
    get() {
        return this.findAnnotation<Name>()?.value ?: this.simpleName!!
    }
val KFunction<*>.userName: String
    get() {
        return this.findAnnotation<Name>()?.value ?: this.name
    }
val KProperty<*>.userName: String
    get() {
        return this.findAnnotation<Name>()?.value ?: this.name
    }

val KParameter.userName: String
    get() {
        return this.findAnnotation<Name>()?.value ?: this.name ?: throw IllegalStateException("参数没有名字")
    }

//label
val KClass<*>.userLabel: String
    get() {
        return this.findAnnotation<Label>()?.value ?: this.userName
    }

val KClass<*>.userDesc: String
    get() {
        val lb = this.findAnnotation<Label>()
        if (lb != null) {
            if (lb.desc.isNotEmpty()) {
                return lb.desc
            }
            if (lb.value.isNotEmpty()) {
                return lb.value
            }
        }
        return this.userName
    }

val KFunction<*>.userLabel: String
    get() {
        return this.findAnnotation<Label>()?.value ?: this.userName
    }
val KFunction<*>.userDesc: String
    get() {
        val lb = this.findAnnotation<Label>()
        if (lb != null) {
            if (lb.desc.isNotEmpty()) {
                return lb.desc
            }
            if (lb.value.isNotEmpty()) {
                return lb.value
            }
        }
        return this.userName
    }

val KProperty<*>.userLabel: String
    get() {
        return this.findAnnotation<Label>()?.value ?: this.userName
    }
val KParameter.userLabel: String
    get() {
        return this.findAnnotation<Label>()?.value ?: this.userName
    }

//==default value


val KProperty<*>.isHideClient: Boolean
    get() {
        return this.hasAnnotation<HideClient>()
    }


val KAnnotatedElement.labelOnly: String?
    get() {
        return this.findAnnotation<Label>()?.value
    }
//inline fun <reified T : Annotation> KAnnotatedElement.hasAnnotation(): Boolean = null != this.findAnnotation<T>()


//12345.format(",###.##")
//12345.6789.format("0,000.00")
fun Number.format(pattern: String): String {
    return if (pattern.isEmpty()) {
        this.toString()
    } else {
        DecimalFormat(pattern).format(this)
    }
}


//fun main() {
//	val fm = DecimalFormat("#.###")
//	println(fm.maximumFractionDigits)
//}