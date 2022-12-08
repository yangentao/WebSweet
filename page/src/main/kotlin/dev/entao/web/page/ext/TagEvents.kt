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
            client.formOnSubmitById('$formId');
        """.trimIndent()
    }
}



var ButtonTag.dataScript: String
    get() = getAttr(PageData.script)
    set(value) {
        PageData.script attr value
        onclick = "client.evalElementAttr('${this.idx}')"
    }

var AnchorTag.dataScript: String
    get() = getAttr(PageData.script)
    set(value) {
        PageData.script attr value
        onclick = "client.evalElementAttr('${this.idx}');return false;"
    }