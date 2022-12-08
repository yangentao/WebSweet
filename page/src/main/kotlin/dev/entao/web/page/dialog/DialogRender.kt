@file:Suppress("unused")

package dev.entao.web.page.dialog

import dev.entao.web.base.Label
import dev.entao.web.base.Mimes
import dev.entao.web.core.HttpAction
import dev.entao.web.core.HttpContext
import dev.entao.web.core.uri
import dev.entao.web.page.ext.needsValidation
import dev.entao.web.tag.render.TagRender
import dev.entao.web.tag.tag.*
import kotlin.reflect.full.findAnnotation

class DialogRender(context: HttpContext) : TagRender(context) {
    val dialogTag: BootDialogTag = BootDialogTag(context)
    override val renderTag: Tag get() = dialogTag
    override val contentType: String = Mimes.HTML

    fun fullScreen() {
        dialogTag.classAdd("modal-fullscreen")
    }

    fun smallSize300() {
        dialogTag.classAdd("modal-sm")
    }

    fun largeSize800() {
        dialogTag.classAdd("modal-lg")
    }

    fun extraLargeSize1140() {
        dialogTag.classAdd("modal-xl")
    }

    fun dialogTitle(text: String) {
        dialogTitle { +text }
    }

    fun dialogTitle(block: H5Tag.() -> Unit) {
        dialogTag.titleTag.apply(block)
    }

    fun dialogFooter(block: DivTag.() -> Unit) {
        dialogTag.footerDiv.apply(block)
    }

    fun dialogContainer(block: DivTag.() -> Unit) {
        dialogTag.containerDiv.apply(block)
    }

    fun removeFooter() {
        dialogTag.footerDiv.removeFromParent()
    }

    fun dialogForm(action: HttpAction, block: FormTag.() -> Unit) {
        dialogTag.buildForm(action, block)
    }

}

class BootDialogTag(context: HttpContext) : DivTag(context) {
    lateinit var titleTag: H5Tag
        private set
    lateinit var containerDiv: DivTag
        private set
    lateinit var footerDiv: DivTag
        private set
    lateinit var errorTag: H6Tag
        private set

    init {
        classAdd("modal-dialog", "modal-dialog-centered", "modal-dialog-scrollable")
        div("modal-content") {
            div("modal-header") {
                titleTag = h5("modal-title") { }
                button("btn-close") {
                    "data-bs-dismiss" attr "modal"
                    "aria-label" attr "Close"
                }

            }
            div("modal-body") {
                containerDiv = div("container-fluid") {
                    div("row") {
                        errorTag = h6("text-danger", "mx-2") {
                            id = "dialogErrorMessage"
                        }
                    }
                }
            }
            footerDiv = div("modal-footer") {
                button("btn", "btn-secondary") {
                    "data-bs-dismiss" attr "modal"
                    +"关闭"
                }
            }
        }
    }

    fun container(block: DivTag.() -> Unit) {
        containerDiv.block()
    }

    fun buildForm(action: HttpAction, block: FormTag.() -> Unit) {
        footerDiv.removeFromParent()
        containerDiv.apply {
            form("row", "g-3") {
                method = "POST"
                this.action = action.uri
                needsValidation()
                "novalidate" attr "novalidate"
                "onsubmit" attr "return false;"
                this.block()

                div("row my-3") {
                    div("col-auto") {
                        button("btn", "btn-primary") {
                            onclick = "client.dialog.submit(this);"
                            val an = action.findAnnotation<Label>()
                            if (an != null && an.value.isNotEmpty()) {
                                +an.value
                            } else {
                                +"提交"
                            }
                        }
                    }
                }
            }
        }
    }
}