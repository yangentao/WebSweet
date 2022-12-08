@file:Suppress("UnsafeCastFromDynamic", "FunctionName", "ClassName")

import org.w3c.dom.Element
import org.w3c.dom.Node


fun bsModal(ele: Element, isStatic: Boolean): bootstrap.Modal {
	return bootstrap.Modal(ele, ModalOptions().apply {
		backdrop = if (isStatic) "static" else true
		keyboard = true
		focus = true
	})
}

fun ModalOptions(): bootstrap.ModalOptions = js("{}")

@JsName("bootstrap")
external class bootstrap {

	class Modal(ele: Node, options: ModalOptions) {
		fun toggle()
		fun show()
		fun hide()
		fun handleUpdate()

		companion object {
			fun getInstance(e: Node): Modal
		}
	}

	interface ModalOptions {
		var backdrop: dynamic
		var keyboard: Boolean?
		var focus: Boolean?
	}
}