package dev.entao.web.page

import dev.entao.web.core.HttpContext
import dev.entao.web.core.uri
import dev.entao.web.tag.HtmlPage
import dev.entao.web.tag.tag.linkCSS
import dev.entao.web.tag.tag.meta
import dev.entao.web.tag.tag.script

open class BootPage(context: HttpContext) : HtmlPage(context) {
    init {
        html.lang = "zh_CN"
        head {
            meta {
                charset = "UTF-8"
            }
            meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1"

            }
//            linkCSS("@/res/boot/bootstrap.css".uri)
//            linkCSS("https://cdn.jsdelivr.net/npm/bootstrap@5.2.0/dist/css/bootstrap.min.css".cached)
            linkCSS("page/bootstrap52.css".uri)
            linkCSS("assets/dashboard.css".uri)
            linkCSS("assets/client.css".uri)
        }
        body {
            script("page/bootstrap52.js".uri)
            script("assets/jquery36.js".uri)
            script("assets/basic.js".uri)
            script("assets/client.js".uri)
        }
    }

}