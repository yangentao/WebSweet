package dev.entao.web.page.ext

import dev.entao.web.tag.tag.AnchorTag
import dev.entao.web.tag.tag.ButtonTag
import dev.entao.web.tag.tag.FormTag
import dev.entao.web.tag.tag.script

fun FormTag.needsValidation() {
    classAdd("needs-validation")
    val formId = this.idx
    script {
        """
            pagescript.formOnSubmitById('$formId');
        """.trimIndent()
    }
}



var ButtonTag.dataScript: String
    get() = getAttr(PageData.script)
    set(value) {
        PageData.script attr value
        onclick = "pagescript.evalElementAttr('${this.idx}')"
    }

var AnchorTag.dataScript: String
    get() = getAttr(PageData.script)
    set(value) {
        PageData.script attr value
        onclick = "pagescript.evalElementAttr('${this.idx}');return false;"
    }