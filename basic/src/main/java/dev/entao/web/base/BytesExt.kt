package dev.entao.web.base

import java.nio.ByteOrder

val ByteArray.strUTF8: String get() = String(this, Charsets.UTF_8)

fun Int.toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
	val b0: Byte = ((this shr 24) and 0x00ff).toByte()
	val b1: Byte = ((this shr 16) and 0x00ff).toByte()
	val b2: Byte = ((this shr 8) and 0x00ff).toByte()
	val b3: Byte = (this and 0x00ff).toByte()
	return if (order == ByteOrder.BIG_ENDIAN) {
		byteArrayOf(b0, b1, b2, b3)
	} else {
		byteArrayOf(b3, b2, b1, b0)
	}
}

object ByteConst {
	const val NL: Byte = 0
	const val LK: Byte = 123 // {
	const val RK: Byte = 125 // }
	const val QT: Byte = 34  // "
	const val ES: Byte = 92  // \
	const val SP: Byte = 32  // 空格
	const val CR: Byte = 13  // CR
	const val LF: Byte = 10  // LF
	const val TB: Byte = 9   // TAB
}