@file:Suppress("EXPERIMENTAL_API_USAGE")

import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.*
import org.w3c.dom.url.URL
import org.w3c.files.File
import org.w3c.files.get
import kotlin.js.Date


@JsExport
@JsName("uploadBindById")
fun uploadBindById(eid: String) {
    val div: HTMLDivElement = body.find("#$eid") ?: return
    UploadDiv(div)
}

class UploadDiv(uploadDiv: HTMLDivElement) {

    private val eHidden: HTMLInputElement = uploadDiv.find("input[type='hidden']")!!
    private val eImage: HTMLImageElement? = uploadDiv.find(".yet-upload-image")
    private val eProgress: HTMLDivElement? = uploadDiv.find(".progress-bar")
    private val eInfo: HTMLSpanElement? = uploadDiv.find(".yet-upload-info")
    private val eResult: HTMLSpanElement? = uploadDiv.find(".yet-upload-result")

    private val uploaURL = uploadDiv.attr(UP_URL)!!
    private val uploaURLParam = uploadDiv.attr(UP_PARAM) ?: "file"
    private val imageUrl = uploadDiv.attr(UP_IMG_URL) ?: ""
    private val imageParam = uploadDiv.attr(UP_IMG_PARAM) ?: "id"
    private val limitSizeM = uploadDiv.attr(UP_SIZE_LIMIT_M)?.toIntOrNull() ?: 0

    private val uploadID: String
        get() {
            val v = eHidden.value
            if (v.isEmpty() || v == "0") return ""
            return v
        }

    init {
        uploadDiv.ondragover = { e ->
            e.preventDefault()
        }
        uploadDiv.ondrop = { e ->
            e.preventDefault()
            onDropFile(e)
            true
        }
        setupImage()
    }

    private fun setupImage() {
        if (imageUrl.isNotEmpty() && uploadID.isNotEmpty()) {
            eImage?.src = URLBuilder(imageUrl).apply {
                set(imageParam, uploadID)
            }.toString()
        }else {
            eImage?.src = ""
        }
    }

    private fun onDropFile(e: DragEvent) {
        val files = e.dataTransfer?.files ?: return
        if (files.length == 0) return
        val file: File = files[0] ?: return
        logd(file.type, file.size, file.name, file.lastModified)
        val filesize = file.size.toInt()

        val szText: String = readableSize(filesize)

        eInfo?.textContent = "名称:${file.name}, 大小:${szText}"

        if ("image" in file.type) {
            eImage?.src = URL.createObjectURL(file)
        } else {
            eImage?.src = ""
        }
        if (limitSizeM > 0 && filesize > limitSizeM * MB) {
            eResult?.failed("文件太大, 不能超过${limitSizeM}M")
            return
        }

        var preTime = Date().getTime()
        HttpPost(uploaURL).formData {
            set(uploaURLParam, file, file.name)
        }.onProgressUpload { pe ->
            val nowDate = Date().getTime()
            if (nowDate - preTime >= 100 || pe.loaded === pe.total) {
                val percent = pe.loaded.toInt() * 100 / pe.total.toInt()
                bsProgressSet(percent)
            }
            preTime = nowDate
        }.onFailed {
            eResult?.failed("上传失败: ${it.status} " + it.msg)
        }.requestJsonResult { r ->
            if (r.OK) {
                eResult?.success("上传成功")
                eHidden.value = (r.dataObject["id"]).toString()
                setupImage()
            } else {
                eResult?.failed("上传失败" + r.msg)
            }
        }

    }

    private fun bsProgressSet(v: Int) {
        eProgress?.apply {
            style.width = "$v%"
            attr("aria-valuenow", v.toString())
            textContent = "$v%"
        }
    }

    companion object {
        private const val UP_SIZE_LIMIT_M = "data-upload-limit-m"
        private const val UP_URL = "data-upload-url"
        private const val UP_PARAM = "data-upload-param"
        private const val UP_IMG_URL = "data-upload-image"
        private const val UP_IMG_PARAM = "data-upload-imageparam"
    }
}

private fun HTMLSpanElement.success(text: String) {
    this.textContent = text
    this.removeClass("text-danger")
    this.addClass("text-success")
}

private fun HTMLSpanElement.failed(text: String) {
    this.textContent = text
    this.removeClass("text-success")
    this.addClass("text-danger")
}

private fun readableSize(filesize: Int): String {
    return when {
        filesize > GB -> (filesize * 1.0 / GB).toString().keep(2) + "G"
        filesize > MB -> (filesize * 1.0 / MB).toString().keep(2) + "M"
        filesize > KB -> (filesize * 1.0 / KB).toString().keep(2) + "K"
        else -> filesize.toString() + "字节"
    }
}