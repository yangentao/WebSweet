package dev.entao.web.page.dialog

import dev.entao.web.page.ext.PageData
import dev.entao.web.tag.tag.AnchorTag
import dev.entao.web.tag.tag.ButtonTag
import dev.entao.web.tag.tag.ImageTag


var ImageTag.dataSrcLarge: String
    get() = getAttr(PageData.srcLarge)
    set(value) {
        PageData.srcLarge attr value
        onclick = "client.dialog.showLargeImageById('${this.idx}')"
    }


var AnchorTag.dataConfirm: String
    get() = getAttr(PageData.confirm)
    set(value) {
        PageData.confirm attr value
        onclick = "client.dialog.confirmById('${this.idx}')"
    }
var ButtonTag.dataConfirm: String
    get() = getAttr(PageData.confirm)
    set(value) {
        PageData.confirm attr value
        onclick = "client.dialog.confirmById('${this.idx}')"
    }


var ButtonTag.dataModal: String
    get() = getAttr(PageData.modal)
    set(value) {
        PageData.modal attr value
        onclick = "client.dialog.showDialogById('${this.idx}');return false"
    }
var AnchorTag.dataModal: String
    get() = getAttr(PageData.modal)
    set(value) {
        PageData.modal attr value
        onclick = "client.dialog.showDialogById('${this.idx}');return false"
    }