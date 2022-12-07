package dev.entao.hare

import dev.entao.web.bootpage.BootPage
import dev.entao.web.core.HttpContext
import dev.entao.web.tag.tag.h1

class DashPage(context: HttpContext) : BootPage(context) {

    init {

        body {
            h1 {
                +"Hello Bootstrap!"
            }
        }
    }
}