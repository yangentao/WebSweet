package dev.entao.web.base

import dev.entao.web.log.logd
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.absoluteValue


fun main() {
	val f = File("/Users/yangentao/Downloads/mao.jpg")
	val outFile = File(f.parent, "mm.jpg")
	imageScale(f, outFile, 150, 100)
}

fun imageScale(file: File, scaledFile: File, width: Int = 360, height: Int = 0): Boolean {
	val bi = ImageIO.read(file)
	if (bi.width == 0 || bi.height == 0) {
		return false
	}
	val extName = if (bi.type in setOf(2, 3, 6, 7)) "png" else "jpg"

	if (height <= 0 || height == width * bi.height / bi.width) {
		if (bi.width <= width) {
			file.copyTo(scaledFile, true)
			return true
		}
		val newheight = bi.height * width / bi.width
		val newImage = BufferedImage(width, newheight, bi.type)
		newImage.graphics.drawImage(bi, 0, 0, width, newheight, null)
		ImageIO.write(newImage, extName, scaledFile)
		return true
	}
	var newWidth = width
	var newHeight = height
	if (bi.width < newWidth) {
		newWidth = bi.width
		newHeight = newWidth * bi.height / height
	}
	if (bi.height < newHeight) {
		newHeight = bi.height
		newWidth = newHeight * bi.width / bi.height
	}
	val x1: Int
	val y1: Int
	val x2: Int
	val y2: Int

	if ((newWidth - bi.width).absoluteValue > (newHeight - bi.height).absoluteValue) {
		val h = bi.height
		val w = h * newWidth / newHeight
		val edgeW = (bi.width - w) / 2
		y1 = 0
		y2 = h
		x1 = edgeW
		x2 = bi.width - edgeW
	} else {
		val w = bi.width
		val h = w * newHeight / newWidth
		val edgeH = (bi.height - h) / 2
		x1 = 0
		x2 = w
		y1 = edgeH
		y2 = bi.height - edgeH
	}
	val newImage = BufferedImage(newWidth, newHeight, bi.type)
	newImage.graphics.drawImage(bi, 0, 0, newWidth, newHeight, x1, y1, x2, y2, null)
	ImageIO.write(newImage, extName, scaledFile)
	return true

}

@Suppress("LocalVariableName")
fun imageCode4(code: String, backFile: File, outFile: File) {
	val IMG_W = 150
	val IMG_H = 60
	val FONT_SIZE = IMG_W * 28 / 100
	val backImg = ImageIO.read(backFile)
	val img = BufferedImage(IMG_W, IMG_H, BufferedImage.TYPE_USHORT_565_RGB)
	val g = img.graphics
	g.apply {
		if (backImg.width * img.height > backImg.height * img.width) {//更宽
			val y = 0
			val h = backImg.height
			val w = backImg.height * img.width / img.height
			val x = (backImg.width - w) / 2
			drawImage(backImg, 0, 0, img.width, img.height, x, y, x + w, y + h, null)
		} else {
			val x = 0
			val w = backImg.width
			val h = backImg.width * img.height / img.width
			val y = (backImg.height - h) / 2
			drawImage(backImg, 0, 0, img.width, img.height, x, y, x + w, y + h, null)
		}
	}

	g.apply {
		val f = this.font
		this.font = Font(f.name, Font.BOLD, FONT_SIZE)
		color = Color.BLUE
		val m = this.fontMetrics
		val rect = m.getStringBounds(code, this)
		val x: Int = (img.width - rect.width.toInt()) / 2
		val y: Int = img.height - (img.height - rect.height.toInt()) / 2 - m.descent
		drawString(code, x, y)
		logd(code, x, y, rect.width, rect.height)
	}
	ImageIO.write(img, "jpg", outFile)
}
