package dev.entao.web.core

import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.UnsupportedEncodingException
import java.util.*
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper


internal class NoBodyResponse(r: HttpServletResponse) : HttpServletResponseWrapper(r) {
    private val noBody: NoBodyOutputStream = NoBodyOutputStream()
    private var printWriter: PrintWriter? = null
    private var didSetContentLength = false
    private var usingOutputStream = false

    // file private
    fun setContentLength() {
        if (!didSetContentLength) {
            printWriter?.flush()
            setContentLength(noBody.contentLength)
        }
    }

    override fun setContentLength(len: Int) {
        super.setContentLength(len)
        didSetContentLength = true
    }

    override fun setContentLengthLong(len: Long) {
        super.setContentLengthLong(len)
        didSetContentLength = true
    }

    override fun setHeader(name: String, value: String) {
        super.setHeader(name, value)
        checkHeader(name)
    }

    override fun addHeader(name: String, value: String) {
        super.addHeader(name, value)
        checkHeader(name)
    }

    override fun setIntHeader(name: String, value: Int) {
        super.setIntHeader(name, value)
        checkHeader(name)
    }

    override fun addIntHeader(name: String, value: Int) {
        super.addIntHeader(name, value)
        checkHeader(name)
    }

    private fun checkHeader(name: String) {
        if ("content-length".equals(name, ignoreCase = true)) {
            didSetContentLength = true
        }
    }

    @Throws(IOException::class)
    override fun getOutputStream(): ServletOutputStream {
        check(printWriter == null) { lStrings.getString("err.ise.getOutputStream") }
        usingOutputStream = true
        return noBody
    }

    @Throws(UnsupportedEncodingException::class)
    override fun getWriter(): PrintWriter {
        check(!usingOutputStream) { lStrings.getString("err.ise.getWriter") }
        if (printWriter == null) {
            val w = OutputStreamWriter(noBody, characterEncoding)
            printWriter = PrintWriter(w)
        }
        return printWriter!!
    }

    companion object {
        private val lStrings = ResourceBundle.getBundle("javax.servlet.http.LocalStrings")
    }
}

