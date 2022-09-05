package dev.entao.web.core

import kotlin.reflect.KClass


abstract class AppSlice(val app: BaseApp) {

    open fun beforeRequest(context: HttpContext) {}
    open fun beforeRouter(context: HttpContext, cls: KClass<*>, action: HttpAction) {}
    open fun afterRouter(context: HttpContext, cls: KClass<*>, action: HttpAction) {}
    open fun afterRequest(context: HttpContext) {}
    open fun onDestory() {}
}

interface HttpSlice {

    fun onInit(httpService: HttpService) {}
    fun beforeRequest(context: HttpContext) {}
    fun beforeRouter(context: HttpContext, cls: KClass<*>, action: HttpAction) {}
    fun afterRouter(context: HttpContext, cls: KClass<*>, action: HttpAction) {}
    fun afterRequest(context: HttpContext) {}
    fun onDestory(httpService: HttpService) {}
}
