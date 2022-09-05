@file:Suppress("unused", "FunctionName")

package dev.entao.web.sql

import dev.entao.web.base.Prop

/**
 * Created by yangentao on 2016/12/14.
 */

//where 不支持json格式参数, ?::json

class Where(val value: String) {

	val args = ArrayList<Any>()

	fun addArg(s: Any): Where {
		args.add(s)
		return this
	}

	fun addArgs(s: Collection<Any>): Where {
		args.addAll(s)
		return this
	}

	override fun toString(): String {
		return value
	}
}

fun IsNull(col: String): Where {
	return Where("$col IS NULL")
}

fun IsNull(p: Prop): Where {
	val col = p.fullNameSQL
	return IsNull(col)
}

fun IsNotNull(col: String): Where {
	return Where("$col IS NOT NULL")
}

fun IsNotNull(p: Prop): Where {
	val col = p.fullNameSQL
	return IsNotNull(col)
}

fun EQS(a: Pair<Prop, Any?>, vararg ps: Pair<Prop, Any?>): Where? {
	var w: Where? = a.first.EQ(a.second)
	for (p in ps) {
		w = w AND p.first.EQ(p.second)
	}
	return w
}

infix fun Prop.EQ(value: Any?): Where {
	return this.fullNameSQL.EQ(value)
}

infix fun String.EQ(value: Any?): Where {
	return when (value) {
		null -> IsNull(this)
		is Number -> Where("$this = $value")
		else -> Where("$this = ?").addArg(value)
	}
}

infix fun Prop.NE(value: Any?): Where {
	return this.fullNameSQL.NE(value)
}

infix fun String.NE(value: Any?): Where {
	val s = this
	return when (value) {
		null -> IsNotNull(s)
		is Number -> Where("$s <> $value")
		else -> Where("$s <> ?").addArg(value)
	}
}

infix fun Prop.GE(value: Any): Where {
	return this.fullNameSQL.GE(value)
}

infix fun String.GE(value: Any): Where {
	val s = this
	return Where("$s >= ?").addArg(value)
}

infix fun Prop.GT(value: Any): Where {
	return this.fullNameSQL.GT(value)
}

infix fun String.GT(value: Any): Where {
	val s = this
	return Where("$s > ?").addArg(value)
}

infix fun Prop.LE(value: Any): Where {
	return this.fullNameSQL.LE(value)
}

infix fun String.LE(value: Any): Where {
	val s = this
	return Where("$s <= ?").addArg(value)
}

infix fun Prop.LT(value: Any): Where {
	return this.fullNameSQL.LT(value)
}

infix fun String.LT(value: Any): Where {
	val s = this
	return Where("$s < ?").addArg(value)
}

infix fun Prop.LIKE(value: String): Where {
	return this.fullNameSQL.LIKE(value)
}

infix fun String.LIKE(value: String): Where {
	val s = this
	return Where("$s LIKE ?").addArg(value)
}

infix fun String.LIKE_IN(value: String): Where? {
	if (value.isEmpty()) {
		return null
	}
	return this.LIKE("%$value%")
}

infix fun Prop.LIKE_IN(value: String): Where? {
	return this.fullNameSQL.LIKE_IN(value)
}


infix fun Prop.IN(values: Collection<Any>): Where {
	return this.fullNameSQL.IN(values)
}

infix fun String.IN(values: Collection<Any>): Where {
	val a = values.joinToString(",") { "?" }
	return Where("$this IN ( $a )").addArgs(values)
}

infix fun Where?.AND(other: Where?): Where? {
	if (this == null) {
		return other
	}
	if (other == null) {
		return this
	}
	assert(this.value.isNotEmpty())
	assert(other.value.isNotEmpty())
	val w = Where("($this) AND ($other)")
	w.args.addAll(this.args)
	w.args.addAll(other.args)
	return w
}

infix fun Where?.OR(other: Where?): Where? {
	if (this == null) {
		return other
	}
	if (other == null) {
		return this
	}
	assert(this.value.isNotEmpty())
	assert(other.value.isNotEmpty())
	val w = Where("($this) OR ($other)")
	w.args.addAll(this.args)
	w.args.addAll(other.args)
	return w
}

fun AND_ALL(vararg ws: Where?): Where? {
	var ww: Where? = null
	for (a in ws) {
		ww = ww AND a
	}
	return ww
}