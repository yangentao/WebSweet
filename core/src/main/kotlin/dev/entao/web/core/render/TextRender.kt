package dev.entao.web.core.render

import dev.entao.web.core.HttpContext

class TextRender(private val context: HttpContext) {

	fun send(value: String) {
		context.sendText(value)
	}
}