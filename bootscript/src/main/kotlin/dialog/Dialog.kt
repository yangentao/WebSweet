@file:Suppress("UnsafeCastFromDynamic", "MemberVisibilityCanBePrivate", "unused", "EXPERIMENTAL_API_USAGE", "OPT_IN_USAGE")

package dialog

import attr
import bootstrap
import bsModal
import button
import div
import h6
import htmlValue
import kotlinx.browser.document
import kotlinx.dom.addClass
import on
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import query
import text





fun bootDialog(block: BootDialog.() -> Unit): BootDialog {
    return BootDialog().apply(block)
}


@JsExport
@JsName("BootDialog")
class BootDialog {
    val modalDiv: HTMLElement

    init {
        modalDiv = prepare()
        onHidden { modalDiv.remove() }
    }

    val modalDialog: HTMLDivElement get() = modalDiv.query(".modal-dialog") as HTMLDivElement
    val modalHeader: HTMLDivElement get() = modalDiv.query(".modal-header") as HTMLDivElement
    val modalTitle: HTMLElement get() = modalDiv.query(".modal-title") as HTMLElement
    val modalBody: HTMLDivElement get() = modalDiv.query(".modal-body") as HTMLDivElement
    val modalContainer: HTMLDivElement get() = modalDiv.query(".container-fluid") as HTMLDivElement
    val modalFooter: HTMLDivElement get() = modalDiv.query(".modal-footer") as HTMLDivElement

    fun onHidden(block: (Event) -> Unit) {
        modalDiv.on("hidden.bs.modal") {
            block(it)
        }
    }

    fun onShown(block: (Event) -> Unit) {
        modalDiv.on("shown.bs.modal") {
            block(it)
        }
    }

    fun fullScreen() {
        modalDialog.addClass("modal-fullscreen")
    }

    //300
    fun smallSize() {
        modalDialog.addClass("modal-sm")
    }

    //800
    fun largeSize() {
        modalDialog.addClass("modal-lg")
    }

    //1140px
    fun extraLargeSize() {
        modalDialog.addClass("modal-xl")
    }

    fun container(block: HTMLDivElement.() -> Unit) {
        modalContainer.block()
    }

    fun footer(block: HTMLDivElement.() -> Unit) {
        modalFooter.block()
    }

    fun title(title: String?) {
        if (title == null || title.isEmpty()) {
            modalHeader.remove()
        } else {
            modalTitle.htmlValue = title
        }
    }

    fun buttonCancel(title: String) {
        modalFooter.querySelector("button")?.firstElementChild?.htmlValue = title
    }

    fun buttonPrimary(title: String, block: () -> Unit) {
        modalFooter.button("btn btn-primary") {
            this.attr("data-bs-dismiss", "modal")
//			text(title)
            textContent = title
            on("click") {
                block()
            }
        }

    }


    fun show(): BootDialog {
        bsModal(modalDiv, false).show()
        return this
    }

    fun showStatic(): BootDialog {
        bsModal(modalDiv, true).show()
        return this
    }

    fun hide() {
        bootstrap.Modal.getInstance(modalDiv).hide()
    }

    fun handleUpdate() {
        bootstrap.Modal.getInstance(modalDiv).handleUpdate()
    }

    private fun prepare(): HTMLElement {
        val modal = document.body!!.div("modal") {
            this.tabIndex = -1
            div("modal-dialog modal-dialog-centered modal-dialog-scrollable") {
                div("modal-content") {
                    div("modal-header") {
                        h6("modal-title") {
                            text("Title Dialog")
                        }
                        button("btn-close") {
                            this.attr("data-bs-dismiss", "modal")
                            this.type = "button"
                        }
                    }
                    div("modal-body") {
                        div("container-fluid") {

                        }
                    }
                    div("modal-footer") {
                        button("btn btn-secondary") {
                            this.attr("data-bs-dismiss", "modal")
                            this.type = "button"
                            text("关闭")
                        }
                    }
                }
            }

        }

        return modal
    }
}