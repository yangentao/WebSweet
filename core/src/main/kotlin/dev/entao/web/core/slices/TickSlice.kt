package dev.entao.web.core.slices

import dev.entao.web.base.userName
import dev.entao.web.core.AppSlice
import dev.entao.web.core.BaseApp
import dev.entao.web.core.ExtValue
import dev.entao.web.core.HttpAction
import dev.entao.web.core.HttpContext
import dev.entao.web.core.actionName
import dev.entao.web.core.pageName
import dev.entao.web.log.logd
import kotlin.reflect.KClass


class TickSlice(app: BaseApp) : AppSlice(app) {

    override fun beforeRouter(context: HttpContext, cls: KClass<*>, action: HttpAction) {
        context.tickValue = System.currentTimeMillis()
    }

    override fun afterRouter(context: HttpContext, cls: KClass<*>, action: HttpAction) {
        val a: Long = context.tickValue ?: return
        val delta = System.currentTimeMillis() - a
        logd("请求${cls.pageName}.${action.userName}用时: ${delta.formatedSeconds} 秒")
    }
}

private var HttpContext.tickValue: Long? by ExtValue()

private val Long.formatedSeconds: String
    get() {
        return String.format("%d.%03d", this / 1000, this % 1000)
    }
