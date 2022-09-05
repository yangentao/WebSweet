package dev.entao.web.core

import dev.entao.web.base.lowerCased
import dev.entao.web.base.ownerClass
import dev.entao.web.log.fatal
import dev.entao.web.log.logd
import java.util.*
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.KClass


class HttpActionManager(private val contextPath: String) {
    val allControllers = ArrayList<KClass<out HttpController>>()
    val routeMap = HashMap<String, Router>(64)
    private val actionMap = HashMap<HttpAction, String>(64)

    private val groupMap = TreeMap<String, KClass<out HttpController>>()

    fun onDestory() {
        routeMap.clear()
        allControllers.clear()
        actionMap.clear()
    }

    fun uriOf(action: HttpAction): String? {
        val cls = action.ownerClass
        if (cls == null) {
            return actionMap[action]
        } else {
            val f = cls.actionList.firstOrNull { it.name == action.name } ?: return null
            return actionMap[f]
        }
    }

    fun findRouter(request: HttpServletRequest): Router? {
        return routeMap[request.requestURI.trimEnd('/').lowercase()]
    }

    fun findRouter(context: HttpContext): Router? {
        return routeMap[context.currentUri]
    }

    fun findController(uri: String): Pair<String, KClass<out HttpController>>? {
//        groupMap.floorEntry(uri)
        val set = HashSet<String>()
        for (s in groupMap.keys) {
            if (uri == s) {
                return s to groupMap[s]!!
            }
            if (uri.startsWith("$s/")) {
                set.add(s)
            }
        }
        val k: String = set.maxByOrNull { it.length } ?: return null
        return k to groupMap[k]!!
    }

    fun addController(vararg clses: KClass<out HttpController>) {
        addController("", *clses)
    }

    //subpath可以是空字符串
    fun addController(subpath: String, vararg clses: KClass<out HttpController>) {
        for (cls in clses) {
            if (cls in allControllers) {
                continue
            }
            allControllers.add(cls)
            groupMap[makeUri(subpath, cls.pageName)] = cls
            val ls = cls.actionList
            for (f in ls) {
                val uri = makeUri(subpath, cls.pageName, f.actionName)
                addRouter(Router(uri, cls, f))
            }
        }
    }

    private fun addRouter(router: Router) {
        val u = router.uri.lowerCased
        val old = routeMap.put(u, router)
        if (old != null) {
            fatal("已经存在对应的Route: ${old.function}  $u,  $old")
        }
        actionMap[router.function] = router.uri
        logd("Add: ", router.functionFullName)
    }


    private fun makeUri(vararg ps: String): String {
        return buildPath(contextPath, *ps)
    }

}
