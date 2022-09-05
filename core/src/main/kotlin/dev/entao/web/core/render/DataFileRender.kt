package dev.entao.web.core.render

import dev.entao.web.base.Mimes
import dev.entao.web.base.substringBetween
import dev.entao.web.core.HttpConst
import dev.entao.web.core.HttpContext
import dev.entao.web.core.HttpController
import dev.entao.web.core.header
import java.io.File
import java.io.InputStream
import java.net.JarURLConnection
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse

class DataInputInfo(val filename: String, val inputStream: InputStream, val lastModifyTime: Long) {
    val fileLength: Int = inputStream.available()
    var contentType: String = Mimes.ofFile(filename)

    companion object {
        fun fromFile(file: File): DataInputInfo {
            return DataInputInfo(file.name, file.inputStream(), file.lastModified())
        }

        fun fromJar(jarURL: JarURLConnection): DataInputInfo {
            val jar = jarURL.jarFile
            val entry = jarURL.jarEntry
            return DataInputInfo(entry.name, jar.getInputStream(entry), entry.lastModifiedTime.toMillis())
        }
    }
}

class DataFileRender(context: HttpContext, val info: DataInputInfo) : Render(context) {
    var isAttach: Boolean = false
    private val etag: String = "\"${info.fileLength}-${info.lastModifyTime}\""


    override fun onSend() {
        val r = context.response.apply {
            contentType = info.contentType
            setDateHeader(HttpConst.H_LASTMOD, info.lastModifyTime)
            setHeader(HttpConst.H_ETAG, etag)
        }
        if (info.lastModifyTime != 0L && context.request.getDateHeader(HttpConst.H_IFMODSINCE) >= info.lastModifyTime) { // 304
            r.setHeader(HttpConst.H_CONTENT_LENGTH, info.fileLength.toString())
            r.status = HttpServletResponse.SC_NOT_MODIFIED
            return
        }
        if (context.request.getHeader(HttpConst.H_ETAG) == etag) {
            r.setHeader(HttpConst.H_CONTENT_LENGTH, info.fileLength.toString())
            r.status = HttpServletResponse.SC_NOT_MODIFIED
            return
        }


        if (isAttach) {
            r.addHeader(HttpConst.H_CONTENT_DISPOSITION, "attachment;filename=${info.filename}")
        }
        val rangeHead = findRange()
        if (info.fileLength > 0 && rangeHead != null) {
            if (rangeHead.second >= 0) {
                r.setHeader(HttpConst.H_CONTENT_LENGTH, (rangeHead.second - rangeHead.first + 1).toString())
                context.response.addHeader(HttpConst.H_CONTENT_RANGE, "bytes ${rangeHead.first}-${rangeHead.second}/${info.fileLength}")
            } else {
                r.setHeader(HttpConst.H_CONTENT_LENGTH, (info.fileLength - rangeHead.first).toString())
                context.response.addHeader(HttpConst.H_CONTENT_RANGE, "bytes ${rangeHead.first}-${info.fileLength - 1}/${info.fileLength}")
            }
            context.response.status = 206
            val os = r.outputStream
            info.inputStream.use {
                outRange(rangeHead.first, rangeHead.second, it, os)
            }
            os.close()
        } else {
            r.setHeader(HttpConst.H_CONTENT_LENGTH, info.fileLength.toString())
            val os = r.outputStream
            info.inputStream.use {
                it.copyTo(os)
            }
            os.close()
        }
    }

    private fun outRange(start: Int, end: Int, fis: InputStream, os: ServletOutputStream) {
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
    //-100  last 100 bytes
    //0-  all bytes
    //100-200   [100,200]
    //0-0 first byte
    private fun findRange(): Pair<Int, Int>? {
        val range = context.request.header("Range") ?: return null
        val startStr = range.substringBetween('=', '-')?.trim() ?: return null
        val endStr = range.substringAfter('_', "").trim()
        val startBytes = startStr.toIntOrNull() ?: return null
        val endBytes = if (endStr.isEmpty()) -1 else endStr.toInt()
        return Pair(startBytes, endBytes)
    }
}

fun HttpContext.sendDataFile(info: DataInputInfo, block: DataFileRender.() -> Unit) {
    val r = DataFileRender(this, info)
    r.block()
    r.send()
}

fun HttpController.sendDataFile(info: DataInputInfo, block: DataFileRender.() -> Unit) {
    this.context.sendDataFile(info, block)
}