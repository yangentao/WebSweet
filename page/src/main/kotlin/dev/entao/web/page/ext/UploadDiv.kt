@file:Suppress("MemberVisibilityCanBePrivate")

package dev.entao.web.page.ext

import dev.entao.web.base.Prop
import dev.entao.web.base.Prop0
import dev.entao.web.base.paramNames
import dev.entao.web.base.userName
import dev.entao.web.core.HttpAction
import dev.entao.web.core.HttpContext
import dev.entao.web.core.controllers.UploadController
import dev.entao.web.tag.tag.*
import kotlin.reflect.full.findAnnotation


fun Tag.inputFileRef(prop: Prop, block: UploadDiv.() -> Unit = {}) {
    upload {
        fromProp(prop)
        this.block()
    }

}

fun Tag.upload(block: UploadDiv.() -> Unit): UploadDiv {
    return append(UploadDiv(context), block )
}

class UploadDiv(context: HttpContext) : DivTag(context) {
    val hiddenTag: InputTag
    val resultSpan: SpanTag
    val imgTag: ImageTag
    val progressDiv: DivTag
    val tipSpan: SpanTag

    fun uploadAction(action: HttpAction) {
        URL attr context.uriAction(action)
        PARAM attr (action.paramNames.firstOrNull() ?: "file")
    }

    fun imageAction(action: HttpAction) {
        IMG_URL attr context.uriAction(action)
        IMG_PARAM attr action.paramNames.first()
    }


    init {
        this.classAdd("yet-upload-div")
        URL attr context.uriAction(UploadController::upload)
        PARAM attr (UploadController::upload.paramNames.firstOrNull() ?: "file")
        IMG_URL attr context.uriAction(UploadController::media)
        IMG_PARAM attr UploadController::media.paramNames.first()

        hiddenTag = hidden { }
        imgTag = img("yet-upload-image") { }
        progressDiv = div("progress", "yet-upload-progress") {
            div("progress-bar") {
                role = "progressbar"
                style = "width:0%;"
                "aria-valuenow" attr "0"
                "aria-valuemin" attr "0"
                "aria-valuemax" attr "100"
                this.text("0%")
            }
        }
        tipSpan = span("yet-upload-info") {
            +"将文件拖拽到此区域"
        }
        resultSpan = span("yet-upload-result") { }

        script {
            """
                pagescript.uploadBindById('${this.idx}');
            """.trimIndent()
        }

    }

    fun fromProp(prop: Prop) {
        SIZE_LIMIT_M attr (prop.findAnnotation<RefUpload>()?.limitSizeM ?: 0).toString()
        hiddenTag.name = prop.userName
        hiddenTag.value = valueOf(prop) ?: ""
        if (prop is Prop0) {
            resultSpan.text(prop.displayString(null))
        }

    }

    companion object {
        private const val SIZE_LIMIT_M = "data-upload-limit-m"
        private const val URL = "data-upload-url"
        private const val PARAM = "data-upload-param"
        private const val IMG_URL = "data-upload-image"
        private const val IMG_PARAM = "data-upload-imageparam"
    }
}