package dev.entao.hare

import dev.entao.web.core.Action
import dev.entao.web.core.HttpContext
import dev.entao.web.core.HttpController
import dev.entao.web.core.render.sendResult
import dev.entao.web.log.logd

@dev.entao.web.core.Controller(index = true)
class IndexPage(context: HttpContext) : HttpController(context) {


    @Action(index = true)
    fun index() {
        context.sendText("Hello")
    }


    @Action
    fun ios() {
        val json = context.request.reader.readText()
        logd("receive: ", json)
        sendResult {
            success()
            data(200)
        }
    }

    @Action
    fun apple() {
        val s = """{
                    "messagefilter": {
                        "apps": [
                            "HPA69P8D2K.dev.entao.SmsFilter",
                            "HPA69P8D2K.dev.entao.SmsFilter.SmsExt"
                        ]
                    },
                     "webcredentials": {
                        "apps": [
                            "HPA69P8D2K.dev.entao.SmsFilter",
                            "HPA69P8D2K.dev.entao.SmsFilter.SmsExt"
                        ]
                    }
                }"""
        context.sendJSON(s.trimIndent())
    }


}