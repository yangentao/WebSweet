@file:Suppress("PropertyName")

package dev.entao.web.core.slices

import dev.entao.web.core.AppSlice
import dev.entao.web.core.BaseApp
import dev.entao.web.core.HttpContext
import dev.entao.web.core.clientIp
import dev.entao.web.core.header
import dev.entao.web.sql.AutoInc
import dev.entao.web.sql.OrmModel
import dev.entao.web.sql.OrmModelClass
import dev.entao.web.sql.PrimaryKey

class IPRecordSlice(app: BaseApp) : AppSlice(app) {

    override fun beforeRequest(context: HttpContext) {
        val req = context.request
        val m = IPRecord()
        m.uri = req.requestURI
        m.queryString = req.queryString
        m.clientIP = req.clientIp
        m.remoteAddr = req.remoteAddr
        m.x_forwarded_for = req.header("X-Forwarded-For")
        m.x_real_ip = req.header("X-Real-IP")

        m.insert()
    }
}

class IPRecord : OrmModel() {

    @PrimaryKey
    @AutoInc
    var id: Int by model

    var uri: String by model

    var queryString: String? by model

    var clientIP: String? by model

    var remoteAddr: String? by model

    var x_forwarded_for: String? by model
    var x_real_ip: String? by model

    companion object : OrmModelClass<IPRecord>() {

    }
}