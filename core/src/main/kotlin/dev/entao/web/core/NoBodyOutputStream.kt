package dev.entao.web.core

import java.io.IOException
import java.text.MessageFormat
import java.util.*
import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener


internal class NoBodyOutputStream : ServletOutputStream() {
    var contentLength = 0
        private set

    override fun write(b: Int) {
        contentLength++
    }

    @Throws(IOException::class)
    override fun write(buf: ByteArray, offset: Int, len: Int) {
        if (offset < 0 || len < 0 || offset + len > buf.size) {
            var msg = lStrings.getString("err.io.indexOutOfBounds")
            val msgArgs = arrayOfNulls<Any>(3)
            msgArgs[0] = Integer.valueOf(offset)
            msgArgs[1] = Integer.valueOf(len)
            msgArgs[2] = Integer.valueOf(buf.size)
            msg = MessageFormat.format(msg, *msgArgs)
            throw IndexOutOfBoundsException(msg)
        }
        contentLength += len
    }

    override fun isReady(): Boolean {
        return false
    }

    override fun setWriteListener(writeListener: WriteListener) {}

    companion object {
        private const val LSTRING_FILE = "javax.servlet.http.LocalStrings"
        private val lStrings = ResourceBundle.getBundle(LSTRING_FILE)
    }
}
