package dev.entao.web.core.render

import dev.entao.web.base.Mimes
import dev.entao.web.base.substringBetween
import dev.entao.web.core.HttpConst
import dev.entao.web.core.HttpContext
import dev.entao.web.core.HttpController
import dev.entao.web.core.header
import java.io.File
import java.io.FileInputStream
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse


class FileRender(context: HttpContext, val file: File) : Render(context) {
    var contentType: String = Mimes.ofFile(file.name)
    var filename: String = file.name
    var isAttach: Boolean = false
    private val fileLength: Long = file.length()
    private val modifyTime: Long = file.lastModified()
    val etag: String = "\"$fileLength-$modifyTime\""


    override fun onSend() {
        if (!file.exists() || !file.isFile) {
            return context.sendError(404)
        }
        val r = context.response
        r.contentType = this.contentType
        r.setDateHeader(HttpConst.H_LASTMOD, modifyTime)
        r.setHeader(HttpConst.H_ETAG, etag)
        if (context.request.getDateHeader(HttpConst.H_IFMODSINCE) >= modifyTime) { // 304
            r.setHeader(HttpConst.H_CONTENT_LENGTH, fileLength.toString())
            r.status = HttpServletResponse.SC_NOT_MODIFIED
            return
        }
        if (context.request.getHeader(HttpConst.H_ETAG) == etag) {
            r.setHeader(HttpConst.H_CONTENT_LENGTH, fileLength.toString())
            r.status = HttpServletResponse.SC_NOT_MODIFIED
            return
        }


        if (isAttach) {
            r.addHeader(HttpConst.H_CONTENT_DISPOSITION, "attachment;filename=$filename")
        }
        val rangeHead = findRange()
        if (fileLength > 0 && rangeHead != null) {
            if (rangeHead.second >= 0) {
                r.setHeader(HttpConst.H_CONTENT_LENGTH, (rangeHead.second - rangeHead.first + 1).toString())
                context.response.addHeader(HttpConst.H_CONTENT_RANGE, "bytes ${rangeHead.first}-${rangeHead.second}/$fileLength")
            } else {
                r.setHeader(HttpConst.H_CONTENT_LENGTH, (fileLength - rangeHead.first).toString())
                context.response.addHeader(HttpConst.H_CONTENT_RANGE, "bytes ${rangeHead.first}-${fileLength - 1}/$fileLength")
            }
            context.response.status = 206
            val os = r.outputStream
            file.inputStream().use {
                outRange(rangeHead.first, rangeHead.second, it, os)
            }
            os.close()
        } else {
            r.setHeader(HttpConst.H_CONTENT_LENGTH, fileLength.toString())
            val os = r.outputStream
            file.inputStream().use {
                it.copyTo(os)
            }
            os.close()
        }
    }

    private fun outRange(start: Int, end: Int, fis: FileInputStream, os: ServletOutputStream) {
        if (start > 0) {
            fis.skip(start.toLong())
        }
        if (end == start) {
            val b = fis.read()
            os.write(b)
            return
        }
        //-1
        if (end < start) {
            fis.copyTo(os)
            return
        }
        val total = end - start + 1
        var readed = 0
        val buf = ByteArray(4096)
        do {
            val n = fis.read(buf)
            if (n < 0) {
                return
            }
            if (readed + n <= total) {
                os.write(buf, 0, n)
                readed += n
                continue
            }
            if (readed < total) {
                os.write(buf, 0, total - readed)
                return
            }
            return
        } while (true)
    }

    //Range: bytes=0-801
    private fun findRange(): Pair<Int, Int>? {
        val range = context.request.header("Range") ?: return null
        val startStr = range.substringBetween('=', '-')?.trim() ?: return null
        val endStr = range.substringAfter('_', "").trim()
        val startBytes = startStr.toIntOrNull() ?: return null
        val endBytes = if (endStr.isEmpty()) -1 else endStr.toInt()
        return Pair(startBytes, endBytes)
    }
}

fun HttpContext.sendFile(file: File, block: FileRender.() -> Unit) {
    val r = FileRender(this, file)
    r.block()
    r.send()
}

fun HttpController.sendFile(file: File, block: FileRender.() -> Unit) {
    this.context.sendFile(file, block)
}