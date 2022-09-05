@file:Suppress("FunctionName", "MemberVisibilityCanBePrivate", "unused", "PropertyName")

package dev.entao.web.tag.tag

import dev.entao.web.base.escapeHtml
import dev.entao.web.core.HttpContext
import dev.entao.web.core.OnHttpContext
import dev.entao.web.tag.attrVal
import dev.entao.web.tag.ident

const val TAGNAME = "tagname"
typealias TagBlock = Tag.() -> Unit
typealias TagAttr = Pair<String, String>

//TODO 将tag的toString单独拿出来.
open class Tag(override val context: HttpContext, val tagName: String) : OnHttpContext {
    private val attrMap: TagPropMap = TagPropMap()
    val children = ArrayList<Tag>(8)
    val classList: ArrayList<String> = ArrayList()
    var parent: Tag? = null
    var id: String by TagProp
    var name: String by TagProp
    var style: String by TagProp
    var onclick: String by TagProp
    val root: Tag get() = this.parent?.root ?: this

    fun classAdd(vararg clses: String) {
        classList.addAll(clses)
    }

    fun classHas(cls: String): Boolean {
        return cls in classList
    }

    fun classRemove(cls: String) {
        classList.remove(cls)
    }

    fun classAddFirst(cls: String) {
        classList.remove(cls)
        classList.add(0, cls)
    }

    fun classBringFirst(cls: String) {
        classList.remove(cls)
        classList.add(0, cls)
    }

    fun parent(block: (Tag) -> Boolean): Tag? {
        val p = this.parent ?: return null
        if (block(p)) {
            return p
        }
        return p.parent(block)
    }

    fun parent(attr: TagAttr, vararg vs: TagAttr): Tag? {
        val p = this.parent ?: return null
        if (p.match(attr, *vs)) {
            return p
        }
        return p.parent(attr, *vs)
    }

    fun removeFromParent() {
        this.parent?.children?.remove(this)
    }

    fun removeChild(tag: Tag) {
        children.remove(tag)
    }

    fun cleanChildren() {
        children.clear()
    }

    private fun filterTo(ls: ArrayList<Tag>, attr: TagAttr, vararg vs: TagAttr): List<Tag> {
        for (c in this.children) {
            if (c.match(attr, *vs)) {
                ls += c
            }
            c.filterTo(ls, attr, *vs)
        }
        return ls
    }

    fun filter(attr: TagAttr, vararg vs: TagAttr): List<Tag> {
        val ls = ArrayList<Tag>()
        return filterTo(ls, attr, *vs)
    }

    fun first(attr: TagAttr, vararg vs: TagAttr): Tag? {
        for (c in this.children) {
            if (c.match(attr, *vs)) {
                return c
            }
            val t = c.first(attr, *vs)
            if (t != null) {
                return t
            }
        }
        return null
    }

    fun first(acceptor: (Tag) -> Boolean): Tag? {
        val t = children.find(acceptor)
        if (t != null) {
            return t
        }
        children.forEach {
            val tt = it.first(acceptor)
            if (tt != null) {
                return tt
            }
        }
        return null
    }

    fun tag(name: String, block: Tag.() -> Unit): Tag {
        val t = createTagInstance(context, name)
        return append(t).apply(block)
    }

    fun single(tagname: String): Tag {
        for (c in this.children) {
            if (c.tagName == tagname) {
                return c
            }
        }
        return this.tag(tagname) {}
    }

    fun match(vararg vs: TagAttr): Boolean {
        for (a in vs) {
            val c = when (a.first) {
                TAGNAME -> this.tagName == a.second
                "tag" -> this.tagName == a.second
                "class" -> this.classHas(a.second)
                else -> this.getAttr(a.first) == a.second
            }
            if (!c) {
                return false
            }
        }
        return true
    }

    val idx: String
        get() {
            return requireID()
        }

    fun requireID(): String {
        if (this.id.isEmpty()) {
            this.id = makeID(tagName)
        }
        return this.id
    }

    fun required() {
        setAttr("required", "required")
    }

    fun readonly() {
        "readonly" attr "true"
    }

    fun disabled() {
        "disabled" attr "true"
    }

    fun bringToFirst() {
        val ls = parent?.children ?: return
        ls.remove(this)
        ls.add(0, this)
    }

    fun allAttrs(): Map<String, String> {
        return this.attrMap
    }

    fun attrRemove(attr: String) {
        attrMap.remove(attr)
    }


    fun <T : Tag> append(tag: T): T {
        children += tag
        tag.parent = this
        return tag
    }

    fun <T : Tag> append(tag: T, block: T.() -> Unit): T {
        return append(tag).apply(block)
    }

    //==textEscaped
    // +"text"
    operator fun String?.unaryPlus() {
        text(this)
    }

    fun data(name: String): String {
        return if (name.startsWith("data-")) {
            this.getAttr(name)
        } else {
            this.getAttr("data-$name")
        }
    }

    fun data(name: String, value: String) {
        if (name.startsWith("data-")) {
            this.setAttr(name, value)
        } else {
            this.setAttr("data-$name", value)
        }
    }

    fun removeAttr(key: String) {
        attrMap.remove(key)
    }

    fun setAttr(key: String, value: String) {
        this.attrMap[key] = value
    }

    fun getAttr(key: String): String {
        return this.attrMap[key] ?: ""
    }

    fun hasAttr(key: String): Boolean {
        return this.attrMap.containsKey(key)
    }

    infix fun String.attr(value: String) {
        attrMap[this] = value
    }


    fun valueFromContext() {
        val k = this.name.ifEmpty { return }
        val v = context[k] ?: return
        "value" attr v
    }

    open fun toHtml(): String {
        val buf = StringBuilder(2048)
        toHtml(buf, 0)
        return buf.toString()
    }

    open fun toHtml(buf: Appendable, level: Int = 0) {
        val multiLine: Boolean = htmlMultiLine()
        val parentMultiLine = parent?.htmlMultiLine() == true

        if (this.classList.isNotEmpty()) {
            this.attrMap["class"] = this.classList.joinToString(" ")
        }
        if (parentMultiLine) {
            buf.newLine(level)
        }
        buf.append("<").append(this.tagName)
        if (this.attrMap.isNotEmpty()) {
            buf.append(' ')
            val s: String = this.attrMap.map { e ->
                htmlAttrPair(e.key, e.value)
            }.filter { it.isNotEmpty() }.joinToString(" ")
            buf.append(s)
        }
        if (this.children.isEmpty()) {
            if (this.tagName in mustBlockTags) {
                buf.append("></").append(this.tagName).append(">")
            } else {
                buf.append("/>")
            }
            return
        }
        buf.append(">")
        children.forEach { tag ->
            tag.toHtml(buf, level + 1)
        }
        if (multiLine) {
            buf.newLine(level)
        }
        buf.append("</").append(tagName).append(">")
    }

    protected fun Appendable.newLine(ident: Int): Appendable {
        this.appendLine().ident(ident)
        return this
    }

    override fun toString(): String {
        return toHtml()
    }

    protected fun htmlAttrPair(key: String, value: String): String {
        if (value.isEmpty()) {
            if (!htmlAllowEmptyAttrValue(key)) return ""
        }
        if (key in singleAttr) {
            return if (key == value || value == "true" || value == "yes" || value == "on" || value == "1") {
                key
            } else {
                ""
            }
        }
        return key + "=" + attrVal(value)
    }

    protected fun htmlAllowEmptyAttrValue(key: String): Boolean {
        if (this.tagName == "col" && key == "width") return true
        if (this.tagName == "option" && key == "value") return true
        return false
    }

    open fun htmlMultiLine(): Boolean {
        return when (children.size) {
            0 -> false
            1 -> children.first().htmlMultiLine()
            else -> true
        }
    }

    companion object {
        protected val singleAttr = setOf("required", "novalidate", "checked", "disabled", "multiple", "readonly", "selected")
        protected val mustBlockTags = setOf("script", "div", "p", "ul", "ol", "span", "datalist", "option", "button", "textarea", "label", "select", "a")
        protected val keepEmptyAttr: Set<Pair<String, String>> = setOf("col" to "width", "option" to "value")
        private var eleId: Int = 0

        @Synchronized
        fun makeID(prefix: String = "e"): String {
            eleId += 1
            if (eleId > 1_000_000) eleId = 1
            return "$prefix$eleId"
        }

        const val TEXT_TAG = "text"
        val trueSet: Set<String> = setOf("on", "1", "true", "yes")

    }
}


class TextTag(context: HttpContext, var text: String = "") : Tag(context, TEXT_TAG) {
    var unsafe: Boolean = false
    var formatOutput = true
    var forView = true

    override fun toHtml(buf: Appendable, level: Int) {
        val multiLine: Boolean = htmlMultiLine()
        val parentMultiLine = parent?.htmlMultiLine() == true

        val s = if (unsafe) this.text else this.text.escapeHtml(forView)
        if (!formatOutput || parent?.tagName in listOf("pre", "code", "textarea")) {
            buf.append(s)
            return
        }
        val lines = s.lines()
        for (i in lines.indices) {
            if (multiLine || parentMultiLine) {
                buf.newLine(level)
            }
            buf.append(lines[i])
        }
    }

    override fun htmlMultiLine(): Boolean {
        return '\n' in text || '\r' in text
    }

}

fun Tag.scriptsToBottom() {
    val ls = this.filter(TAGNAME to "script")
    for (a in ls) {
        a.removeFromParent()
        this.append(a)
    }
}