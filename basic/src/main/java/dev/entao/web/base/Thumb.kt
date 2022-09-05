package dev.entao.web.base

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Thumb(val file: File) {

	private var oldImg: BufferedImage? = null
	private var oldWidth = 0
	private var oldHeight = 0

	private var maxEdge: Int = 0
	private var minEdge: Int = 0
	private var maxWidth: Int = 0
	private var maxHeight: Int = 0

	init {
		try {
			oldImg = ImageIO.read(file)
			println(oldImg?.isAlphaPremultiplied)
			oldWidth = oldImg?.width ?: 0
			oldHeight = oldImg?.height ?: 0
		} catch (ex: Exception) {
			ex.printStackTrace()
		}
	}

	fun maxEdge(n: Int): Thumb {
		this.maxEdge = n
		return this
	}

	fun minEdge(n: Int): Thumb {
		this.minEdge = n
		return this
	}

	fun maxWidth(n: Int): Thumb {
		this.maxWidth = n
		return this
	}

	fun maxHeight(n: Int): Thumb {
		this.maxHeight = n
		return this
	}

	private fun saveToFile(newW: Int, newH: Int, newFile: File): Boolean {
		if (newW >= oldWidth && newH >= oldHeight) {
			val f = file.copyTo(newFile, true)
			return f.exists()
		}
		try {
			val img = oldImg ?: return false
			val dst = BufferedImage(newW, newH, img.type)
			val g = dst.graphics
			g.drawImage(img, 0, 0, newW, newH, null)
			val a = if (img.type in listOf(2, 3, 6, 7)) "png" else "jpg"
			return ImageIO.write(dst, a, newFile)
		} catch (ex: Exception) {
			ex.printStackTrace()
		}
		return false
	}

	fun toFile(newFile: File): Boolean {
		if (oldWidth <= 0 || oldHeight <= 0) {
			return false
		}
		var newW = 0
		var newH = 0
		if (maxWidth > 0) {
			newW = maxWidth
			newH = newW * oldHeight / oldWidth
		} else if (maxHeight > 0) {
			newH = maxHeight
			newW = newH * oldWidth / oldHeight
		} else if (maxEdge > 0) {
			if (oldWidth > oldHeight) {
				newW = maxEdge
				newH = newW * oldHeight / oldWidth
			} else {
				newH = maxEdge
				newW = newH * oldWidth / oldHeight
			}
		} else if (minEdge > 0) {
			if (oldWidth > oldHeight) {
				newH = minEdge
				newW = newH * oldWidth / oldHeight
			} else {
				newW = minEdge
				newH = newW * oldHeight / oldWidth
			}
		}
		if (newW > 0 && newH > 0) {
			return saveToFile(newW, newH, newFile)
		}
		return false
	}
}

@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) {
	val f = File("/Users/entaoyang/Downloads/2.png")
	Thumb(f).maxWidth(100).toFile(File(f.parent, "2_w100.png"))
	Thumb(f).maxHeight(100).toFile(File(f.parent, "2_h100.png"))
	Thumb(f).maxEdge(100).toFile(File(f.parent, "2_e100.png"))
}
