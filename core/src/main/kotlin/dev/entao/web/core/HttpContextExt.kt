package dev.entao.web.core

import java.io.File
import javax.servlet.http.Part

//@表示app的目录
//@/static/a.png

context (OnHttpContext)
val Part.fileTemp: File?
    get() {
        if (this.submittedFileName == null) return null
        if (this.submittedFileName == "") return null //有file元素,但是用户没有选择文件
        val file = context.createTempPartFile()
        this.writeTo(file)
        this.delete()
        return file
    }

context (OnHttpContext)
val String.uri: String
    get() = context.uriOf(this)

context (OnHttpContext)
val String.url: String
    get() = context.urlOf(this)

context (OnHttpContext)
val HttpAction.uri: String
    get() = context.uriAction(this)

context (OnHttpContext)
val HttpAction.url: String
    get() = context.uriAction(this).url



context (OnHttpContext)
fun HttpAction.values(vararg vs: Any): String {
    return context.uriActionValues(this, *vs)

}

context (OnHttpContext)
fun HttpAction.keyValues(vararg ps: Pair<String, Any>): String {
    return context.uriActionKeyValues(this, ps.toList())
}