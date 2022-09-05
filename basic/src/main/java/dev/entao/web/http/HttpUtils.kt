package dev.entao.web.http

import java.io.IOException
import java.io.OutputStream


//accept => Accept
//userAgent => User-Agent
internal val String.headerKeyFormat: String
    get() {
        val sb = StringBuilder()
        for (ch: Char in this) {
            if (sb.isEmpty()) {
                sb.append(ch.uppercaseChar())
            } else if (ch.isUpperCase()) {
                sb.append('-').append(ch)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

internal class SizeStream : OutputStream() {
    var size = 0
        private set

    @Throws(IOException::class)
    override fun write(oneByte: Int) {
        ++size
    }

    fun incSize(size: Int) {
        this.size += size
    }

}