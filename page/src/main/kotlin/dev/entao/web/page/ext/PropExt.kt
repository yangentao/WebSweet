package dev.entao.web.page.ext

import dev.entao.web.base.Name
import dev.entao.web.tag.tag.AnchorTag
import dev.entao.web.tag.tag.ButtonTag
import dev.entao.web.tag.tag.TagProp


const val PatternDouble = "[0-9]+([\\.][0-9]+)?"

object PageConst {
    const val QUERY_FORM = "queryForm"
    const val MSG_DIV = "messageDiv"


}

object PageData {
    const val script = "data-yet-script"
    const val modal = "data-yet-modal"
    const val key = "data-yet-key"
    const val url = "data-yet-url"
    const val search = "data-yet-search"
    const val confirm = "data-yet-confirm"
    const val confirm2 = "data-yet-confirm2"
    const val srcLarge = "data-src-large"

}


@Name(PageData.search)
var AnchorTag.dataSearch: String by TagProp

@Name(PageData.url)
var AnchorTag.dataUrl: String by TagProp

@Name(PageData.key)
var AnchorTag.datakey: String by TagProp


@Name(PageData.search)
var ButtonTag.dataSearch: String by TagProp

@Name(PageData.url)
var ButtonTag.dataUrl: String by TagProp

@Name(PageData.key)
var ButtonTag.datakey: String by TagProp