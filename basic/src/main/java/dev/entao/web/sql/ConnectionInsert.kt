package dev.entao.web.sql


import dev.entao.web.base.Prop
import dev.entao.web.base.getPropValue
import dev.entao.web.base.plusAssign
import dev.entao.web.base.useX
import dev.entao.web.log.logd
import java.sql.Connection
import kotlin.reflect.KClass

fun Connection.insert(modelCls: KClass<*>, kvs: List<Pair<Prop, Any?>>): Boolean {
	return this.insert(modelCls.nameSQL, kvs.map { it.first.nameSQL to it.second })
}

fun Connection.insert(table: String, kvs: List<Pair<String, Any?>>): Boolean {
	val ks = kvs.joinToString(", ") { it.first }
	val vs = kvs.joinToString(", ") { "?" }
	val sql = "INSERT INTO $table ($ks) VALUES ($vs) "
	val args = kvs.map { it.second }
	return this.update(sql, args) > 0
}

fun Connection.insertGenKey(modelCls: KClass<*>, kvs: List<Pair<Prop, Any?>>): Long {
	return this.insertGenKey(modelCls.nameSQL, kvs.map { it.first.nameSQL to it.second })
}

fun Connection.insertGenKey(table: String, kvs: List<Pair<String, Any?>>): Long {
	val ks = kvs.joinToString(", ") { it.first }
	val vs = kvs.joinToString(", ") { "?" }
	val sql = "INSERT INTO $table ($ks) VALUES ($vs) "
	val args = kvs.map { it.second }
	return this.insertGen(sql, args)
}

fun Connection.insertGen(sql: String, args: List<Any?>): Long {
	val st = this.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
	st.setParams(args)
	if (ConnPick.enableLog) {
		logd(sql)
		logd(args)
	}
	st.useX {
		val n = it.executeUpdate()
		return if (n <= 0) {
			0L
		} else {
			it.generatedKeys.firstLong() ?: 0L
		}
	}
}

fun Connection.insertOrUpdate(modelCls: KClass<*>, kvs: List<Pair<Prop, Any?>>, uniqColumns: List<Prop>): Boolean {
	return this.insertOrUpdate(modelCls.nameSQL, kvs.map { it.first.nameSQL to it.second }, uniqColumns.map { it.nameSQL })
}

fun Connection.insertOrUpdate(table: String, kvs: List<Pair<String, Any?>>, uniqColumns: List<String>): Boolean {
	if (uniqColumns.isEmpty()) {
		throw IllegalArgumentException("insertOrUpdate $table  uniqColumns 参数不能是空")
	}
	val ks = kvs.joinToString(", ") { it.first }
	val vs = kvs.joinToString(", ") { "?" }
	val buf = StringBuilder(512)
	buf.append("INSERT INTO $table ($ks ) VALUES ( $vs ) ")

	val updateCols = kvs.filter { it.first !in uniqColumns }
	buf += " ON DUPLICATE KEY UPDATE "
	buf += updateCols.joinToString(", ") { "${it.first} = ? " }
	return this.update(buf.toString(), kvs.map { it.second } + updateCols.map { it.second }) > 0
}

//现有记录和要插入的记录完全一样, 也会返回false, 表示没有更新
fun Connection.insertOrUpdate(model: OrmModel): Boolean {
	val pks = model::class._PrimaryKeys
	assert(pks.isNotEmpty())
	val cs = model._PropertiesExists
	return this.insertOrUpdate(model::class, cs.map { it to it.getPropValue(model) }, pks)
}

