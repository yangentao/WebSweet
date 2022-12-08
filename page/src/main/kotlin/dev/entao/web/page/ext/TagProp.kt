@file:Suppress("unused")

package dev.entao.web.page.ext

import dev.entao.web.base.*
import dev.entao.web.core.BaseApp
import dev.entao.web.core.HttpAction
import dev.entao.web.core.HttpConst
import dev.entao.web.core.HttpContext
import dev.entao.web.core.controllers.KeyLabelController
import dev.entao.web.core.uri
import dev.entao.web.page.dialog.dataConfirm
import dev.entao.web.page.dialog.dataModal
import dev.entao.web.page.ext.PageConst.QUERY_FORM
import dev.entao.web.sql.Decimal
import dev.entao.web.sql.display
import dev.entao.web.sql.keyProp
import dev.entao.web.sql.nameSQL
import dev.entao.web.sql.refModel
import dev.entao.web.sql.toMap
import dev.entao.web.tag.tag.*
import java.math.BigDecimal
import kotlin.math.pow
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation


fun Tag.linkButton(vararg classes: String, block: AnchorTag.() -> Unit): AnchorTag {
    return a(*classes) {
        "role" attr "button"
        this.block()
    }
}


fun Tag.divRow(vararg classes: String, block: DivTag.() -> Unit) {
    div("row", *classes, block = block)
}

fun Tag.divCol(block: DivTag.() -> Unit) {
    div("col", "col-sm-12", block = block)
}

fun Tag.divCol2(block: DivTag.() -> Unit) {
    div("col-md-2", "col-sm-12", block = block)
}

fun Tag.divCol3(block: DivTag.() -> Unit) {
    div("col-md-3", "col-sm-12", block = block)
}

fun Tag.divCol4(block: DivTag.() -> Unit) {
    div("col-md-4", "col-sm-12", block = block)
}

fun Tag.divCol6(block: DivTag.() -> Unit) {
    div("col-md-6", "col-sm-12", block = block)
}

fun Tag.divCol12(block: DivTag.() -> Unit) {
    div("col-md-12", "col-sm-12", block = block)
}

fun Tag.viewProp(p: Prop0) {
    label { +p.userLabel }
    appendText(": ")
    appendText(p.displayString(null))
}

fun Tag.viewProp(p: Prop, inst: Any) {
    label { +p.userLabel }
    appendText(": ")
    appendText(p.displayString(inst))
}

val HttpContext.backURL: String?
    get() {
        return this[BaseApp.BACK_URL]?.base64Decoded
    }


fun Tag.inputRef(prop: Prop, action: HttpAction = KeyLabelController::search, keyValue: String = "", valueValue: String = "") {
    val valID = Tag.makeID("input")
    val refAn = prop.refModel ?: error("NO RefModel found")
    label("form-label") {
        forID = valID
        +prop.userLabel
    }
    div("input-group") {
        val keyID = Tag.makeID("hidden")
        hidden(
            "yet-ref-key",

            ) {
            id = keyID
            name = prop.userName
            value = valueOf(prop) ?: keyValue
        }
        input("form-control", "yet-ref-label") {
            id = valID
            type = "text"
            placeholder = prop.userLabel
            if (!inQueryForm && !prop.returnType.isMarkedNullable) {
                required()
            }
//				readonly()
            "onfocus" attr "this.blur();"
            val v = valueOf(prop)
            if (v != null) {
                value = refAn.display(v)
            } else {
                value = valueValue
            }
        }
        button("btn", "btn-outline-secondary", "yet-ref-query") {
            PageData.url attr context.uriActionValues(action, refAn.foreignTable.nameSQL, refAn.keyProp.nameSQL, refAn.displayField)
            PageData.search attr action.paramNames.last()
            onclick = "client.refQuery(this);"
            +"查找"
        }
        if (inQueryForm || prop.returnType.isMarkedNullable) {
            button("btn", "btn-outline-danger", "yet-ref-clear") {
                +"清除"
                onclick = "client.refClear(this);"
            }
        }
    }

}

fun Tag.selectOptionsDependOn(select: Prop, dependSelect: Prop, byProp: Prop) {
    val action = KeyLabelController::eq
    val refField = select.refModel ?: error("$select 没有被RefModel标注")
    val selTag = this.first { it.name == select.userName } ?: return
    val dependTag = this.first { it.name == dependSelect.userName } ?: return
    selTag.setAttr(
        "depend_url",
        context.uriActionValues(action, refField.foreignTable.nameSQL, refField.keyProp.nameSQL, refField.displayField, byProp.nameSQL)
    )
    selTag.setAttr("onval", action.paramNames.last())
    script {
        """
				client.selectOptionsDependOn('${selTag.requireID()}', '${dependTag.requireID()}')
			""".trimIndent()
    }
}

fun Tag.tablePaginationX(action: HttpAction, totalRow: Int) {
    this.tablePagination(action, totalRow, action.currentPage, action.pageSize)
}

fun Tag.tablePagination(action: HttpAction, totalRow: Int, currentPage: Int, pageSize: Int) {
    val maxDisplaySize = 10
    if (totalRow < 1) {
        return
    }
    val totalPage = (totalRow + pageSize - 1) / pageSize
    nav {
        ul("pagination", "pagination-sm") {
            li("page-item") {
                if (currentPage == 0) {
                    classAdd("active")
                    span("page-link") {
                        +"首页"
                    }
                } else {
                    a("page-link") {
                        +"首页"
                        "href" attr pageUriBuilder(action, 0)
                    }
                }
            }
            li("page-item") {
                if (currentPage == 0) {
                    classAdd("disabled")
                    a("page-link") {
                        href = "#"
                        unsafe("&laquo;")
                    }
                } else {
                    a("page-link") {
                        unsafe("&laquo;")
                        "href" attr pageUriBuilder(action, currentPage - 1)
                    }
                }
            }
            val ls = ArrayList<Int>()
            if (totalPage > maxDisplaySize) {
                if (currentPage < totalPage / 2) {
                    val from = Integer.max(0, currentPage - maxDisplaySize / 2)
                    for (i in from until (from + maxDisplaySize)) {
                        ls += i
                    }
                } else {
                    val topage = Integer.min(totalPage - 1, currentPage + maxDisplaySize / 2)
                    for (i in (topage + 1 - maxDisplaySize)..topage) {
                        ls += i
                    }
                }
            } else {
                for (i in 0 until totalPage) {
                    ls += i
                }
            }
            for (i in ls) {
                li("page-item") {
                    if (currentPage == i) {
                        classAdd("active")
                        span("page-link") {
                            +(i + 1).toString()
                        }
                    } else {
                        a("page-link") {
                            +(i + 1).toString()
                            "href" attr pageUriBuilder(action, i)
                        }
                    }
                }
            }

            li("page-item") {
                if (currentPage == totalPage - 1) {
                    classAdd("disabled")
                    a("page-link") {
                        href = "#"
                        unsafe("&raquo;")
                    }
                } else {
                    a("page-link") {
                        unsafe("&raquo;")
                        "href" attr pageUriBuilder(action, currentPage + 1)
                    }
                }
            }
            li("page-item") {
                if (currentPage == totalPage - 1) {
                    classAdd("active")
                    span("page-link") {
                        +"末页"
                    }
                } else {
                    a("page-link") {
                        +"末页"
                        "href" attr pageUriBuilder(action, totalPage - 1)
                    }
                }
            }
        }
    }
}

private fun Tag.pageUriBuilder(action: HttpAction, n: Int): String {
    val pname = action.pageParam ?: error("User @PageParam to indicate pager index")
    val ls = ArrayList<Pair<String, String>>()
    for ((k, v) in context.paramMap) {
        if (k != pname) {
            for (s in v) {
                ls += k to s
            }
        }
    }
    ls += pname to n.toString()
    return context.uriActionKeyValues(action, ls)
}



fun Tag.labelSelect(prop: Prop) {
    val isQueryForm = this.inQueryForm
    label("form-label") {
        +prop.userLabel
    }
    select("form-select") {
        forLabelPre()
        name = prop.userName
        if (!isQueryForm && !prop.returnType.isMarkedNullable) {
            required()
        }
        if (isQueryForm) {
            option {
                this.value = ""
                +"全部"
            }
        }
        val map: Map<String, String> = prop.findAnnotation<OptionList>()?.toMap() ?: prop.refModel?.toMap() ?: emptyMap()
        val propValue = valueOf(prop)
        for ((k, v) in map) {
            option(v, k) {
                if (propValue == k) {
                    "selected" attr "true"
                }
            }
        }
    }
    if (!isQueryForm) {
        div("invalid-feedback") {
            +"请输入${prop.userLabel}"
        }
    }
}

fun Tag.option(label: String, value: String, block: OptionTag.() -> Unit): OptionTag {
    return option {
        this.value = value
        +label
        this.block()
    }
}

fun Tag.radioDivHorizontal(prop: Prop) {
    val map: Map<String, String> =
        prop.findAnnotation<OptionList>()?.toMap() ?: prop.refModel?.toMap() ?: return
    val pname = prop.userName
    val pv = valueOf(prop) ?: ""

    for ((k, v) in map) {
        div("form-check", "form-check-inline") {
            radio("form-check-input") {
                name = pname
                value = k
                if (pv == k) {
                    "checked" attr "true"
                }
            }
            label("form-check-label") {
                forInputPre()
                +v
            }
        }
    }
}

fun Tag.radioDivVertical(prop: Prop) {
    val map: Map<String, String> =
        prop.findAnnotation<OptionList>()?.toMap() ?: prop.refModel?.toMap() ?: return
    val pname = prop.userName
    for ((k, v) in map) {
        div("form-check") {
            radio("form-check-input") {
                this.name = pname
                value = k
            }
            label("form-check-label") {
                forInputPre()
                +v
            }
        }
    }
}

fun Tag.navItem(action: HttpAction) {
    navItem(action.userLabel, action.uri, action.findAnnotation<NavItem>()?.icon)
}

fun Tag.navItem(label: String, href: String, icon: String?) {
    this.navItem {
        this.href = href
        if (icon != null) {
            img("yet-side-icon") {
                src = icon.uri
            }
        }
        +label
        val hrefUri = href.substringBefore('?')
        if (context.currentUri == hrefUri) {
            this.classAdd("active")
        }
    }
}

fun Tag.queryForm(action: HttpAction, block: FormTag.() -> Unit) {
    queryForm(action.uri) {
        this.block()
        action.sortParams?.also { sp ->
            hidden("sortCol", context[sp.col])
            hidden("sortDir", context[sp.dir])
        }
    }
}

fun Tag.queryForm(action: String, block: FormTag.() -> Unit) {
    this.form("row", "g-3") {
        this.method = "POST"
        this.action = action
        PageConst.QUERY_FORM attr "true"
        this.block()

        div("row mt-2") {
            div("col-auto") {
                submit("btn", "btn-primary") {
                    +"查询"
                }
            }
        }
    }
}

fun Tag.queryCol4(block: DivTag.() -> Unit) {
    this.div("col-md-4", "col-sm-12", block = block)
}

fun Tag.formBuild(action: HttpAction, block: FormTag.() -> Unit) {
    this.form("row", "g-3") {
        fromAction(action)
        needsValidation()
        "novalidate" attr "novalidate"
        this.block()
        hidden {
            name = BaseApp.BACK_URL
            this.valueFromContext()
        }
        div("col-auto") {
            submit("btn", "btn-primary") {
                +"提交"
            }
        }
    }
}

fun AnchorTag.fromAction(action: HttpAction, vararg valueArgs: Any) {
    +action.userLabel
    if (action.hasAnnotation<dev.entao.web.core.Action>()) {
        href = context.uriActionValues(action, *valueArgs)
    }
    action.findAnnotation<ConfirmMessage>()?.also {
        this.classAdd("text-danger")
        dataConfirm = it.value
    }
    action.findAnnotation<DialogPop>()?.also {
        dataModal = it.value
        return
    }
    action.findAnnotation<ClientAction>()?.also { ajs ->
        val s = ajs.value
        dataScript = s.ifEmpty {
            "client.${action.name}(this)"
        }
        href = "#"
        return
    }

}

fun Tag.switchDiv(prop: Prop) {
    val pname = prop.userName
    div("form-check", "form-switch") {
        checkbox("form-check-input") {
            name = pname
            value = "1"
            if (valueOf(prop) in Tag.trueSet) this.setAttr("checked", "true")
        }
        label("form-check-label") {
            forInputPre()
            +prop.userLabel
        }
        hidden(pname, "0")
    }
}

fun Tag.checkboxDiv(prop: Prop) {
    val pname = prop.userName
    div("form-check") {
        checkbox("form-check-input") {
            name = pname
            value = "1"
            if (valueOf(prop) in Tag.trueSet) this.setAttr("checked", "true")
        }
        label("form-check-label") {
            forInputPre()
            +prop.userLabel
        }
        hidden(pname, "0")
    }
}

fun Tag.queryDiv(block: DivTag.() -> Unit) {
    this.div("col-xl-3", "col-lg-4", "col-md-6", "col-sm-12", block = block)
}

fun Tag.navItem(block: AnchorTag.() -> Unit) {
    this.li("nav-item") {
        a("nav-link") {
            href = "#"
            this.block()
        }
    }
}

fun Tag.labelFile(name: String) {
    label("form-label") {
    }
    file("form-control") {
        forLabelPre()
        this.name = name
    }
    parent { it.tagName == "form" }?.setAttr("enctype", "multipart/form-data")
}

fun Tag.labelEdit(prop: Prop, block: TagBlock = {}) {
    val isQueryForm = this.inQueryForm
    label("form-label") {
        fromProp(prop)
    }
    val ft = prop.findAnnotation<FormText>()
    input("form-control") {
        this.fromProp(prop)
        this.block()
    }
    if (!isQueryForm) {
        div("invalid-feedback") {
            +"请输入${prop.userLabel} ${ft?.value ?: ""}"
        }
    }
}

fun Tag.labelEdit(label: String, name: String, block: TagBlock) {
    label("form-label") {
        +label
    }
    input("form-control") {
        forLabelPre()
        this.name = name
        this.type = "text"
        this.block()
    }
}

fun Tag.labelTextArea(prop: Prop, block: TagBlock = {}) {
    label("form-label") {
        fromProp(prop)
    }
    textarea("form-control") {
        fromProp(prop)
        this.block()
    }
    val ft = prop.findAnnotation<FormText>()
    div("invalid-feedback") {
        +"请输入${prop.userLabel} ${ft?.value ?: ""}"
    }
}

fun Tag.labelTextArea(label: String, name: String, block: TagBlock) {
    label("form-label") {
        +label
    }
    textarea("form-control") {
        forLabelPre()
        this.name = name
        rows = 3
        this.block()
    }
}

fun TextareaTag.fromProp(p: Prop) {
    id = p.userName
    name = p.userName
    rows = 3
    if (!p.returnType.isMarkedNullable) {
        required()
    }
    this.text(valueOf(p) ?: "")

    val ft = p.findAnnotation<FormText>()
    if (ft != null) {
        placeholder = ft.value
    }
    p.findAnnotation<DatePattern>()?.also {
        placeholder = ((ft?.value ?: "") + " 格式如:" + DateX().format(it.format))
    }
}

val Tag.inQueryForm: Boolean
    get() {
        if (this.tagName == "form") {
            return this.hasAttr(QUERY_FORM)
        }
        return parent(TAGNAME to "form")?.hasAttr(QUERY_FORM) ?: false
    }

fun Tag.label(p: Prop): Tag {
    return this.label {
        forID = p.userName
        +p.userLabel
    }
}

fun Tag.hidden(p: Prop0) {
    val v = p.getPropValue()?.toString() ?: ""
    this.hidden {
        name = p.userName
        value = v
    }
}

fun Tag.hidden(p: Prop1, v: Any?) {
    val vv = v?.toString() ?: ""
    this.hidden {
        name = p.userName
        value = vv
    }
}

fun LabelTag.fromProp(p: Prop) {
    p.labelOnly?.also {
        this.text(it)
    }
    forID = p.userName
}

fun LabelTag.fromParameter(p: KParameter) {
    p.labelOnly?.also {
        this.text(it)
    }
    forID = p.userName
}

fun InputTag.fromProp(p: Prop) {
    this.name = p.userName
    this.id = this.name
    val isQueryForm = this.inQueryForm
    if (!isQueryForm && !p.returnType.isMarkedNullable) {
        this.required()
    }
    valueFromProp(p)
    fromAnno(p.returnType.classifier, p)
}

fun InputTag.fromParameter(p: KParameter) {
    this.name = p.userName
    this.id = this.name
    val isQueryForm = this.inQueryForm
    if (!isQueryForm && !p.isOptional) {
        this.required()
    }
    value = context[p.userName] ?: ""
    fromAnno(p.type.classifier, p)
}

private fun InputTag.fromAnno(cls: KClassifier?, p: KAnnotatedElement) {
    when (cls) {
        Long::class, Int::class, Short::class, Byte::class -> type = "number"
        Double::class, Float::class, BigDecimal::class -> {
            val dm = p.findAnnotation<Decimal>()
            if (dm != null) {
                type = "number"
                step = (1.0 / 10.0.pow(dm.d)).toString()
            } else {
                pattern = """[0-9]+([\.][0-9]+)?"""
            }
        }

        else -> {}
    }
    p.findAnnotation<MaxLength>()?.also {
        "maxlength" attr it.value.toString()
    }
    p.findAnnotation<ValueRange>()?.also {
        "min" attr it.minVal
        "max" attr it.maxVal
    }
    p.findAnnotation<MinValue>()?.also {
        "min" attr it.value
    }
    p.findAnnotation<MinValue>()?.also {
        "max" attr it.value
    }

    val formText = p.findAnnotation<FormText>()
    placeholder = formText?.value ?: (p.labelOnly ?: "")
    p.findAnnotation<DatePattern>()?.also {
        "placeholder" attr ((formText?.value ?: "") + " 格式如:" + DateX().format(it.format))
    }
    if (value.isEmpty()) {
        p.findAnnotation<NullValue>()?.also {
            value = it.value
        }
    }
    p.findAnnotation<Password>()?.also {
        type = "password"
        value = ""
    }
}

fun FormTag.fromAction(ac: HttpAction) {
    this.action = context.uriAction(ac)
    val ls: List<String> = ac.findAnnotation<dev.entao.web.core.HttpMethod>()?.value?.toList() ?: ac.ownerClass?.findAnnotation<dev.entao.web.core.HttpMethod>()?.value?.toList() ?: emptyList()
    if (HttpConst.M_POST in ls) {
        this.method = HttpConst.M_POST
    } else if (HttpConst.M_GET in ls) {
        this.method = HttpConst.M_GET
    } else if (HttpConst.M_PUT in ls) {
        this.method = HttpConst.M_PUT
    } else if (HttpConst.M_DELETE in ls) {
        this.method = HttpConst.M_DELETE
    } else {
        this.method = HttpConst.M_POST
    }
}

private fun Tag.preLabel(time: Int = 0): Tag? {
    if (time > 1) return null
    val ls = parent?.children ?: return null
    val n = ls.indexOf(this)
    if (n - 1 >= 0) {
        val ch = ls[n - 1]
        if (ch.tagName == "label") {
            return ch
        }
    }
    return parent?.preLabel(time + 1)
}

fun Tag.forLabelPre() {
    preLabel()?.setAttr("for", this.idx)
}

fun Tag.valueOf(p: Prop): String? {
    return if (p is Prop0) {
        p.getPropValue()?.toString()
    } else {
        context[p.userName]
    }
}

fun InputTag.valueFromProp(p: Prop) {
    this.value = valueOf(p) ?: ""
}