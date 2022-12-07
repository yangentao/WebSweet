package dev.entao.web.tag.tag

import dev.entao.web.base.Name
import dev.entao.web.core.HttpContext


class HtmlTag(context: HttpContext) : Tag(context, "html") {
    var lang: String by TagProp
}

class AnchorTag(context: HttpContext) : Tag(context, "a") {
    var href: String by TagProp
    var role: String by TagProp
}

class ButtonTag(context: HttpContext) : Tag(context, "button") {
    var type: String by TagProp
    var role: String by TagProp
}

class LabelTag(context: HttpContext) : Tag(context, "label") {
    @Name("for")
    var forID: String by TagProp

    fun forInputPre() {
        val ls = parent?.children ?: return
        val n = ls.indexOf(this)
        if (n - 1 >= 0) {
            val ch = ls[n - 1]
            if (ch.tagName == "input") {
                forID = ch.idx
                return
            }
        }
    }
}

class InputTag(context: HttpContext) : Tag(context, "input") {
    var type: String by TagProp
    var value: String by TagProp
    var placeholder: String by TagProp
    var step: String by TagProp
    var pattern: String by TagProp
}

class TextareaTag(context: HttpContext) : Tag(context, "textarea") {
    var rows: Int by TagProp
    var placeholder: String by TagProp
}

class FormTag(context: HttpContext) : Tag(context, "form") {
    var action: String by TagProp
    var method: String by TagProp
}

class ScriptTag(context: HttpContext) : Tag(context, "script") {
    var src: String by TagProp
    var type: String by TagProp

    init {
        type = "text/javascript"
    }

    override fun htmlMultiLine(): Boolean {
        return this.children.isNotEmpty()
    }
}

class LinkTag(context: HttpContext) : Tag(context, "link") {
    var rel: String by TagProp
    var href: String by TagProp
}

class OptionTag(context: HttpContext) : Tag(context, "option") {
    var value: String by TagProp
}

class MetaTag(context: HttpContext) : Tag(context, "meta") {
    var content: String by TagProp
    var charset: String by TagProp

}

class ImageTag(context: HttpContext) : Tag(context, "img") {
    var src: String by TagProp
}

open class DivTag(context: HttpContext) : Tag(context, "div") {
    var role: String by TagProp
}

class BaseTag(context: HttpContext) : Tag(context, "base") {
    var href: String by TagProp
}

class HeadTag(context: HttpContext) : Tag(context, "head")
class BodyTag(context: HttpContext) : Tag(context, "body")

open class HeaderTag(context: HttpContext) : Tag(context, "header")
class MainTag(context: HttpContext) : Tag(context, "main")

class SelectTag(context: HttpContext) : Tag(context, "select")

open class NavTag(context: HttpContext) : Tag(context, "nav")

class SpanTag(context: HttpContext) : Tag(context, "span")

class HrTag(context: HttpContext) : Tag(context, "hr")

class PreTag(context: HttpContext) : Tag(context, "pre")
class CodeTag(context: HttpContext) : Tag(context, "code")

class OlTag(context: HttpContext) : Tag(context, "ol")

class UlTag(context: HttpContext) : Tag(context, "ul")

class LiTag(context: HttpContext) : Tag(context, "li")

class H1Tag(context: HttpContext) : Tag(context, "h1")

class H2Tag(context: HttpContext) : Tag(context, "h2")

class H3Tag(context: HttpContext) : Tag(context, "h3")

class H4Tag(context: HttpContext) : Tag(context, "h4")

class H5Tag(context: HttpContext) : Tag(context, "h5")

class H6Tag(context: HttpContext) : Tag(context, "h6")

class PTag(context: HttpContext) : Tag(context, "p")

class DlTag(context: HttpContext) : Tag(context, "dl")

class DtTag(context: HttpContext) : Tag(context, "dt")

class DdTag(context: HttpContext) : Tag(context, "dd")

open class TableTag(context: HttpContext) : Tag(context, "table")

class THeadTag(context: HttpContext) : Tag(context, "thead")

class TBodyTag(context: HttpContext) : Tag(context, "tbody")

class ThTag(context: HttpContext) : Tag(context, "th") {
    var scope: String by TagProp
}

class TdTag(context: HttpContext) : Tag(context, "td")

class TrTag(context: HttpContext) : Tag(context, "tr")

class ColTag(context: HttpContext) : Tag(context, "col")

class ColGroupTag(context: HttpContext) : Tag(context, "colgroup")

class WellTag(context: HttpContext) : Tag(context, "well")

class StrongTag(context: HttpContext) : Tag(context, "strong")

class FontTag(context: HttpContext) : Tag(context, "font")

class SmallTag(context: HttpContext) : Tag(context, "small")

class DatalistTag(context: HttpContext) : Tag(context, "datalist")

class FooterTag(context: HttpContext) : Tag(context, "footer")

class ArticalTag(context: HttpContext) : Tag(context, "article")
