@file:Suppress("unused", "UNUSED_PARAMETER")

package dev.entao.web.core

import dev.entao.web.core.render.FileRender
import dev.entao.web.core.render.Render
import dev.entao.web.log.logd
import java.io.File
import java.util.*
import javax.servlet.http.HttpServletRequest


//@/
//@/static
//@/static/cat.png
//@/user/login
class StaticRender(context: HttpContext) : Render(context) {
    val existFile: Boolean get() = this.staticFile != null

    private val staticFile: File? by lazy {
        findFile()
    }

    private fun findFile(): File? {
        return findFile(context.request)
    }

    override fun onSend() {
        val f = staticFile ?: return context.sendError(404)
        logd("send file: ", f.absolutePath)
        FileRender(context, f).send()
    }


    companion object {
        private val permList: ArrayList<StaticPermission> = ArrayList()
        private val fileMap: HashMap<String, File> = HashMap()
        private val dirMap: TreeMap<String, DirPublishConfig> = TreeMap()

        init {
            permissionAdd {
                val p = it.absolutePath
                "/META-INF" !in p && "/WEB-INF" !in p && "/." !in p //&& "/.." !in p
            }
        }

        private val HttpServletRequest.appPath: String
            get() {
                if (this.contextPath.isEmpty()) return this.requestURI.lowercase()
                return this.requestURI.lowercase().substringAfter(this.contextPath.lowercase(), "")
            }


        fun findFile(request: HttpServletRequest): File? {
            return findFile("@" + request.appPath)
        }

        private fun findFile(reqPath: String): File? {
            val file: File? = fileMap[reqPath]
            if (file != null) {
//                logd("found: ", reqPath, "  => ", file.absolutePath)
                return file
            }
            val keyList = dirMap.keys.sortedBy { it.length }.reversed()
            val keyFound: String = keyList.firstOrNull { it == reqPath } ?: keyList.firstOrNull { reqPath.startsWith(it.withSep) } ?: return null
            val dirInfo = dirMap[keyFound] ?: return null
            val subpath = reqPath.substringAfter(keyFound).trimStart('/')
            //TODO 处理大小写,  用listFile
            val subfile = File(dirInfo.dir, subpath)
            if (!subfile.exists() || !subfile.isFile) return null
            if (!allow(subfile) || !dirInfo.allow(subfile)) return null
//            logd("found: ", reqPath, "  => ", subfile.absolutePath)
            return subfile
        }

        fun publishFile(webpath: String, file: File) {
            if (!file.isFile) error("publish file with NOT a file")
            fileMap[webpath.lowercase()] = file
        }

        fun publishDir(webpath: String, dir: File, block: DirPublishConfig.() -> Unit) {
            if (!dir.isDirectory) error("publish directory with NOT a directory")
            val info = DirPublishConfig(dir)
            dirMap[webpath.lowercase().withSep] = info
            info.block()
        }

        fun permissionAdd(perm: StaticPermission) {
            permList.add(perm)
        }

        fun allow(file: File): Boolean {
            for (p in permList) {
                if (!p.allow(file)) {
                    return false
                }
            }
            return true
        }
    }
}

fun interface StaticPermission {
    fun allow(file: File): Boolean
}

class DirPublishConfig(val dir: File) {
    var allowListFile: Boolean = false
    private var perm: StaticPermission = StaticPermission { true }

    fun allow(file: File): Boolean {
        return perm.allow(file)
    }

    fun permission(perm: StaticPermission) {
        this.perm = perm
    }
}

//TODO regex
class ResourcePublishConfig(val resource: String) {
    var allowListFile: Boolean = false
    private var perm: StaticPermission = StaticPermission { true }

    fun allow(file: File): Boolean {
        return perm.allow(file)
    }

    fun permission(perm: StaticPermission) {
        this.perm = perm
    }
}
