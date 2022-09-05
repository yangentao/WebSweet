@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.web.json

import java.util.*


abstract class YsonValue {

	abstract fun yson(buf: StringBuilder)

	open fun yson(): String {
		val sb = StringBuilder(preferBufferSize())
		yson(sb)
		return sb.toString()
	}

	open fun preferBufferSize(): Int {
		return 64
	}

	override fun toString(): String {
		return yson()
	}

	val isCollection: Boolean get() = this is YsonObject || this is YsonArray


	companion object {
		fun from(value: Any?): YsonValue {
			if (value == null) {
				return YsonNull.inst
			}
			if (value is YsonValue) {
				return value
			}
			return YsonCoders[value::class]?.toYsonValue(value) ?: YsonString(value.toString())
		}
	}
}

class YsonNull private constructor() : YsonValue() {

	override fun yson(buf: StringBuilder) {
		buf.append("null")
	}

	override fun equals(other: Any?): Boolean {
		return other is YsonNull
	}

	override fun preferBufferSize(): Int {
		return 8
	}

	override fun hashCode(): Int {
		return 1000
	}

	companion object {
		val inst: YsonNull = YsonNull()
	}
}

class YsonNum(val data: Number) : YsonValue() {

	override fun yson(buf: StringBuilder) {
		buf.append(data.toString())
	}

	override fun equals(other: Any?): Boolean {
		if (other is YsonNum) {
			return other.data == data
		}
		return false
	}

	override fun hashCode(): Int {
		return data.hashCode()
	}

	override fun preferBufferSize(): Int {
		return 12
	}

}

class YsonString(val data: String) : YsonValue() {

	constructor(v: Char) : this(String(charArrayOf(v)))
	constructor(v: StringBuffer) : this(v.toString())
	constructor(v: StringBuilder) : this(v.toString())

	override fun yson(buf: StringBuilder) {
		buf.append("\"")
		buf.append(escapeJson(data))
		buf.append("\"")
	}

	override fun equals(other: Any?): Boolean {
		if (other is YsonString) {
			return other.data == data
		}
		return false
	}

	override fun hashCode(): Int {
		return data.hashCode()
	}

	override fun preferBufferSize(): Int {
		return data.length + 8
	}
}

class YsonBool(val data: Boolean) : YsonValue() {
	override fun yson(buf: StringBuilder) {
		if (data) {
			buf.append("true")
		} else {
			buf.append("false")
		}
	}

	override fun equals(other: Any?): Boolean {
		if (other is YsonBool) {
			return other.data == data
		}
		return false
	}

	override fun hashCode(): Int {
		return data.hashCode()
	}

	override fun preferBufferSize(): Int {
		return 8
	}

	companion object {
		val True: YsonBool = YsonBool(true)
		val False: YsonBool = YsonBool(false)
	}
}


class YsonBlob(val data: ByteArray) : YsonValue() {

	constructor(v: java.sql.Blob) : this(v.getBytes(1, v.length().toInt()))

	override fun yson(buf: StringBuilder) {
		buf.append("\"")
		buf.append(encoded)
		buf.append("\"")
	}

	val encoded: String get() = encode(data)

	override fun equals(other: Any?): Boolean {
		if (other is YsonBlob) {
			return other.data.contentEquals(data)
		}
		return false
	}

	override fun hashCode(): Int {
		return data.hashCode()
	}

	override fun preferBufferSize(): Int {
		return data.size * 4 / 3 + 4
	}

	companion object {
		fun encode(data: ByteArray): String {
			return Base64.getUrlEncoder().encodeToString(data)
		}

		fun decode(s: String): ByteArray {
			return Base64.getUrlDecoder().decode(s)
		}

	}

}

fun escapeJson(s: String): String {
	var n = 0
	for (c in s) {
		if (c == '\\' || c == '\"') {
			n += 1
		}
	}
	if (n == 0) {
		return s
	}
	val sb = StringBuilder(s.length + n)
	for (c in s) {
		if (c == '\\' || c == '\"') {
			sb.append("\\")
		}
		sb.append(c)
	}
	return sb.toString()
}
