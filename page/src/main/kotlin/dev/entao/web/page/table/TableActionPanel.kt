package dev.entao.web.page.table

import dev.entao.web.base.paramNames
import dev.entao.web.base.userLabel
import dev.entao.web.core.HttpAction
import dev.entao.web.page.ext.ConfirmMessage
import dev.entao.web.page.ext.PageConst
import dev.entao.web.page.ext.PageData
import dev.entao.web.page.ext.linkButton
import dev.entao.web.tag.tag.Tag
import dev.entao.web.tag.tag.div
import dev.entao.web.tag.tag.script
import kotlin.reflect.full.findAnnotation

fun Tag.tableActions(vararg actionList: HttpAction) {
    div("d-flex", "flex-row", "justify-content-start", "border-bottom", "py-2", "mt-3") {
        for (ac in actionList) {
            linkButton("btn btn-sm mx-1") {
                +ac.userLabel
                href = context.uriAction(ac)
                PageData.key attr ac.paramNames.first()
                val cm = ac.findAnnotation<ConfirmMessage>()
                if (cm != null && cm.value.isNotEmpty()) {
                    PageData.confirm2 attr cm.value
                    classAdd("btn-outline-danger")
                } else {
                    classAdd("btn-outline-primary")
                }
                script {
                    """
                        client.table.appendCheckedValues('${this.idx}');
                    """.trimIndent()
                }
            }
        }

    }
}