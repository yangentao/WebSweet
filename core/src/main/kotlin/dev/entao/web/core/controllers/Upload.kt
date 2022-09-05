package dev.entao.web.core.controllers

import dev.entao.web.base.DateX
import dev.entao.web.base.Name
import dev.entao.web.base.Now
import dev.entao.web.base.joinPath
import dev.entao.web.base.tempFileName
import dev.entao.web.core.HttpContext
import dev.entao.web.core.OnHttpContext
import dev.entao.web.core.slices.accountID
import dev.entao.web.sql.AutoInc
import dev.entao.web.sql.Index
import dev.entao.web.sql.OrmModel
import dev.entao.web.sql.OrmModelClass
import dev.entao.web.sql.PrimaryKey
import java.io.File
import java.sql.Timestamp
import javax.servlet.http.Part

/**
 * Created by entaoyang@163.com on 2017/4/5.
 */

@Name("upload")
class Upload : OrmModel() {

    @PrimaryKey
    @AutoInc
    var id: Int by model
    var localFileName: String by model
    var extName: String by model
    var basedir: String by model
    var yeardir: String by model
    var monthdir: String by model

    var rawname: String by model
    var size: Int by model
    var contentType: String by model

    @Index
    var userId: Int by model

    @Index
    var accountId: Int by model

    @Index
    var uploadTime: Timestamp by model

    var platform: String by model

    fun localFile(baseDir: String? = null): File {
        val localPath = joinPath(baseDir ?: this.basedir, this.yeardir, this.monthdir, this.localFileName)
        return File(localPath)
    }

    fun localDir(baseDir: String? = null): File {
        val localPath = joinPath(baseDir ?: this.basedir, this.yeardir, this.monthdir)
        return File(localPath)
    }

    companion object : OrmModelClass<Upload>() {
        const val PLATFORM = "platform"

        fun deleteRes(context: HttpContext, id: Int) {
            val item = Upload.oneByKey(id) ?: return
            val file = item.localFile(context.app.uploadDir.absolutePath)
            file.delete()
            item.deleteByKey()
        }

        fun fromContext(context: HttpContext, part: Part): Upload {
            val ext = part.submittedFileName.substringAfterLast('.', "")
            val m = Upload()
            m.localFileName = tempFileName(ext)

            m.extName = ext
            m.basedir = context.app.uploadDir.absolutePath
            val date = DateX()
            m.yeardir = date.format("yyyy")
            m.monthdir = date.format("MM")
            m.contentType = part.contentType
            m.rawname = part.submittedFileName
            m.size = part.size.toInt()
            m.accountId = context.accountID.toInt()
            m.userId = context.accountID.toInt()
            m.uploadTime = Now.timestamp
            m.platform = context[PLATFORM] ?: context["os"] ?: ""
            return m
        }

        fun fromFile(context: HttpContext, file: File, extName: String, contentType: String): Upload {
            val ext = extName.trim('.')
            val m = Upload()
            m.localFileName = tempFileName(ext)

            m.extName = ext
            m.basedir = context.app.uploadDir.absolutePath
            val date = DateX()
            m.yeardir = date.format("yyyy")
            m.monthdir = date.format("MM")
            m.contentType = contentType
            m.rawname = file.name
            m.size = file.length().toInt()
            m.accountId = context.accountID.toInt()
            m.userId = context.accountID.toInt()
            m.uploadTime = Now.timestamp
            m.platform = context[PLATFORM] ?: context["os"] ?: ""

            val dir = m.localDir(context.app.uploadDir.absolutePath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            file.renameTo(File(dir, m.localFileName))
            m.insert()
            return m
        }
    }
}

context (OnHttpContext)
fun Upload.localFile(): File {
    return this.localFile(context.app.uploadDir.absolutePath)
}
