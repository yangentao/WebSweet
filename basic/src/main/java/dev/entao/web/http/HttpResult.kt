@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.web.http

import dev.entao.web.json.YsonArray
import dev.entao.web.json.YsonObject
import dev.entao.web.log.logd
import dev.entao.web.log.loge
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.concurrent.TimeoutException


class HttpResult(val url: String) {
	var buffer: ByteArray? = null//如果Http.request参数给定了文件参数, 则,response是null
	var code: Int = 0//200
	var msg: String? = null//OK
	var contentType: String? = null
	var contentLength: Int = 0//如果是gzip格式, 这个值!=response.length
	var headers: Map<String, List<String>>? = null
	var exception: Exception? = null

	var needDecode: Boolean = false

	val OK: Boolean get() = code in 200..299

	val errorMessage: String?
		get() {
			return when (val ex = exception) {
				null -> httpMsgByCode(code)
				is NoRouteToHostException -> "NO Router To Host"
				is TimeoutException -> "Request Timeout"
				is SocketTimeoutException -> "Request Timeout"
				is SocketException -> "Socket Error"
				is FileNotFoundException -> "File Not Found"
				else -> ex.localizedMessage
			}
		}
	//Content-Type: text/html; charset=GBK
	val contentCharset: Charset?
		get() {
			val ct = contentType ?: return null
			val code = ct.lowercase().substringAfter("charset", "").substringAfter("=", "").substringBefore(";").trim()
			if (code.isNotEmpty()) return Charset.forName(code.uppercase())
			return null
		}

	fun bufferToString(charset: Charset = Charsets.UTF_8): String? {
		val r = this.buffer ?: return null
		val ch = contentCharset ?: charset
		var s = String(r, ch)
		if (needDecode) {
			s = URLDecoder.decode(s, ch.name())
		}
		return s
	}

	internal fun dump() {
		logd(">>Response:", this.url)
		logd("  >>status:", code, msg ?: "")
		val map = this.headers
		if (map != null) {
			for ((k, v) in map) {
				if (v.size == 1) {
					logd("  >>head:", k, "=", v.first())
				} else {
					logd("  >>head:", k, "=", "[" + v.joinToString(",") + "]")
				}
			}
		}
		if (this.exception != null) {
			logd("  >>Exception:", this.exception?.localizedMessage)
		}
		if (allowDump(this.contentType)) {
			logd("  >>body:", this.bufferToString())
		}

	}

	val valueBytes: ByteArray?
		get() {
			if (OK) {
				return buffer
			}
			return null
		}

	val valueText: String?
		get() {
			if (OK) {
				return this.bufferToString(Charsets.UTF_8)
			}
			return null
		}

	fun <T> textTo(block: (String) -> T): T? {
		val s = valueText
		if (s != null && s.isNotEmpty()) {
			try {
				return block(s)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
		return null
	}

	fun ysonArray(): YsonArray? {
		return textTo { YsonArray(it) }
	}

	fun ysonObject(): YsonObject? {
		return textTo { YsonObject(it) }
	}


	fun saveTo(file: File): Boolean {
		val data = this.valueBytes ?: return false
		val dir = file.parentFile
		if (dir != null) {
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					loge("创建目录失败")
					return false
				}
			}
		}
		FileOutputStream(file).use {
			it.write(data)
			it.flush()
		}
		return false
	}

}