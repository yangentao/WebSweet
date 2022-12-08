package dev.entao.web.page.template

import dev.entao.web.base.valueParams
import dev.entao.web.core.HttpAction
import dev.entao.web.core.HttpContext
import dev.entao.web.core.render.backErrorMessage
import dev.entao.web.core.uri
import dev.entao.web.page.BootPage
import dev.entao.web.page.ext.forLabelPre
import dev.entao.web.page.ext.fromParameter
import dev.entao.web.page.ext.needsValidation
import dev.entao.web.tag.tag.*


class LoginPage(context: HttpContext) : BootPage(context) {

    lateinit var userLabel: LabelTag
    lateinit var pwdLabel: LabelTag
    lateinit var userInput: InputTag
    lateinit var pwdInput: InputTag
    lateinit var form: FormTag

    init {
        prepare()
    }

    fun fromAction(action: HttpAction) {
        val ps = action.valueParams
        userLabel.fromParameter(ps[0])
        userInput.fromParameter(ps[0])

        pwdLabel.fromParameter(ps[1])
        pwdInput.fromParameter(ps[1])

        form.action = action.uri
    }

    private fun prepare() {
        title("登录")
        body {
            div("container") {
                div("row", "justify-content-md-center") {
                    p {
                        style = "height:3rem"
                    }
                    div("card") {
                        style = "width:30rem;"
                        div("card-body") {
                            div("card-title", "text-center") {
                                +"登录"
                            }
                            form = form("row", "g-3") {
                                method = "POST"
                                action = "#"
                                needsValidation()
                                "novalidate" attr "novalidate"
                                div("mb-3") {
                                    userLabel = label("form-label") {
                                        +"用户名"
                                    }
                                    userInput = input("form-control") {
                                        forLabelPre()
                                        name = "user"
                                        required()
                                    }
                                    div("invalid-feedback") {
                                        +"请输入用户名"
                                    }
                                }
                                div("mb-3") {
                                    pwdLabel = label("form-label") {
                                        +"密码"
                                    }
                                    pwdInput = password("form-control") {
                                        forLabelPre()
                                        name = "pwd"
                                        required()
                                    }
                                    div("invalid-feedback") {
                                        +"请输入密码"
                                    }
                                }
                                hidden {
                                    name = "backurl"
                                    this.valueFromContext()
                                }
                                submit("btn", "btn-primary") {
                                    +"提交"
                                }
                            }
                            div("card-text", "text-center", "text-danger") {
                                +context.backErrorMessage
                            }
                        }
                    }
                }
            }
        }
    }


}