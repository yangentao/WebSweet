package dev.entao.web.page.ext

import dev.entao.web.base.DatePattern
import dev.entao.web.base.NumberFormat
import dev.entao.web.base.Prop
import dev.entao.web.base.Prop0
import dev.entao.web.base.display
import dev.entao.web.base.format
import dev.entao.web.base.getPropValue
import dev.entao.web.core.controllers.Upload
import dev.entao.web.json.YsonValue
import dev.entao.web.sql.Decimal
import dev.entao.web.sql.display
import dev.entao.web.sql.refModel
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2017/4/8.
 */


//  """^1[3,4,5,6,7,8,9]\d{9}$"""

fun String.matchPhone(): Boolean {
    return this.matches("^1[3456789]\\d{9}$".toRegex())
}





fun Prop.displayString(inst: Any?): String {
    return when {
        this is Prop0 -> displayOf(this, this.getPropValue())
        inst != null -> displayOf(this, this.getPropValue(inst))
        else -> ""

    }
}

fun displayOf(p: Prop, v: Any?): String {
    if (v == null) {
        return ""
    }
    if (v is YsonValue) {
        return v.yson()
    }
    val opList = p.findAnnotation<OptionList>()
    if (opList != null) {
        return opList.display(v)
    }
    val refAn = p.refModel
    if (refAn != null) {
        return refAn.display(v)
    }
    val resAn = p.findAnnotation<RefUpload>()
    if (resAn != null) {
        return Upload.oneByKey(v)?.rawname ?: v.toString()
    }
    val decAn = p.findAnnotation<Decimal>()
    if (decAn != null) {
        return decAn.display(v)
    }

    val numPat = p.findAnnotation<NumberFormat>()?.pattern
    if (numPat != null && numPat.isNotEmpty()) {
        if (v is Number) {
            return v.format(numPat)
        }
    }

    val dateAn = p.findAnnotation<DatePattern>()
    if (dateAn != null) {
        return dateAn.display(v)
    }

    return v.toString()
}


//不处理关联表, 只处理格式
fun displayTextOf(p: Prop, v: Any?): String {
    if (v == null) {
        return ""
    }
    if (v is YsonValue) {
        return v.yson()
    }
    val opList = p.findAnnotation<OptionList>()
    if (opList != null) {
        return opList.display(v)
    }
    val decAn = p.findAnnotation<Decimal>()
    if (decAn != null) {
        return decAn.display(v)
    }

    val numPat = p.findAnnotation<NumberFormat>()?.pattern
    if (numPat != null && numPat.isNotEmpty()) {
        if (v is Number) {
            return v.format(numPat)
        }
    }

    val dateAn = p.findAnnotation<DatePattern>()
    if (dateAn != null) {
        return dateAn.display(v)
    }

    return v.toString()
}


