package dev.entao.web.core

import dev.entao.web.core.render.DataInputInfo
import dev.entao.web.core.render.sendDataFile
import dev.entao.web.core.render.sendFile
import java.io.File
import java.net.JarURLConnection


typealias ResAcceptor = (String) -> Boolean

class ResController(context: HttpContext) : HttpController(context) {

    @MissAction
    fun onMissAction() {
        val path = context.pathInController
        if (path.isEmpty()) {
            return context.sendError(404)
        }
        val ext = path.substringAfterLast('.', "")
        if (ext.lowercase() !in allowFileExtSet) {
            return context.sendError(404)
        }
        if (!isAccept(path)) {
            return context.sendError(404)
        }

        val url = this::class.java.classLoader.getResource(path) ?: return context.sendError(404)
        if (url.protocol == "file") {
            val file = File(url.toURI())
            return context.sendFile(file) {
                isAttach = false
            }
        }
        if (url.protocol == "jar") {
            val jarURL = url.openConnection() as JarURLConnection
            return context.sendDataFile(DataInputInfo.fromJar(jarURL)) {
                isAttach = false
            }
        }

        return context.sendError(404)
    }

    companion object {
        val allowFileExtSet: HashSet<String> = hashSetOf("jpg", "png", "jpeg", "js", "css", "webp", "html", "htm", "txt", "xml", "json")
        private val acceptorList: ArrayList<ResAcceptor> = ArrayList()
        private fun isAccept(path: String): Boolean {
            for (a in acceptorList) {
                if (a(path)) return true
            }
            return false
        }

        fun accept(block: ResAcceptor) {
            acceptorList += block
        }
    }

}