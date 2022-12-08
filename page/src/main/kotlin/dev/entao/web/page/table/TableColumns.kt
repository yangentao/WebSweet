@file:Suppress("MemberVisibilityCanBePrivate")

package dev.entao.web.page.table

import dev.entao.web.base.DatePattern
import dev.entao.web.base.Prop
import dev.entao.web.base.dateDisplay
import dev.entao.web.base.getPropValue
import dev.entao.web.base.lowerCased
import dev.entao.web.base.userLabel
import dev.entao.web.base.userName
import dev.entao.web.core.HttpContext
import dev.entao.web.core.controllers.Upload
import dev.entao.web.core.controllers.UploadController
import dev.entao.web.core.controllers.scaleImageUrl
import dev.entao.web.core.values
import dev.entao.web.json.YsonObject
import dev.entao.web.page.dialog.dataSrcLarge
import dev.entao.web.page.ext.UploadImage
import dev.entao.web.page.ext.displayString
import dev.entao.web.page.ext.displayTextOf
import dev.entao.web.sql.*
import dev.entao.web.tag.tag.Tag
import dev.entao.web.tag.tag.a
import dev.entao.web.tag.tag.img
import dev.entao.web.tag.tag.text
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class CustomColumnInfo<T : Any>(context: HttpContext, title: String, sortCol: String, val textCallback: (T) -> String) : BasicColumnInfo<T>(context, title, sortCol) {

    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        val s = textCallback(item)
        tdTag.text(s)
    }
}

class ImageColumnInfo<T : Any>(context: HttpContext, val prop: Prop, title: String = prop.userLabel, sortCol: String = prop.userName) : BasicColumnInfo<T>(context, title, sortCol) {
    var listWidth: Int = 64
    var gridWidth: Int = 320
    var largeWidth: Int = 0

    init {
        val imgAnn = prop.findAnnotation<UploadImage>()
        if (imgAnn != null) {
            listWidth = imgAnn.listWidth
            gridWidth = imgAnn.gridWidth
            largeWidth = imgAnn.largeWidth
        }
    }

    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        val imgId: Int = prop.getPropValue(item) as? Int ?: 0
        tdTag.img {
            "src" attr scaleImageUrl(imgId, listWidth)
            data("src-grid", scaleImageUrl(imgId, gridWidth))
            dataSrcLarge = scaleImageUrl(imgId, largeWidth)
        }
    }

}

class TextColumnInfo<T : Any>(context: HttpContext, val prop: Prop, title: String = prop.userLabel, sortCol: String = prop.userName) : BasicColumnInfo<T>(context, title, sortCol) {
    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        val s = displayTextOf(prop, prop.getPropValue(item))
        tdTag.trimText(s)
    }

}

class PropColumnInfo<T : Any>(context: HttpContext, val prop: Prop, title: String = prop.userLabel, sortCol: String = prop.userName) : BasicColumnInfo<T>(context, title, sortCol) {
    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        val imgAnn = prop.findAnnotation<UploadImage>()
        if (imgAnn == null) {
            val s = prop.displayString(item)
            tdTag.trimText(s)
        } else {
            val imgId: Int = prop.getPropValue(item) as? Int ?: 0
            tdTag.img {
                "src" attr scaleImageUrl(imgId, imgAnn.listWidth)
                data("src-grid", scaleImageUrl(imgId, imgAnn.gridWidth))
                data("src-large", scaleImageUrl(imgId, imgAnn.largeWidth))
            }
        }
    }
}

typealias KeyColumnDisplayBlock<T> = (T, String) -> String

class KeyColumnInfo<T : Any>(context: HttpContext, title: String, val key: String, sortCol: String = key) : BasicColumnInfo<T>(context, title, sortCol) {

    private var displayBlock: KeyColumnDisplayBlock<T>? = null

    private fun defaultDisplay(item: T, key: String): String {
        return when (item) {
            is ResultSet -> item.getObject(key.lowerCased).toString()
            is YsonObject -> item[key]?.toString() ?: ""
            is Map<*, *> -> item[key]?.toString() ?: ""
            else -> ""
        }
    }

    fun display(block: KeyColumnDisplayBlock<T>): KeyColumnInfo<T> {
        this.displayBlock = block
        return this
    }

    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        val s = displayBlock?.invoke(item, key) ?: defaultDisplay(item, key)
        tdTag.text(s)
    }

}

class ForeignKeyColumnInfo<T : Any>(context: HttpContext, val prop: Prop, title: String = prop.userLabel, sortCol: String = prop.userName) : BasicColumnInfo<T>(context, title, sortCol) {
    val foreignKey: ForeignKey = prop.refModel ?: error("No ForeignKey found on $prop")

    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        val v = prop.getPropValue(item) ?: return
        val s = foreignKey.display(v)
        tdTag.trimText(s)
    }
}

class DownloadColumnInfo<T : Any>(context: HttpContext, val prop: Prop, title: String = "文件") :
    BasicColumnInfo<T>(context, title, "") {

    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        val v = prop.getPropValue(item) ?: return
        val u = Upload.oneByKey(v) ?: return
        tdTag.a("yet-action") {
            href = UploadController::download.values(v)
            +u.rawname.ifEmpty { v.toString() }
        }
    }
}

class DateColumnInfo<T : Any>(context: HttpContext, val prop: Prop, val format: String?) :
    BasicColumnInfo<T>(context, prop.userLabel, prop.userName) {

    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        val v = prop.getPropValue(item) ?: return
        val fmt = format ?: prop.findAnnotation<DatePattern>()?.format
        val s = if (fmt != null && fmt.trim().isNotEmpty()) {
            dateDisplay(v, fmt)
        } else {
            v.toString()
        }
        tdTag.text(s)
    }
}

class RefColumnInfo<T : Any>(context: HttpContext, val prop: Prop, val refTable: KClass<out OrmModel>, val refProp: Prop, val refDisplay: Prop? = null, val filter: Where? = null) :
    BasicColumnInfo<T>(context, prop.userLabel, prop.userName) {

    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        val v = prop.getPropValue(item) ?: return
        val s = refDisplay(v, refTable, refProp, refDisplay, filter)
        tdTag.trimText(s)
    }
}

private fun refDisplay(v: Any, refTable: KClass<out OrmModel>, refProp: Prop, refDisplay: Prop?, filter: Where?): String? {
    if (refDisplay == null || refProp.nameSQL == refDisplay.nameSQL) {
        return ConnPick.connection.querySQL {
            selectDistinct(refProp)
            from(refTable)
            where(filter, refProp EQ v)
            orderBy(refProp.ASC)
            limit(1)
        }.firstRow {
            it.getObject(refProp.nameSQL).toString()
        }
    } else {
        return ConnPick.connection.querySQL {
            select(refProp, refDisplay)
            from(refTable)
            where(filter, refProp EQ v)
            orderBy(refProp.ASC)
            limit(1)
        }.firstRow {
            it.getObject(refDisplay.nameSQL).toString()
        }
    }
}