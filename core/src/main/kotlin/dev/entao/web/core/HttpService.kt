@file:Suppress("MemberVisibilityCanBePrivate")

package dev.entao.web.core

import dev.entao.web.base.createInstanceX
import dev.entao.web.log.LogX
import dev.entao.web.log.logd
import dev.entao.web.log.loge
import dev.entao.web.sql.ConnPick
import java.io.File
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass


class HttpService(filterConfig: FilterConfig) : BaseService(filterConfig) {
    val appDir: File = File(servletContext.getRealPath("/"))
    val routeManager: HttpActionManager = HttpActionManager(servletContext.contextPath)
    lateinit var app: BaseApp

    private fun instanceApp(): BaseApp {
        val configs = SweetConfig.load()
        val appCls = configs.appClass
        try {
            return if (appCls != null && appCls.isNotEmpty()) {
                Class.forName(appCls).kotlin.createInstanceX(this)
            } else {
                Class.forName("dev.entao.web.SweetApp").kotlin.createInstanceX(this)
            }
        } catch (ex: Exception) {
            loge("Instance App Failed!!!")
            ex.printStackTrace()
            throw ex
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = instanceApp()
        app.onCreate()
        StaticRender.publishDir("@/", appDir) {
            this.permission {
                "hole" !in it.absolutePath
            }
        }
    }


    override fun allowMethods(request: HttpServletRequest): Set<String> {
        return routeManager.findRouter(request)?.allowMethods ?: emptySet()
    }

    override fun getLastModified(req: HttpServletRequest): Long {
        StaticRender.findFile(req)?.also { return it.lastModified() }
        return super.getLastModified(req)
    }

    private fun doHttpAction(context: HttpContext) {
        routeManager.findRouter(context)?.also { router ->
            if (context.request.method.uppercase() !in router.allowMethods) {
                context.response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
                return
            }

            app.beforeRouter(context, router.cls, router.function)
            try {
                if (!context.isCommited) {
                    router.dispatch(context)
                }
            } finally {
                app.afterRouter(context, router.cls, router.function)
            }
            return
        }
        routeManager.findController(context.currentUri)?.also { pair ->
            val cls = pair.second
            cls.missFunction?.also { missFun ->
                logd("MissAction: ", cls.qualifiedName, missFun.name)
                app.beforeRouter(context, cls, missFun)
                try {
                    if (!context.isCommited) {
                        val inst = cls.createInstanceX<Any>(context)
                        val map = Router.prepareParams(context, inst, missFun)
                        missFun.callBy(map)
                    }
                } finally {
                    app.afterRouter(context, cls, missFun)
                }
                return
            }
        }

        val sr = StaticRender(context)
        if (sr.existFile) {
            sr.send()
            return
        }

        context.nextChain()
    }

    override fun doOptions(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        allowCross(request, response)
        super.doOptions(request, response, chain)
    }

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        doHttpFilter(request, response, chain)
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        doHttpFilter(request, response, chain)
    }

    override fun doPut(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        doHttpFilter(request, response, chain)
    }

    override fun doDelete(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        doHttpFilter(request, response, chain)
    }

    private fun doHttpFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val context = HttpContext(this, request, response, chain)
        try {
            app.beforeRequest(context)
            if (response.isCommitted) return
            doHttpAction(context)
        } finally {
            app.afterRequest(context)
            context.clean()
        }
    }

    override fun onDestroy() {
        app.onDestroy()
        routeManager.onDestory()
        LogX.flush()
        LogX.clearPrinter()
        ConnPick.clean()
        println("HttpService.onDestroy()")
    }

    fun uriOf(action: HttpAction): String? {
        return routeManager.uriOf(action)
    }

    fun findRouter(block: (Router) -> Boolean): Router? {
        return routeManager.routeMap.values.firstOrNull(block)
    }

    fun addController(vararg clses: KClass<out HttpController>) {
        addController("", *clses)
    }

    fun addController(subpath: String, vararg clses: KClass<out HttpController>) {
        this.routeManager.addController(subpath, *clses)
    }


    companion object {
        val controllerSuffixs: HashSet<String> = hashSetOf("Controller", "Pages", "Page", "Apis", "Api", "Group", "PageGroup")

    }
}