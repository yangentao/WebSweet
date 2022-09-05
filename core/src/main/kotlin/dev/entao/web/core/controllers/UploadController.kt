package dev.entao.web.core.controllers

import dev.entao.web.base.Label
import dev.entao.web.base.Mimes
import dev.entao.web.core.HttpContext
import dev.entao.web.core.HttpController
import dev.entao.web.core.OnHttpContext
import dev.entao.web.core.isFilePart
import dev.entao.web.core.render.sendFile
import dev.entao.web.core.render.sendResult
import dev.entao.web.core.url
import dev.entao.web.core.writeTo
import dev.entao.web.log.logd
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.servlet.http.Part
import kotlin.math.absoluteValue

context (OnHttpContext)
fun downloadUrl(resId: Int): String {
    return context.uriActionValues(UploadController::download, resId).url
}

context (OnHttpContext)
fun imageUrl(imgId: Int): String {
    return context.uriActionValues(UploadController::download, imgId).url
}

context (OnHttpContext)
fun scaleImageUrl(imgId: Int, width: Int, height: Int = 0): String {
    return context.uriActionValues(UploadController::scale, imgId, width, height).url
}

class UploadController(context: HttpContext) : HttpController(context) {

    @Suppress("UNUSED_PARAMETER")
    @dev.entao.web.core.Action
    fun typeimg(type: String) {
        sendFile(File(context.httpService.appDir, "assets/file_miss.png")) {}
    }

    //上传一个文件
    @dev.entao.web.core.Action
    @dev.entao.web.core.HttpMethod("POST")
    fun upload() {
        val part: Part = context.partList.firstOrNull { it.isFilePart } ?: return context.sendError(400, "NO file found")
        val m = Upload.fromContext(context, part)
        val dir = m.localDir(context.app.uploadDir.absolutePath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, m.localFileName)
        try {
            part.writeTo(file)
        } catch (ex: Exception) {
            file.delete()
            context.sendResult {
                failed("写文件失败")
            }
            return
        } finally {
            part.delete()
        }

        if (m.insert()) {
            context.sendResult {
                success()
                data {
                    "id" TO m.id
                    "url" TO ::download.url
                }
            }
        } else {
            context.sendResult {
                failed("保存失败")
            }
        }
    }

    @Label("下载")
    @dev.entao.web.core.Action
    fun download(id: Int) {
        sendFile(id, false)
    }

    @dev.entao.web.core.Action
    fun media(id: Int) {
        sendFile(id, true)
    }

    //TODO crop = false
    @dev.entao.web.core.Action
    fun scale(id: Int, width: Int = 360, height: Int = 0) {
        val item = Upload.oneByKey(id) ?: return context.sendError(404, "无效的标识")
        val file = item.localFile(context.app.uploadDir.absolutePath)
        if (!file.exists()) {
            context.sendError(404, "无效的标识")
            return
        }
        if (width == 0) {
            media(id)
            return
        }

        val scaledFile = File(context.app.tempDir, "${item.id}@$width@$height")
        if (!scaledFile.exists()) {
            val bi = ImageIO.read(file)
            if (bi.width == 0 || bi.height == 0) {
                context.sendError(404, "无效文件")
                return
            }
            if (height <= 0 || height == width * bi.height / bi.width) {
                if (bi.width <= width) {
                    sendFile(file) {
                        filename = item.rawname
                        contentType = Mimes.ofFile(item.rawname)
                    }
                    return
                }
                val newheight = bi.height * width / bi.width
                val newImage = BufferedImage(width, newheight, bi.type)
                newImage.graphics.drawImage(bi, 0, 0, width, newheight, null)
                ImageIO.write(newImage, item.extName, scaledFile)
            } else {
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
                logd(newWidth, newHeight)
                logd(x1, y1, x2, y2)
                val newImage = BufferedImage(newWidth, newHeight, bi.type)
                newImage.graphics.drawImage(bi, 0, 0, newWidth, newHeight, x1, y1, x2, y2, null)
                ImageIO.write(newImage, item.extName, scaledFile)
            }
        }

        if (scaledFile.exists()) {

            sendFile(scaledFile) {
                filename = item.rawname
                contentType = Mimes.ofFile(item.rawname)
            }
        } else {
            media(id)
//			context.sendError(404, "无效文件")
        }
    }

    private fun sendFile(id: Int, isMedia: Boolean) {
        val item = Upload.oneByKey(id) ?: return context.sendError(404, "无效的标识")
        sendItem(item, !isMedia)
    }

    private fun sendItem(item: Upload, attach: Boolean = false) {
        val file = item.localFile(context.app.uploadDir.absolutePath)
        if (!file.exists()) {
            context.sendError(404, "无效的标识")
            return
        }
        sendFile(file) {
            isAttach = attach
            contentType = Mimes.ofFile(item.rawname)
            filename = item.rawname
        }
    }
}