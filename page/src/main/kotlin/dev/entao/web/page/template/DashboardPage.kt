@file:Suppress("MemberVisibilityCanBePrivate")

package dev.entao.web.page.template

import dev.entao.web.base.userLabel
import dev.entao.web.core.HttpAction
import dev.entao.web.core.HttpContext
import dev.entao.web.core.HttpController
import dev.entao.web.core.actionList
import dev.entao.web.core.render.backErrorMessage
import dev.entao.web.core.render.backSuccessMessage
import dev.entao.web.core.uri
import dev.entao.web.page.BootPage
import dev.entao.web.page.ext.NavGroup
import dev.entao.web.page.ext.NavItem
import dev.entao.web.page.ext.PageConst
import dev.entao.web.page.ext.navItem
import dev.entao.web.tag.tag.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

class DashboardPage(context: HttpContext) : BootPage(context) {

    val topBar: TopBar = TopBar(context)
    val sideBar: SidebarNav = SidebarNav(context)
    val workBar: WorkBarDiv = WorkBarDiv(context)
    val alertDiv: AlertDiv = AlertDiv(context)
    lateinit var workspaceDiv: DivTag
        private set

    init {
        head {
            title(context.app.appName)
        }
        body {
            append(topBar)
            div("container-fluid") {
                div("row") {
                    append(sideBar)
                    main("col-md-9", "ms-sm-auto", "col-lg-10", "px-md-4") {
                        append(workBar)
                        div("row", "py-2") {
                            append(alertDiv) {
                                id = PageConst.MSG_DIV
                            }
                        }
                        workspaceDiv = div("row", "py-10") {
                            id = "workspace"

                        }
                    }
                }
            }
        }
        buildSideItems()
    }

    private fun buildSideItems() {
        val cs = ArrayList<KClass<out HttpController>>(context.httpService.routeManager.allControllers)
        for (c in cs) {
            val itemList = c.actionList.filter { it.hasAnnotation<NavItem>() }.sortedBy { it.findAnnotation<NavItem>()?.order ?: 100 }
            if (itemList.isEmpty()) {
                continue
            }
            val sg = c.findAnnotation<NavGroup>()
            if (sg != null) {
                sideBar.submenu(sg.label.ifEmpty { c.userLabel }, sg.icon) {
                    itemList.forEach { ac ->
                        navItem(ac)
                    }
                }
            } else {
                itemList.forEach { ac ->
                    sideBar.addItem(ac)
                }
            }
        }
    }
//
//    fun Tag.workCol(block: DivTag.() -> Unit) {
//        div("col-lg-8", "col-md-10", "col-sm-12", block = block)
//    }

    fun workspace(block: DivTag.() -> Unit) {
        workspaceDiv.block()
    }

    fun workTitle(text: String) {
        workBar.titleTag.text(text)
    }

    fun workToolbar(block: TagBlock) {
        workBar.toolbarDiv.block()
    }

    fun successMessage(msg: String) {
        alertDiv.successMessage(msg)
    }

    fun errorMessage(msg: String) {
        alertDiv.errorMessage(msg)
    }
}

class AlertDiv(context: HttpContext) : DivTag(context) {
    val textNode: TextTag = TextTag(context)

    init {
        role = "alert"
        classAdd("alert", "alert-danger", "alert-dismissible", "fade", "show", "d-none")
        append(textNode)
        button("btn-close") {
            "data-bs-dismiss" attr "alert"
            "aria-label" attr "Close"
        }

        context.backSuccessMessage?.also {
            successMessage(it)
        }
        context.backErrorMessage?.also {
            errorMessage(it)
        }

    }

    fun successMessage(msg: String) {
        this.classRemove("d-none")
        this.classRemove("alert-danger")
        this.classAdd("alert-success")
        textNode.text = msg
    }

    fun errorMessage(msg: String) {
        this.classRemove("d-none")
        this.classRemove("alert-success")
        this.classAdd("alert-danger")
        textNode.text = msg
    }
}

class WorkBarDiv(context: HttpContext) : DivTag(context) {
    lateinit var titleTag: H4Tag
        private set
    lateinit var toolbarDiv: DivTag
        private set

    init {
        classAdd("row", "pt-3", "pb-2", "mb-3", "border-bottom")
        id = "workHeader"
        div("col") {
            titleTag = h4("h4") {
                id = "workTitle"
            }
        }
        div("col-auto") {
            toolbarDiv = div("btn-toolbar", "mb-2", "mb-md-0") {
                id = "workToolbar"
            }
        }
    }
}

class SidebarNav(context: HttpContext) : NavTag(context) {
    lateinit var listTag: UlTag
        private set

    init {
        classAdd("col-md-3", "col-lg-2", "d-md-block", "bg-dark", "sidebar", "collapse")
        id = "sidebarMenu"
        div("position-sticky", "pt-3") {
            listTag = ul("nav", "flex-column") {
                id = "leftNav"

            }
        }
    }

    fun addItem(action: HttpAction) {
        listTag.navItem(action)
    }

    fun addItem(label: String, href: String, spanIcon: String?) {
        listTag.navItem(label, href, spanIcon)
    }

    fun submenu(label: String, iconPath: String?, block: UlTag.() -> Unit) {
        val menuID = makeID("submenu")
        listTag.li("nav-item") {
            val menuTag = a("nav-link") {
                href = "#$menuID"
                "aria-current" attr "page"
                "data-bs-toggle" attr "collapse"

                if (iconPath != null) {
                    img("yet-side-icon") {
                        src = iconPath.uri
                    }
                }
                +label
            }
            val itemsTag = ul("submenu", "collapse ") {
                id = menuID
                this.block()
            }
            val activeItem = itemsTag.first { it.tagName == "a" && it.classHas("active") }
            if (activeItem != null) {
                menuTag.classAdd("active")
                itemsTag.classAdd("show")
            }
        }
    }

}

class TopBar(context: HttpContext) : HeaderTag(context) {
    val brandAnchor: AnchorTag
    val listTag: UlTag
    var userButton: ButtonTag

    init {
        classAdd("navbar", "navbar-dark", "sticky-top", "bg-dark", "flex-md-nowrap", "p-0", "shadow")
        brandAnchor = a("navbar-brand", "col-md-3", "col-lg-2", "me-auto", "px-3") {
            id = "navbarBrand"
            "href" attr context.contextPath
            +context.app.appName
        }
        listTag = ul("d-flex", "flex-row", "list-inline", "my-auto") {
            id = "topNav"
        }
        userButton = button("btn", "btn-dark", "me-1") {
            id = "ACCOUNT_BTN"
            +"登录"
        }
        button("navbar-toggler", "d-md-none", "collapsed") {
            "type" attr "button"
            "data-bs-toggle" attr "collapse"
            "data-bs-target" attr "#sidebarMenu"
            "aria-controls" attr "sidebarMenu"
            "aria-expanded" attr "false"
            "aria-label" attr "Toggle navigation"
            span("navbar-toggler-icon") {}
        }
    }

    fun brand(block: AnchorTag.() -> Unit) {
        this.brandAnchor.block()
    }

    fun user(block: ButtonTag.() -> Unit) {
        userButton.apply(block)
    }

    fun addItem(block: AnchorTag.() -> Unit) {
        listTag.li("nav-item", "text-nowrap") {
            a("nav-link") {
                this.block()
            }
        }
    }
}