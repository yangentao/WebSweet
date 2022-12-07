package dev.entao.web.tag.tag

import kotlin.reflect.full.primaryConstructor


fun Tag.button(vararg classes: String, block: ButtonTag.() -> Unit): ButtonTag {
    return appendTag(*classes) {
        "type" attr "button"
        this.block()
    }
}

fun Tag.submit(vararg classes: String, block: ButtonTag.() -> Unit): ButtonTag {
    return appendTag(*classes) {
        "type" attr "submit"
        this.block()
    }
}

fun Tag.appendText(text: String?): TextTag? {
    if (text != null) {
        return append(TextTag(context, text)) {}
    }
    return null
}

fun Tag.text(text: String?): TextTag? {
    this.children.removeAll { it.tagName == Tag.TEXT_TAG }
    if (text != null) {
        return append(TextTag(context, text)) {}
    }
    return null
}

fun Tag.unsafe(text: String?) {
    if (text != null) {
        append(TextTag(context, text)) { unsafe = true }
    }
}

fun Tag.unsafe(block: () -> String) {
    unsafe(block())
}

fun Tag.text(text: String, block: TextTag.() -> Unit): TextTag {
    return append(TextTag(context, text), block)
}

fun Tag.base(block: BaseTag.() -> Unit): BaseTag {
    return appendTag(block = block)
}

fun Tag.head(vararg classes: String, block: HeadTag.() -> Unit): HeadTag {
    return appendTag(*classes, block = block)
}

fun Tag.body(vararg classes: String, block: HeadTag.() -> Unit): HeadTag {
    return appendTag(*classes, block = block)
}

fun Tag.header(vararg classes: String, block: HeaderTag.() -> Unit): HeaderTag {
    return appendTag(*classes, block = block)
}

fun Tag.main(vararg classes: String, block: MainTag.() -> Unit): MainTag {
    return appendTag(*classes, block = block)
}

fun Tag.div(vararg classes: String, block: DivTag.() -> Unit): DivTag {
    return appendTag(*classes, block = block)
}

fun Tag.a(vararg classes: String, block: AnchorTag.() -> Unit): AnchorTag {
    return appendTag(*classes, block = block)
}

fun Tag.link(
    charset: String? = null, href: String? = null, hreflang: String? = null,
    media: String? = null, referrerpolicy: String? = null, rel: String? = null, rev: String? = null,
    sizes: String? = null, target: String? = null, type: String? = null
): LinkTag {
    return appendTag {
        charset?.also { this.setAttr("charset", it) }
        href?.also { this.setAttr("href", it) }
        hreflang?.also { this.setAttr("hreflang", it) }
        media?.also { this.setAttr("media", it) }
        referrerpolicy?.also { this.setAttr("referrerpolicy", it) }
        rel?.also { this.setAttr("rel", it) }
        rev?.also { this.setAttr("rev", it) }
        sizes?.also { this.setAttr("sizes", it) }
        target?.also { this.setAttr("target", it) }
        type?.also { this.setAttr("type", it) }
    }
}

fun Tag.link(vararg classes: String, block: LinkTag.() -> Unit): LinkTag {
    return appendTag(*classes, block = block)
}

fun Tag.linkCSS(filename: String): LinkTag {
    return link {
        href = filename
        rel = "stylesheet"
    }
}

fun Tag.meta(charset: String? = null, content: String? = null, http_equiv: String? = null, name: String? = null, scheme: String? = null): MetaTag {
    return appendTag {
        charset?.also { this.setAttr("charset", it) }
        content?.also { this.setAttr("content", it) }
        name?.also { this.setAttr("name", it) }
        scheme?.also { this.setAttr("scheme", it) }
        http_equiv?.also { this.setAttr("http-equiv", it) }
    }
}

fun Tag.meta(vararg classes: String, block: MetaTag.() -> Unit): MetaTag {
    return appendTag(*classes, block = block)
}

fun Tag.keywords(ws: List<String>): MetaTag {
    return meta {
        name = "keywords"
        content = ws.joinToString(",")
    }
}

fun Tag.select(vararg classes: String, block: SelectTag.() -> Unit): SelectTag {
    return appendTag(*classes, block = block)
}

fun Tag.option(vararg classes: String, block: OptionTag.() -> Unit): OptionTag {
    return appendTag(*classes, block = block)
}

fun Tag.nav(vararg classes: String, block: NavTag.() -> Unit): NavTag {
    return appendTag(*classes, block = block)
}

fun Tag.form(vararg classes: String, block: FormTag.() -> Unit): FormTag {
    return appendTag(*classes, block = block)
}

fun Tag.script(src: String) {
    appendTag<ScriptTag> {
        this.src = src
    }
}

fun Tag.script(block: () -> String) {
    appendTag<ScriptTag> {
        unsafe(block())
    }
}

fun Tag.label(vararg classes: String, block: LabelTag.() -> Unit): LabelTag {
    return appendTag(*classes, block = block)
}

fun Tag.img(vararg classes: String, block: ImageTag.() -> Unit): ImageTag {
    return appendTag(*classes, block = block)
}

fun Tag.span(vararg classes: String, block: SpanTag.() -> Unit): SpanTag {
    return appendTag(*classes, block = block)
}

fun Tag.hr(vararg classes: String, block: HrTag.() -> Unit): HrTag {
    return appendTag(*classes, block = block)
}

fun Tag.pre(vararg classes: String, block: PreTag.() -> Unit): PreTag {
    return appendTag(*classes, block = block)
}

fun Tag.code(vararg classes: String, block: CodeTag.() -> Unit): CodeTag {
    return appendTag(*classes, block = block)
}

fun Tag.ol(vararg classes: String, block: OlTag.() -> Unit): OlTag {
    return appendTag(*classes, block = block)
}

fun Tag.ul(vararg classes: String, block: UlTag.() -> Unit): UlTag {
    return appendTag(*classes, block = block)
}

fun Tag.li(vararg classes: String, block: LiTag.() -> Unit): LiTag {
    return appendTag(*classes, block = block)
}

fun Tag.h1(vararg classes: String, block: H1Tag.() -> Unit): H1Tag {
    return appendTag(*classes, block = block)
}

fun Tag.h2(vararg classes: String, block: H2Tag.() -> Unit): H2Tag {
    return appendTag(*classes, block = block)
}

fun Tag.h3(vararg classes: String, block: H3Tag.() -> Unit): H3Tag {
    return appendTag(*classes, block = block)
}

fun Tag.h4(vararg classes: String, block: H4Tag.() -> Unit): H4Tag {
    return appendTag(*classes, block = block)
}

fun Tag.h5(vararg classes: String, block: H5Tag.() -> Unit): H5Tag {
    return appendTag(*classes, block = block)
}

fun Tag.h6(vararg classes: String, block: H6Tag.() -> Unit): H6Tag {
    return appendTag(*classes, block = block)
}

fun Tag.p(vararg classes: String, block: PTag.() -> Unit): PTag {
    return appendTag(*classes, block = block)
}

fun Tag.dl(vararg classes: String, block: DlTag.() -> Unit): DlTag {
    return appendTag(*classes, block = block)
}

fun Tag.dt(vararg classes: String, block: DtTag.() -> Unit): DtTag {
    return appendTag(*classes, block = block)
}

fun Tag.dd(vararg classes: String, block: DdTag.() -> Unit): DdTag {
    return appendTag(*classes, block = block)
}

fun Tag.table(vararg classes: String, block: TableTag.() -> Unit): TableTag {
    return appendTag(*classes, block = block)
}

fun Tag.thead(vararg classes: String, block: THeadTag.() -> Unit): THeadTag {
    return appendTag(*classes, block = block)
}

fun Tag.tbody(vararg classes: String, block: TBodyTag.() -> Unit): TBodyTag {
    return appendTag(*classes, block = block)
}

fun Tag.th(vararg classes: String, block: ThTag.() -> Unit): ThTag {
    return appendTag(*classes, block = block)
}

fun Tag.td(vararg classes: String, block: TdTag.() -> Unit): TdTag {
    return appendTag(*classes, block = block)
}

fun Tag.tr(vararg classes: String, block: TrTag.() -> Unit): TrTag {
    return appendTag(*classes, block = block)
}

fun Tag.col(vararg classes: String, block: ColTag.() -> Unit): ColTag {
    return appendTag(*classes, block = block)
}

fun Tag.colgroup(vararg classes: String, block: ColGroupTag.() -> Unit): ColGroupTag {
    return appendTag(*classes, block = block)
}

fun Tag.well(vararg classes: String, block: WellTag.() -> Unit): WellTag {
    return appendTag(*classes, block = block)
}

fun Tag.strong(vararg classes: String, block: StrongTag.() -> Unit): StrongTag {
    return appendTag(*classes, block = block)
}

fun Tag.font(vararg classes: String, block: FontTag.() -> Unit): FontTag {
    return appendTag(*classes, block = block)
}

fun Tag.small(vararg classes: String, block: SmallTag.() -> Unit): SmallTag {
    return appendTag(*classes, block = block)
}

fun Tag.font(size: Int, color: String, block: TagBlock) {
    font {
        "size" attr size.toString()
        "color" attr color
        this.block()
    }
}

fun Tag.textarea(vararg classes: String, block: TextareaTag.() -> Unit): TextareaTag {
    return appendTag(*classes) {
        rows = 3
        this.block()
    }
}

fun Tag.input(vararg classes: String, block: InputTag.() -> Unit): InputTag {
    return appendTag(*classes, block = block)
}

fun Tag.date(vararg classes: String, block: InputTag.() -> Unit): InputTag {
    return input(*classes) {
        type = "date"
        this.block()
    }
}

fun Tag.time(vararg classes: String, block: InputTag.() -> Unit): InputTag {
    return input(*classes) {
        type = "time"
        this.block()
    }
}

fun Tag.datetime(vararg classes: String, block: InputTag.() -> Unit): InputTag {
    return input(*classes) {
        type = "datetime"
        this.block()
    }
}

fun Tag.file(vararg classes: String, block: InputTag.() -> Unit): InputTag {
    return input(*classes) {
        type = "file"
        this.block()
    }
}

fun Tag.password(vararg classes: String, block: InputTag.() -> Unit): InputTag {
    return input(*classes) {
        type = "password"
        this.block()
    }
}

fun Tag.email(vararg classes: String, block: InputTag.() -> Unit): InputTag {
    return input(*classes) {
        type = "email"
        this.block()
    }
}

fun Tag.hidden(vararg classes: String, block: InputTag.() -> Unit): InputTag {
    return input(*classes) {
        type = "hidden"
        this.block()
    }
}

fun Tag.radio(vararg classes: String, block: InputTag.() -> Unit): InputTag {
    return input(*classes) {
        type = "radio"
        this.block()
    }
}

fun Tag.checkbox(vararg classes: String, block: InputTag.() -> Unit): InputTag {
    return input(*classes) {
        type = "checkbox"
        this.block()
    }
}

fun Tag.footer(vararg classes: String, block: FooterTag.() -> Unit): FooterTag {
    return appendTag(*classes, block = block)
}

fun Tag.article(vararg classes: String, block: ArticalTag.() -> Unit): ArticalTag {
    return appendTag(*classes, block = block)
}

fun Tag.datalist(vararg classes: String, block: DatalistTag.() -> Unit): DatalistTag {
    return appendTag(*classes, block = block)
}

fun Tag.stylesheet(url: String) {
    link {
        rel = "stylesheet"
        href = url
    }
}

fun Tag.br(): Tag {
    return tag("br") {}
}

fun Tag.hr(): Tag {
    return this.hr {}
}

fun Tag.label(text: String): Tag {
    return label {
        +text
    }
}

fun Tag.hidden(hiddenName: String, hiddenValue: Any?) {
    this.hidden {
        name = hiddenName
        value = hiddenValue?.toString() ?: ""
    }
}

fun Tag.pArticle(text: String) {
    val textList = text.split("\n")
    for (s in textList) {
        this.p {
            "style" attr "text-indent:2em"
            text(s)?.forView = true
        }
    }
}

private inline fun <reified T : Tag> Tag.appendTag(vararg clses: String, block: T.() -> Unit): T {
    val t: T = append(T::class.primaryConstructor!!.call(context))
    t.classAdd(*clses)
    t.block()
    return t
}


fun Tag.style(block: () -> String): Tag {
    return append(Tag(context, "style")) {
        this.text(block())
    }
}