package dev.entao.web.core.slices

import dev.entao.web.base.Label
import dev.entao.web.core.AppSlice
import dev.entao.web.core.BaseApp
import dev.entao.web.core.HttpAction
import dev.entao.web.core.HttpContext
import dev.entao.web.sql.AutoInc
import dev.entao.web.sql.ConnPick
import dev.entao.web.sql.Index
import dev.entao.web.sql.OrmModel
import dev.entao.web.sql.OrmModelClass
import dev.entao.web.sql.PrimaryKey
import dev.entao.web.sql.query
import java.sql.Timestamp
import kotlin.collections.set
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

//授权管理

//需要授权
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Auth


//授权检查
class AuthSlice(app: BaseApp) : AppSlice(app) {
    private var perms: List<AuthPerm> = emptyList()
    private var userGroups: List<AuthUserGroup> = emptyList()
    private var groupMap: Map<Int, AuthGroup> = emptyMap()

    private var groupTime: Long = 0L
    private var userGroupTime: Long = 0L
    private var permTime: Long = 0L

    override fun beforeRouter(context: HttpContext, cls: KClass<*>, action: HttpAction) {
        val accept = cls.hasAnnotation<Auth>() || action.hasAnnotation<Auth>()
        if (!accept) return

        val userId = context.accountID
        if (userId == 0L) return context.sendError(401)
        if (userId <= 0) return context.sendError(401)
        val uri = context.currentUri
        prepare()
        val permList = perms.filter { it.uri == uri }
        val p1 = permList.firstOrNull {
            it.userId == userId.toInt()
        }
        if (p1 != null) {
            if (p1.sql.isEmpty()) {
                return
            } else {
                if (evalSQL(context, p1.sql)) return
            }
        }

        val gm = groupsOf(userId.toInt())
        val gKeys = gm.keys
        permList.forEach { p ->
            if (p.groupId in gKeys) {
                if (p.sql.isEmpty()) {
                    return
                } else {
                    if (evalSQL(context, p.sql)) return
                }
            }
        }
        context.sendError(403)
    }

    @Synchronized
    fun prepare() {
        val gt: Long = AuthGroup.maxBy(AuthGroup::modifyTime)?.modifyTime?.time ?: 0L
        if (gt > groupTime) {
            val gs = AuthGroup.list { orderBy(AuthGroup::groupId.ASC) }
            val map = HashMap<Int, AuthGroup>(128)
            for (g in gs) {
                map[g.groupId] = g
            }
            groupMap = map
        }
        val ugt: Long = AuthUserGroup.maxBy(AuthUserGroup::modifyTime)?.modifyTime?.time ?: 0L
        if (ugt > userGroupTime) {
            userGroups = AuthUserGroup.list { orderBy(AuthUserGroup::userId.ASC) }
        }
        val pt: Long = AuthPerm.maxBy(AuthPerm::modifyTime)?.modifyTime?.time ?: 0L
        if (pt > permTime) {
            perms = AuthPerm.list { orderBy(AuthPerm::permId.ASC) }
        }
    }

    fun groupsOf(userId: Int): HashMap<Int, AuthGroup> {
        val ls = userGroups.filter { it.userId == userId }
        val map = HashMap<Int, AuthGroup>()
        for (ug in ls) {
            var g: AuthGroup? = groupMap[ug.groupId]
            while (g != null && g.groupId !in map.keys) {
                map[g.groupId] = g
                g = groupMap[g.parentGroupId]
            }
        }
        return map
    }

    @Suppress("UNUSED_PARAMETER")
    fun evalSQL(context: HttpContext, sql: String): Boolean {
        val argList: ArrayList<Any?> = arrayListOf()
        val sb = StringBuilder()
        val params = StringBuilder()
        var isParam = false
        for (ch in sql) {
            if (ch == '$') {
                isParam = true
                continue
            }
            if (ch == ' ') {
                if (isParam) {
                    params.append(' ')
                    isParam = false
                    sb.append(" ? ")
                    continue
                }
            }
            if (isParam) {
                params.append(ch)
            } else {
                sb.append(ch)
            }
        }

        ConnPick.connection.query(sql, argList)
        return false
    }


}

//用户或组, 对资源是否有访问权限
@Label("授权")
class AuthPerm : OrmModel() {

    @AutoInc
    @PrimaryKey
    var permId: Int by model

    var userId: Int by model

    var groupId: Int by model

    //	var resId: Int by model
    var uri: String by model
    var sql: String by model

    //修改时间戳
    @Index
    var modifyTime: Timestamp by model

    companion object : OrmModelClass<AuthPerm>()
}

////资源, uri
//class AuthResource : Model() {
//
//	@AutoInc
//	@PrimaryKey
//	var resId: Int by model
//
//	var uri: String by model
//
//
//	companion object : ModelClass<AuthResource>()
//}

@Label("组信息")
class AuthGroup : OrmModel() {
    @AutoInc
    @PrimaryKey
    var groupId: Int by model


    var groupName: String by model

    //上级权限组, 继承上级的权限
    var parentGroupId: Int by model

    //修改时间戳
    @Index
    var modifyTime: Timestamp by model

    companion object : OrmModelClass<AuthGroup>()
}

//用户和组关联表
@Label("用户组")
class AuthUserGroup : OrmModel() {
    @PrimaryKey
    var groupId: Int by model

    @PrimaryKey
    var userId: Int by model

    //修改时间戳
    @Index
    var modifyTime: Timestamp by model

    companion object : OrmModelClass<AuthUserGroup>()
}