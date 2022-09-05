package dev.entao.web.core

import dev.entao.web.base.KClassValue
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions

typealias HttpAction = KFunction<*>

val KClass<*>.actionList: List<HttpAction> by KClassValue { cls ->
    cls.memberFunctions.filter { it.hasAnnotation<Action>() }
}


object HttpConst {
    const val M_HEAD = "HEAD"
    const val M_GET = "GET"
    const val M_POST = "POST"
    const val M_PUT = "PUT"
    const val M_DELETE = "DELETE"
    const val M_TRACE = "TRACE"
    const val M_OPTIONS = "OPTIONS"

    const val H_IFMODSINCE = "If-Modified-Since"
    const val H_LASTMOD = "Last-Modified"
    const val H_IFNONEMATCH = "If-None-Match"
    const val H_ETAG = "ETag"

    const val H_CONTENT_LENGTH = "Content-Length"
    const val H_CONTENT_RANGE = "Content-Range"
    const val H_CONTENT_DISPOSITION = "Content-Disposition"
}


