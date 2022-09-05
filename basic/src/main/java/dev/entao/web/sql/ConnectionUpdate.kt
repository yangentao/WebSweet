package dev.entao.web.sql

import dev.entao.web.base.Prop
import dev.entao.web.base.getPropValue
import dev.entao.web.base.plusAssign
import java.sql.Connection
import kotlin.reflect.KMutableProperty

/**
 * Created by entaoyang@163.com on 2017/4/5.
 */


fun Connection.dropTable(tableName: String): Int {
	return this.update("DROP TABLE IF EXISTS $tableName")
}

fun Connection.createTable(tableName: String, columns: List<String>, options:Map<String,String> = emptyMap()): Int {
	val sql = buildString {
		append("CREATE TABLE IF NOT EXISTS $tableName (")
		append(columns.joinToString(", "))
		append(")")
		this += options.map {
			if(it.value.isEmpty()){
				it.key
			}else {
				it.key + "=" + it.value
			}
		}.joinToString(" ")
	}
	return this.update(sql)
}

fun Connection.createIndex(tableName: String, columnName: String) {
	val idxName = "${tableName.trimSQL}_${columnName.trimSQL}_INDEX"
	exec("CREATE INDEX  $idxName ON $tableName(${columnName})")
}


fun Connection.delete(cls: TabClass, w: Where?): Int {
	return this.delete(cls.nameSQL, w)
}

fun Connection.delete(tableName: String, w: Where?): Int {
	val buf = StringBuilder(256)
	buf += "DELETE FROM $tableName "
	if (w != null && w.value.isNotEmpty()) {
		buf += " WHERE "
		buf += w.value
	}
	return this.update(buf.toString(), w?.args ?: emptyList())
}


fun Connection.update(cls: TabClass, map: List<Pair<Prop, Any?>>, w: Where?): Int {
	return this.update(cls.nameSQL, map.map { it.first.nameSQL to it.second }, w)
}


fun Connection.update(tableName: String, values: List<Pair<String, Any?>>, w: Where?): Int {
	assert(values.isNotEmpty())
	val buf = StringBuilder(200)
	buf += "UPDATE $tableName SET "
	buf += values.joinToString(", ") {
		it.first + " = ?"
	}
	var args = values.map { it.second }
	if (w != null && w.value.isNotEmpty()) {
		buf += " WHERE "
		buf += w.value
		args = args + w.args
	}
	return this.update(buf.toString(), args)
}


fun Connection.updateByKey(model: OrmModel, ps: List<KMutableProperty<*>> = model._PropertiesExists): Int {
	val pkList = model::class._PrimaryKeys
	assert(pkList.isNotEmpty())
	val ls = ps.filter { it !in pkList }
	return this.update(model::class, ls.map { it to it.getPropValue(model) }, model.whereByPrimaryKey)
}




