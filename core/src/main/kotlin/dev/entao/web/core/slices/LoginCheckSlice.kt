@file:Suppress("PropertyName", "unused", "MemberVisibilityCanBePrivate", "UNUSED_PARAMETER")

package dev.entao.web.core.slices

import dev.entao.web.base.DAY
import dev.entao.web.base.Encrypt
import dev.entao.web.base.MaxLength
import dev.entao.web.core.AppSlice
import dev.entao.web.core.BaseApp
import dev.entao.web.core.ExtLongValue
import dev.entao.web.core.ExtStringValue
import dev.entao.web.core.HttpAction
import dev.entao.web.core.HttpContext
import dev.entao.web.core.header
import dev.entao.web.json.YsonObject
import dev.entao.web.sql.EQ
import dev.entao.web.sql.Index
import dev.entao.web.sql.OrmModel
import dev.entao.web.sql.OrmModelClass
import dev.entao.web.sql.PrimaryKey
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


//检查是否登录
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginNeed(val type: String = "*")


var HttpContext.accountID: Long by ExtLongValue()
var HttpContext.accountType: String by ExtStringValue()

class LoginCheckSlice(app: BaseApp) : AppSlice(app) {

    private fun checkToken(loginAnno: LoginNeed, context: HttpContext, cls: KClass<*>, action: HttpAction): Boolean {
        val token = context.tokenText ?: return false
        val j = JWT(pwd, token)
        if (!j.OK) return false
        val info = TokenInfo(YsonObject(j.body))
        if (info.expired) return false
        if (loginAnno.type != info.type) return false
        TokenTable.one(TokenTable::token EQ token) ?: return false
        context.accountID = info.id
        context.accountType = info.type
        return true
    }

    private fun checkSession(loginAnno: LoginNeed, context: HttpContext, cls: KClass<*>, action: HttpAction): Boolean {
        val id: Long = context.getSession(ACC_ID)?.toLongOrNull() ?: return false
        val type = context.getSession(ACC_TYP) ?: return false
        if (type != loginAnno.type) {
            context.removeSession(ACC_ID)
            context.removeSession(ACC_TYP)
            return false
        }
        context.accountID = id
        context.accountType = type
        return true
    }

    override fun beforeRouter(context: HttpContext, cls: KClass<*>, action: HttpAction) {
        val tokenAnno: LoginNeed = action.findAnnotation() ?: cls.findAnnotation() ?: return
        if (checkSession(tokenAnno, context, cls, action)) return
        if (checkToken(tokenAnno, context, cls, action)) return
        context.app.onAuthFailed(context, cls, action)
        context.commitNeed()
    }


    companion object {
        private const val ACC_TYP = "_ACCOUNT_TYPE_"
        private const val ACC_ID = "_ACCOUNT_ID_"

        fun putSessionAccountID(context: HttpContext, accountID: Long, type: String) {
            context.putSession(ACC_ID, accountID.toString())
            context.putSession(ACC_TYP, type)
        }

        private const val header: String = """{"alg":"HS256","typ":"JWT"}"""
        var pwd: String = "1234567890"
        var expireDays: Int = 30

        fun removeToken(id: Int, type: String) {
            TokenTable.delete(TokenTable::id EQ id, TokenTable::type EQ type)
        }

        fun removeToken(token: String) {
            TokenTable.delete(TokenTable::token EQ token)
        }


        fun makeToken(ident: Long, type: String, expireTime: Long = System.currentTimeMillis() + expireDays.DAY, platform: String = "app"): String {
            val m = TokenInfo.from(ident, type, expireTime, platform)
            val token = JWT.make(pwd, m.toString(), header)
            val ut = TokenTable()
            ut.id = ident
            ut.type = type
            ut.expireTime = expireTime
            ut.platform = platform
            ut.token = token
            ut.saveByKey()
            return token
        }
    }

}

val HttpContext.tokenText: String?
    get() {
        request.header("Authorization")?.substringAfter("Bearer ", "")?.trim()?.also {
            if (it.isNotEmpty()) return it
        }
        val tk = this["access_token"] ?: this["token"] ?: return null
        return tk.ifEmpty {
            null
        }
    }

class TokenTable : OrmModel() {

    @PrimaryKey
    var id: Long by model

    @PrimaryKey
    var type: String by model

    @Index
    var expireTime: Long by model

    @Index
    var platform: String by model

    @Index
    @MaxLength(1024)
    var token: String by model

    companion object : OrmModelClass<TokenTable>()
}


class TokenInfo(private val yo: YsonObject = YsonObject()) {
    var id: Long by yo
        private set
    var type: String by yo
        private set
    var expireTime: Long by yo
        private set
    var platform: String by yo
        private set

    val expired: Boolean
        get() {
            if (expireTime != 0L) {
                return System.currentTimeMillis() >= expireTime
            }
            return false
        }

    override fun toString(): String {
        return yo.toString()
    }

    companion object {
        fun from(ident: Long, type: String, expireTime: Long, platform: String): TokenInfo {
            val a = TokenInfo()
            a.id = ident
            a.type = type
            a.expireTime = expireTime
            a.platform = platform
            return a
        }
    }

}


private class JWT(pwd: String, token: String) {
    var header: String = ""
    var body: String = ""
    var sign: String = ""
    var OK: Boolean = false

    init {
        val ls = token.split('.')
        if (ls.size == 3) {
            val h = ls[0]
            val d = ls[1]
            val g = ls[2]
            val m = Encrypt.hmacSha256("$h.$d", pwd)
            if (m == g) {
                header = Encrypt.B64.decode(h)
                body = Encrypt.B64.decode(d)
                sign = g
                OK = true
            }
        }
    }

    companion object {

        fun make(pwd: String, data: String, header: String = """{"alg":"HS256","typ":"JWT"}"""): String {
            val a = Encrypt.B64.encode(header)
            val b = Encrypt.B64.encode(data)
            val m = Encrypt.hmacSha256("$a.$b", pwd)
            return "$a.$b.$m"
        }
    }
}
