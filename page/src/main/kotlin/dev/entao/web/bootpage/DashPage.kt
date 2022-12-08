package dev.entao.web.bootpage

import dev.entao.web.core.HttpContext
import dev.entao.web.core.uri
import dev.entao.web.tag.tag.*

class DashPage(context: HttpContext) : BootPage(context) {

    init {

        head {

            title("Dashboards")
            style {
                exampleCSS
            }
            linkCSS(href = "dashboard.css".pageRes)
        }

        body {
            header("navbar navbar-dark sticky-top bg-dark flex-md-nowrap p-0 shadow") {
                a("navbar-brand col-md-3 col-lg-2 me-0 px-3 fs-6") {
                    href = "#"
                    +context.httpService.app.appName
                }
            }

            h1 {
                +"Hello Bootstrap!"
            }
        }
    }

    companion object {
        val exampleCSS: String
            get() = """
        .bd-placeholder-img {
            font-size: 1.125rem;
            text-anchor: middle;
            -webkit-user-select: none;
            -moz-user-select: none;
            user-select: none;
        }

        @media (min-width: 768px) {
            .bd-placeholder-img-lg {
                font-size: 3.5rem;
            }
        }

        .b-example-divider {
            height: 3rem;
            background-color: rgba(0, 0, 0, .1);
            border: solid rgba(0, 0, 0, .15);
            border-width: 1px 0;
            box-shadow: inset 0 .5em 1.5em rgba(0, 0, 0, .1), inset 0 .125em .5em rgba(0, 0, 0, .15);
        }
        
        .b-example-vr {
            flex-shrink: 0;
            width: 1.5rem;
            height: 100vh;
        }
        
        .bi {
            vertical-align: -.125em;
            fill: currentColor;
        }
        
        .nav-scroller {
            position: relative;
            z-index: 2;
            height: 2.75rem;
            overflow-y: hidden;
        }
        
        .nav-scroller .nav {
            display: flex;
            flex-wrap: nowrap;
            padding-bottom: 1rem;
            margin-top: -1px;
            overflow-x: auto;
            text-align: center;
            white-space: nowrap;
            -webkit-overflow-scrolling: touch;
        }
            """.trimIndent()

    }
}