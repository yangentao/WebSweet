package dev.entao.web.tag.tag

import dev.entao.web.core.HttpContext
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

fun createTagInstance(context: HttpContext, name: String): Tag {
    return tagMap[name]?.let { it.primaryConstructor!!.call(context) as Tag } ?: Tag(context, name)
}


private val tagMap: Map<String, KClass<*>> = mapOf(
    "main" to MainTag::class,
    "header" to DivTag::class,
    "head" to DivTag::class,
    "body" to DivTag::class,
    "div" to DivTag::class,
    "a" to AnchorTag::class,
    "button" to ButtonTag::class,
    "link" to LinkTag::class,
    "meta" to MetaTag::class,
    "select" to SelectTag::class,
    "option" to OptionTag::class,
    "nav" to NavTag::class,
    "form" to FormTag::class,
    "script" to ScriptTag::class,
    "label" to LabelTag::class,
    "img" to ImageTag::class,
    "span" to SpanTag::class,
    "hr" to HrTag::class,
    "pre" to PreTag::class,
    "code" to CodeTag::class,
    "ol" to OlTag::class,
    "ul" to UlTag::class,
    "li" to LiTag::class,
    "h1" to H1Tag::class,
    "h2" to H2Tag::class,
    "h3" to H3Tag::class,
    "h4" to H4Tag::class,
    "h5" to H5Tag::class,
    "h6" to H6Tag::class,
    "p" to PTag::class,
    "dl" to DlTag::class,
    "dt" to DtTag::class,
    "dd" to DdTag::class,
    "table" to TableTag::class,
    "thead" to THeadTag::class,
    "tbody" to TBodyTag::class,
    "th" to ThTag::class,
    "tr" to TrTag::class,
    "td" to TdTag::class,
    "col" to ColTag::class,
    "colgroup" to ColGroupTag::class,
    "well" to WellTag::class,
    "small" to SmallTag::class,
    "font" to FontTag::class,
    "strong" to StrongTag::class,
    "textarea" to TextareaTag::class,
    "input" to InputTag::class,
    "datalist" to DatalistTag::class,
    "footer" to FooterTag::class,
    "article" to ArticalTag::class,
    "base" to BaseTag::class,
)
