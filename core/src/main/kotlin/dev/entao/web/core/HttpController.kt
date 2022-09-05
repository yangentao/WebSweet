@file:Suppress("unused", "FunctionName")

package dev.entao.web.core

/**
 * Created by entaoyang@163.com on 2016/12/19.
 */

interface OnHttpContext {
	val context: HttpContext
}

open class HttpController(override val context: HttpContext) : OnHttpContext {
	val timeNow: Long = System.currentTimeMillis()
}


open class HttpPages(context: HttpContext) : HttpController(context) {
}

open class HttpApis(context: HttpContext) : HttpController(context) {
	init {
		context.allowCross()
	}
}