package dev.entao.hare

import dev.entao.web.core.Action
import dev.entao.web.core.HttpContext
import dev.entao.web.core.HttpController
import dev.entao.web.tag.HtmlPage
import dev.entao.web.tag.tag.h1

@dev.entao.web.core.Controller(index = true)
class IndexPage(context: HttpContext) : HttpController(context) {


    @Action(index = true)
    fun index() {

        val p = HtmlPage(context)
        p.body {
            h1 {
                +"Hello Yang"
            }
        }
        p.send()
    }


}