package dev.entao.web.tag

val IDENT_HTML = "    "
val QUOT = "\""
fun attrVal(value: String): String {
    val s = value.replace(QUOT, "&quot;")
    return QUOT + s + QUOT
}


fun Appendable.ident(n: Int): Appendable {
    if (n > 0) {
        for (i in 0 until n) {
            this.append(IDENT_HTML)
        }
    }
    return this
}
