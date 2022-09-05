package dev.entao.web.core

import dev.entao.web.base.base64Encoded
import dev.entao.web.base.ensureDirs
import dev.entao.web.base.toArrayList
import dev.entao.web.core.slices.LoginCheckSlice
import dev.entao.web.log.ConsoleLogPrinter
import dev.entao.web.log.DirLogPrinter
import dev.entao.web.log.LogTree
import dev.entao.web.log.LogX
import dev.entao.web.log.logd
import java.io.File
import javax.servlet.ServletContext
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

//config -> create ----> [service loop] --> destroy
abstract class BaseApp(val httpService: HttpService) {
    abstract val appName: String
    var sessionTimeoutSeconds: Int = 3600
    var uploadDir: File = File(File(httpService.appDir.parentFile, httpService.contextPath.trim('/') + "_work"), "uploads").ensureDirs()
    var tempDir: File = (httpService.servletContext.getAttribute(ServletContext.TEMPDIR) as File).ensureDirs()

    val sliceList: ArrayList<AppSlice> = ArrayList()

    fun addSlice(slice: AppSlice) {
        sliceList += slice
    }

    fun addSliceList(vararg slices: AppSlice) {
        sliceList += slices
    }

    open fun onCreate() {
        val d = File(httpService.appDir.parentFile, httpService.contextPath.trim('/') + "_work")
        val p = LogTree(DirLogPrinter(File(d, "xlog"), 15), ConsoleLogPrinter())
        LogX.setPrinter(p)
    }

    open fun onDestroy() {
        sliceList.toArrayList().forEach {
            it.onDestory()
        }
        sliceList.clear()
    }

    open fun beforeRequest(context: HttpContext) {
        logd("Request: ", context.request.method, " ", context.currentUri, "   ", context.request.contentType)
        for ((k, v) in context.paramMap) {
            logd("        Param: ", k, "=", v)
        }
        if (context.request.isMultipart) {
            for (p in context.partList) {
                logd("    Part:", p.contentType, p.name, p.size, p.submittedFileName)
            }
        }
        sliceList.toArrayList().forEach {
            it.beforeRequest(context)
        }
    }

    open fun beforeRouter(context: HttpContext, cls: KClass<*>, action: HttpAction) {
        logd("Router:  ${cls.qualifiedName}.${action.name}")
        sliceList.toArrayList().forEach {
            it.beforeRouter(context, cls, action)
        }
    }

    open fun afterRouter(context: HttpContext, cls: KClass<*>, action: HttpAction) {
        sliceList.toArrayList().forEach {
            it.afterRouter(context, cls, action)
        }
    }

    open fun afterRequest(context: HttpContext) {
        sliceList.toArrayList().forEach {
            it.afterRequest(context)
        }
    }

    open fun onAuthFailed(context: HttpContext, cls: KClass<*>, action: HttpAction) {
        if (context.acceptHtml && context.request.referer != null) {
            context.sendError(401)
        } else {
            gotoLoginPage(context, cls, action)
        }
        context.response.flushBuffer()
    }

    protected open fun loginUrl(context: HttpContext): String {
        return httpService.findRouter { it.function.hasAnnotation<LoginWebAction>() }?.uri ?: ""
    }

    protected open fun gotoLoginPage(context: HttpContext, cls: KClass<*>, action: HttpAction) {
        val loginUri: String = loginUrl(context)
        if (loginUri.isNotEmpty()) {
            var url = context.request.requestURI
            val qs = context.request.queryString ?: ""
            if (qs.isNotEmpty()) {
                url = "$url?$qs"
            }
            url = url.base64Encoded
            context.redirect(loginUri, "$BACK_URL=$url")
        } else {
            context.sendError(401)
        }
    }

    fun setTokenPassword(pwd: String) {
        LoginCheckSlice.pwd = pwd
    }


    companion object {
        const val BACK_URL = "backurl"
    }

}
