package dev.entao.web.core

import dev.entao.web.base.resourceText
import dev.entao.web.json.YsonObject

class SweetConfig(val yo: YsonObject) {
    var appClass: String? by yo

    companion object {
        const val APP_JSON = "app.json"

        fun load(): SweetConfig {
            val s = this::class.resourceText(APP_JSON) ?: return SweetConfig(YsonObject())
            return SweetConfig(YsonObject(s))
        }
    }
}