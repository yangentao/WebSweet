@file:Suppress("unused", "FunctionName")

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import org.w3c.xhr.FormData
import kotlin.js.Promise
import kotlin.reflect.KProperty

//@JsExport
//@JsName("testTask")
//fun testTask() {
//    task {
//        logd("Task block")
//    }.then {
//        logd("Then")
//    }.catch {
//        logd("catch")
//    }.finally {
//        logd("finally")
//    }
//}


external fun invokeElementAttr(elem: HTMLElement, attr: String)

fun <T : Any> task(taskBlock: () -> T): Promise<T> {
    return Promise<T> { okBlock, _ ->
        okBlock(taskBlock())
    }
}


inline fun <reified T : Element> Element.nearest(): T? {
    return this.nearest { it is T } as? T
}

fun Element.nearest(block: (Element) -> Boolean): Element? {
    val p = this.parentElement ?: return null
    val list = p.childElements
    val idx = list.indexOf(this)
    if (idx < 0) return null
    for (i in 1 until list.size) {
        val a = idx - i
        if (a >= 0) {
            val ele = list[a]
            ele.matchElement(false, block)?.also { return it }
        }
        val b = idx + i
        if (b < list.size) {
            val ele = list[b]
            ele.matchElement(true, block)?.also { return it }
        }
    }
    return p.nearest(block)
}

private fun Element.matchElement(inc: Boolean, block: (Element) -> Boolean): Element? {
    if (block(this)) return this
    val ls = this.childElements
    val list = if (inc) ls else ls.reversed()
    for (e in list) {
        e.matchElement(inc, block)?.also { return it }
    }
    return null
}

val body: HTMLBodyElement get() = document.body as HTMLBodyElement

@Suppress("UNCHECKED_CAST")
fun <T : HTMLElement> createTag(tagName: String): T {
    return document.createElement(tagName) as T
}

fun findElementById(elementId: String): HTMLElement? {
    return document.getElementById(elementId) as? HTMLElement?
}

fun ParentNode.query(selector: String): HTMLElement? {
    return this.querySelector(selector) as? HTMLElement
}

fun ParentNode.queryAll(selector: String): List<HTMLElement> {
    return this.querySelectorAll(selector).asList().mapNotNull { it as? HTMLElement }
}

@Suppress("UNCHECKED_CAST")
fun <T : Element> findById(elementId: String): T? {
    return document.getElementById(elementId) as? T
}

@Suppress("UNCHECKED_CAST")
fun <T : HTMLElement> ParentNode.find(selector: String): T? {
    return this.querySelector(selector) as? T
}

@Suppress("UNCHECKED_CAST")
fun <T : HTMLElement> ParentNode.one(selector: String): T {
    return this.querySelector(selector) as T
}

@Suppress("UNCHECKED_CAST")
fun <T : HTMLElement> ParentNode.all(selector: String): List<T> {
    return this.querySelectorAll(selector).asList().mapNotNull { it as? T }
}

val Element.childElements: List<Element> get() = this.childNodes.asList().mapNotNull { it as? Element }

// div input:checkbox  => div input[type='checkbox']
private val String.convertTypeValue: String
    get() {
        val n = this.indexOf(':')
        if (n < 0) {
            return this
        }
        var typeModel = false
        val sb = StringBuilder()
        for (ch in this) {
            if (ch == ':') {
                typeModel = true
                sb.append("[type='")
                continue
            }
            if (!typeModel) {
                sb.append(ch)
            } else {
                //type 的值只有字母
                if (ch in '0'..'9' || ch in 'a'..'z' || ch in 'A'..'Z') {
                    sb.append(ch)
                } else {
                    sb.append("']")
                    sb.append(ch)
                    typeModel = false
                }
            }
        }
        if (typeModel) {
            sb.append("']")
        }
        return sb.toString()
    }

typealias EventBlock = (Event) -> Unit

fun EventTarget.on(evt: String, block: EventBlock) {
    this.addEventListener(evt, block, false)
}

fun <T : EventTarget> T.onSelf(evt: String, block: (T) -> Unit) {
    this.addEventListener(evt, {
        block(this)
    }, false)
}

fun Element.attr(attr: String): String? {
    return this.getAttribute(attr)
}

fun Element.attr(attr: String, value: String) {
    this.setAttribute(attr, value)
}

fun Element.data(attr: String): String? {
    return if (attr.startsWith("data-")) {
        this.getAttribute(attr)
    } else {
        this.getAttribute("data-$attr")
    }
}

fun Element.data(attr: String, value: String) {
    if (attr.startsWith("data-")) {
        this.setAttribute(attr, value)
    } else {
        this.setAttribute("data-$attr", value)
    }
}

fun PostForm(form: HTMLFormElement): HttpPost {
    return HttpPost(form.action).data(FormData(form))
}

typealias TagBlock = HTMLElement.() -> Unit

object IDGen {
    private var current: Int = 0
    fun next(): Int {
        current += 1
        return current
    }

    fun makeID(prefix: String = "e"): String {
        return "$prefix${next()}"
    }
}

fun logd(vararg vs: Any?) {
    console.log(*vs)
}

fun loge(vararg vs: Any?) {
    console.error(*vs)
}

fun HTMLElement.listSet(name: String, list: List<String>, sepChar: String = ",") {
    this.dataset[name] = list.joinToString(sepChar)
}

fun HTMLElement.listGet(name: String, sepChar: Char = ','): List<String> {
    val value = this.dataset[name] ?: return emptyList()
    return value.split(sepChar).filter { it.isNotEmpty() }
}

fun HTMLElement.listAdd(name: String, value: String, sepChar: Char = ','): List<String> {
    val old = this.dataset[name]
    if (old == null) {
        this.dataset[name] = value
    } else {
        this.dataset[name] = "$old,$value"
    }
    return this.listGet(name, sepChar)
}

fun Div(vararg classes: String): HTMLDivElement {
    return createTag<HTMLDivElement>("div").apply { addClass(*classes) }
}

fun Span(): HTMLSpanElement {
    return createTag("span")
}

operator fun HTMLElement.plusAssign(text: String) {
    this.text(text)
}

var Element.htmlValue: String
    get() = this.innerHTML
    set(value) {
        this.innerHTML = value
        val ls: List<HTMLScriptElement> = this.all("script")
        for (sc in ls) {
            sc.remove()
            if (sc.src.isNotEmpty()) {
                body.script {
                    src = sc.src
                }
            } else {
                val innerS = sc.innerText.trim()
                if (innerS.isNotEmpty()) eval(innerS)
            }
        }
    }

object StorageText {
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        if (value == null) {
            window.localStorage.removeItem(property.name)
        } else {
            window.localStorage[property.name] = value
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        return window.localStorage[property.name]
    }
}

object Storage {
    inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T {
        val s: String? = window.localStorage[property.name]
        return when (T::class) {
            Double::class -> (s?.toDouble() ?: 0.0) as T
            Int::class -> (s?.toInt() ?: 0) as T
            Long::class -> (s?.toLong() ?: 0) as T
            String::class -> (s ?: "") as T
            else -> throw IllegalArgumentException("NOT support type:${T::class})")
        }

    }

    inline operator fun <reified T> setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (value == null) {
            window.localStorage.removeItem(property.name)
        } else {
            window.localStorage[property.name] = value.toString()
        }
    }

}