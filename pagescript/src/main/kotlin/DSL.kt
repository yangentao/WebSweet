@file:Suppress("unused")

import kotlinx.browser.document
import kotlinx.dom.addClass
import org.w3c.dom.*


fun HTMLElement.tag(tagname: String, vararg classes: String): HTMLElement {
    val e = document.createElement(tagname)
    this.appendChild(e)
    if (classes.isNotEmpty()) {
        e.addClass(*classes)
    }
    return e as HTMLElement
}

fun HTMLElement.menuitem(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("menuitem", *classes)
    a.block()
    return a
}

fun HTMLElement.menu(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("menu", *classes)
    a.block()
    return a
}


fun HTMLElement.nav(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("nav", *classes)
    a.block()
    return a
}

fun HTMLElement.small(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("small", *classes)
    a.block()
    return a
}

fun HTMLElement.strike(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("strike", *classes)
    a.block()
    return a
}

fun HTMLElement.strong(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("strong", *classes)
    a.block()
    return a
}

fun HTMLElement.video(vararg classes: String, block: HTMLVideoElement.() -> Unit): HTMLVideoElement {
    val a = tag("video", *classes) as HTMLVideoElement
    a.block()
    return a
}

fun HTMLElement.ul(vararg classes: String, block: HTMLUListElement.() -> Unit): HTMLUListElement {
    val a = tag("ul", *classes) as HTMLUListElement
    a.block()
    return a
}

fun HTMLElement.title(vararg classes: String, block: HTMLTitleElement.() -> Unit): HTMLTitleElement {
    val a = tag("title", *classes) as HTMLTitleElement
    a.block()
    return a
}

fun HTMLElement.time(vararg classes: String, block: HTMLTimeElement.() -> Unit): HTMLTimeElement {
    val a = tag("time", *classes) as HTMLTimeElement
    a.block()
    return a
}

fun HTMLElement.textarea(vararg classes: String, block: HTMLTextAreaElement.() -> Unit): HTMLTextAreaElement {
    val a = tag("textarea", *classes) as HTMLTextAreaElement
    a.block()
    return a
}

fun HTMLElement.section(vararg classes: String, block: HTMLTableSectionElement.() -> Unit): HTMLTableSectionElement {
    val a = tag("section", *classes) as HTMLTableSectionElement
    a.block()
    return a
}

fun HTMLElement.caption(vararg classes: String, block: HTMLTableCaptionElement.() -> Unit): HTMLTableCaptionElement {
    val a = tag("caption", *classes) as HTMLTableCaptionElement
    a.block()
    return a
}

fun HTMLElement.col(vararg classes: String, block: HTMLTableColElement.() -> Unit): HTMLTableColElement {
    val a = tag("col", *classes) as HTMLTableColElement
    a.block()
    return a
}

fun HTMLElement.colgroup(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("colgroup", *classes)
    a.block()
    return a
}

fun HTMLElement.code(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("code", *classes)
    a.block()
    return a
}

fun HTMLElement.center(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("center", *classes)
    a.block()
    return a
}

fun HTMLElement.tr(vararg classes: String, block: HTMLTableRowElement.() -> Unit): HTMLTableRowElement {
    val a = tag("tr", *classes) as HTMLTableRowElement
    a.block()
    return a
}

fun HTMLElement.td(vararg classes: String, block: HTMLTableCellElement.() -> Unit): HTMLTableCellElement {
    val a = tag("td", *classes) as HTMLTableCellElement
    a.block()
    return a
}

fun HTMLElement.table(vararg classes: String, block: HTMLTableElement.() -> Unit): HTMLTableElement {
    val a = tag("table", *classes) as HTMLTableElement
    a.block()
    return a
}

fun HTMLElement.tbody(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("tbody", *classes)
    a.block()
    return a
}

fun HTMLElement.thead(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("thead", *classes)
    a.block()
    return a
}

fun HTMLElement.styleTag(block: HTMLStyleElement.() -> Unit): HTMLStyleElement {
    val a = tag("style") as HTMLStyleElement
    a.block()
    return a
}

fun HTMLElement.span(vararg classes: String, block: HTMLSpanElement.() -> Unit): HTMLSpanElement {
    val a = tag("span", *classes) as HTMLSpanElement
    a.block()
    return a
}


fun HTMLElement.source(vararg classes: String, block: HTMLSourceElement.() -> Unit): HTMLSourceElement {
    val a = tag("source", *classes) as HTMLSourceElement
    a.block()
    return a
}

fun HTMLElement.select(vararg classes: String, block: HTMLSelectElement.() -> Unit): HTMLSelectElement {
    val a = tag("select", *classes) as HTMLSelectElement
    a.block()
    return a
}

fun HTMLSelectElement.option(text: String, value: String, selected: Boolean = false) {
    this.options.add(Option(text = text, value = value, selected = selected))
}


fun HTMLElement.script(vararg classes: String, block: HTMLScriptElement.() -> Unit): HTMLScriptElement {
    val a = tag("script", *classes) as HTMLScriptElement
    a.block()
    return a
}

fun HTMLElement.q(vararg classes: String, block: HTMLQuoteElement.() -> Unit): HTMLQuoteElement {
    val a = tag("q", *classes) as HTMLQuoteElement
    a.block()
    return a
}

fun HTMLElement.progress(vararg classes: String, block: HTMLProgressElement.() -> Unit): HTMLProgressElement {
    val a = tag("progress", *classes) as HTMLProgressElement
    a.block()
    return a
}


fun HTMLElement.pre(vararg classes: String, block: HTMLPreElement.() -> Unit): HTMLPreElement {
    val a = tag("pre", *classes) as HTMLPreElement
    a.block()
    return a
}


fun HTMLElement.picture(vararg classes: String, block: HTMLPictureElement.() -> Unit): HTMLPictureElement {
    val a = tag("picture", *classes) as HTMLPictureElement
    a.block()
    return a
}

fun HTMLElement.param(vararg classes: String, block: HTMLParamElement.() -> Unit): HTMLParamElement {
    val a = tag("param", *classes) as HTMLParamElement
    a.block()
    return a
}

fun HTMLElement.p(vararg classes: String, block: HTMLParagraphElement.() -> Unit): HTMLParagraphElement {
    val a = tag("p", *classes) as HTMLParagraphElement
    a.block()
    return a
}

fun HTMLElement.option(vararg classes: String, block: HTMLOptionElement.() -> Unit): HTMLOptionElement {
    val a = tag("option", *classes) as HTMLOptionElement
    a.block()
    return a
}

@Suppress("FunctionName")
fun HTMLElement.object_(vararg classes: String, block: HTMLObjectElement.() -> Unit): HTMLObjectElement {
    val a = tag("object", *classes) as HTMLObjectElement
    a.block()
    return a
}

fun HTMLElement.ol(vararg classes: String, block: HTMLOListElement.() -> Unit): HTMLOListElement {
    val a = tag("ol", *classes) as HTMLOListElement
    a.block()
    return a
}


fun HTMLElement.meta(vararg classes: String, block: HTMLMetaElement.() -> Unit): HTMLMetaElement {
    val a = tag("meta", *classes) as HTMLMetaElement
    a.block()
    return a
}

fun HTMLElement.media(vararg classes: String, block: HTMLMediaElement.() -> Unit): HTMLMediaElement {
    val a = tag("media", *classes) as HTMLMediaElement
    a.block()
    return a
}

fun HTMLElement.link(vararg classes: String, block: HTMLLinkElement.() -> Unit): HTMLLinkElement {
    val a = tag("link", *classes) as HTMLLinkElement
    a.block()
    return a
}


fun HTMLElement.legend(vararg classes: String, block: HTMLLegendElement.() -> Unit): HTMLLegendElement {
    val a = tag("legend", *classes) as HTMLLegendElement
    a.block()
    return a
}


fun HTMLElement.label(vararg classes: String, block: HTMLLabelElement.() -> Unit): HTMLLabelElement {
    val a = tag("label", *classes) as HTMLLabelElement
    a.block()
    return a
}


fun HTMLElement.li(vararg classes: String, block: HTMLLIElement.() -> Unit): HTMLLIElement {
    val a = tag("li", *classes) as HTMLLIElement
    a.block()
    return a
}

fun HTMLElement.br() {
    tag("br")
}

fun HTMLElement.input(vararg classes: String, block: HTMLInputElement.() -> Unit): HTMLInputElement {
    val a = tag("input", *classes) as HTMLInputElement
    a.block()
    return a
}

fun HTMLElement.image(vararg classes: String, block: HTMLImageElement.() -> Unit): HTMLImageElement {
    val a = tag("img", *classes) as HTMLImageElement
    a.block()
    return a
}

fun HTMLElement.iframe(vararg classes: String, block: HTMLIFrameElement.() -> Unit): HTMLIFrameElement {
    val a = tag("iframe", *classes) as HTMLIFrameElement
    a.block()
    return a
}

fun HTMLElement.hr(vararg classes: String) {
    tag("hr", *classes)
}

fun HTMLElement.frameSet(vararg classes: String, block: HTMLFrameSetElement.() -> Unit): HTMLFrameSetElement {
    val a = tag("frameset", *classes) as HTMLFrameSetElement
    a.block()
    return a
}

fun HTMLElement.frame(vararg classes: String, block: HTMLFrameElement.() -> Unit): HTMLFrameElement {
    val a = tag("frame", *classes) as HTMLFrameElement
    a.block()
    return a
}

fun HTMLElement.form(vararg classes: String, block: HTMLFormElement.() -> Unit): HTMLFormElement {
    val a = tag("form", *classes) as HTMLFormElement
    a.block()
    return a
}

fun HTMLElement.font(vararg classes: String, block: HTMLFontElement.() -> Unit): HTMLFontElement {
    val a = tag("font", *classes) as HTMLFontElement
    a.block()
    return a
}

fun HTMLElement.fieldset(vararg classes: String, block: HTMLFieldSetElement.() -> Unit): HTMLFieldSetElement {
    val a = tag("fieldset", *classes) as HTMLFieldSetElement
    a.block()
    return a
}

fun HTMLElement.em(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("em", *classes)
    a.block()
    return a
}

fun HTMLElement.embed(vararg classes: String, block: HTMLEmbedElement.() -> Unit): HTMLEmbedElement {
    val a = tag("embed", *classes) as HTMLEmbedElement
    a.block()
    return a
}

fun HTMLElement.datalist(vararg classes: String, block: HTMLDataListElement.() -> Unit): HTMLDataListElement {
    val a = tag("datalist", *classes) as HTMLDataListElement
    a.block()
    return a
}

fun HTMLElement.canvas(vararg classes: String, block: HTMLCanvasElement.() -> Unit): HTMLCanvasElement {
    val a = tag("canvas", *classes) as HTMLCanvasElement
    a.block()
    return a
}

fun HTMLElement.div(vararg classes: String, block: HTMLDivElement.() -> Unit): HTMLDivElement {
    val a = tag("div", *classes) as HTMLDivElement
    a.block()
    return a
}


fun HTMLElement.button(vararg classes: String, block: HTMLButtonElement.() -> Unit): HTMLButtonElement {
    val a = tag("button", *classes) as HTMLButtonElement
    a.type = "button"
    a.block()
    return a
}

fun HTMLElement.h6(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("h6", *classes)
    a.block()
    return a
}

fun HTMLElement.h5(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("h5", *classes)
    a.block()
    return a
}

fun HTMLElement.h4(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("h4", *classes)
    a.block()
    return a
}

fun HTMLElement.h3(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("h3", *classes)
    a.block()
    return a
}

fun HTMLElement.h2(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("h2", *classes)
    a.block()
    return a
}

fun HTMLElement.h1(vararg classes: String, block: HTMLElement.() -> Unit): HTMLElement {
    val a = tag("h1", *classes)
    a.block()
    return a
}

fun HTMLElement.a(vararg classes: String, block: HTMLAnchorElement.() -> Unit): HTMLAnchorElement {
    val a = tag("a", *classes) as HTMLAnchorElement
    a.block()
    return a
}


fun HTMLElement.text(text: String) {
    this.textContent = text
}
