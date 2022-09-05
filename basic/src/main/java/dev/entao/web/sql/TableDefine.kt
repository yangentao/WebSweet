package dev.entao.web.sql

import dev.entao.web.base.Comment
import dev.entao.web.base.FixLength
import dev.entao.web.base.Label
import dev.entao.web.base.MaxLength
import dev.entao.web.base.NullValue
import dev.entao.web.base.Prop
import dev.entao.web.base.plusAssign
import dev.entao.web.json.YsonArray
import dev.entao.web.json.YsonObject
import java.sql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation


private val tableCreateSet = HashSet<KClass<*>>()

@Synchronized
fun tableMigrate(vararg tables: KClass<*>) {
	for (table in tables) {
		if (table !in tableCreateSet) {
			TableMigrater(table.namedConnection, table)
		}
	}
}


class TableMigrater(private val conn: Connection, private val cls: KClass<*>) {
	val name: String = cls.nameSQL
	private val columns: List<DefineColumnInfo> = cls._SQLProperties.map { DefineColumnInfo(conn, it) }.sortedWith(Comparator<DefineColumnInfo> { o1, o2 ->
		var n = -o1.pk.compareTo(o2.pk)
		if (n == 0) {
			n = -(o1.unique ?: "").compareTo(o2.unique ?: "")
			if (n == 0) {
				n = -o1.index.compareTo(o2.index)
			}
			if (n == 0) {
				n = o1.name.compareTo(o2.name)
			}
		}
		n
	})


	init {
		if (cls !in tableCreateSet) {
			tableCreateSet.add(cls)
			if (cls.findAnnotation<AutoCreateTable>()?.value != false) {
				if (!conn.tableExists(name)) {
					createTable(conn)
				} else {
					mergeTable()
					mergeIndex()
				}
			}
		}
	}

	private fun mergeIndex() {
		val oldIdxs = conn.tableIndexList(name).map { it.COLUMN_NAME }.toSet()
		val newIdxs = columns.filter { it.index }
		for (p in newIdxs) {
			if (p.name !in oldIdxs) {
				conn.createIndex(name, p.name)
			}
		}
	}

	private fun mergeTable() {
		val cols: Set<String> = conn.tableDesc(name).map { it.columnName }.toSet()
		for (p in columns) {
			if (p.name !in cols) {
				val s = p.defColumnn()
				conn.exec("ALTER TABLE $name ADD COLUMN $s")
			}
		}
	}

	private fun createTable(conn: Connection) {
		val colList: MutableList<String> = columns.map {
			it.defColumnn()
		}.toMutableList()
		val pkCols = columns.filter { it.pk }.joinToString(",") { it.name }
		if (pkCols.isNotEmpty()) {
			colList += "PRIMARY KEY ($pkCols)"
		}
		val uniq = columns.filter { it.unique != null && it.unique.isEmpty() }
		for (u in uniq) {
			colList += "UNIQUE (${u.name}) "
		}
		val uniq2 = columns.filter { it.unique != null && it.unique.isNotEmpty() }.groupBy { it.unique }
		for ((k, ls) in uniq2) {
			val s = ls.joinToString(",") { it.name }
			colList += "CONSTRAINT $k UNIQUE ($s) "
		}
		val opMap = LinkedHashMap<String, String>()
		for (col in columns) {
			val ai = col.prop.findAnnotation<AutoInc>()
			if (ai != null) {
				opMap["AUTO_INCREMENT"] = ai.value.toString()
				break
			}
		}
		conn.createTable(name, colList, opMap)
		val idxList = columns.filter { it.index }
		for (idx in idxList) {
			conn.createIndex(name, idx.name)
		}
	}
}

class DefineColumnInfo(private val conn: Connection, val prop: Prop) {
	val name: String = prop.nameSQL
	val pk: Boolean = prop.hasAnnotation<PrimaryKey>()
	val index: Boolean = prop.hasAnnotation<Index>()
	val unique: String? = prop.findAnnotation<Unique>()?.value
	val commment: String? = prop.findAnnotation<Comment>()?.value ?: prop.findAnnotation<Label>()?.value

	fun defColumnn(): String {
		for (d in definer) {
			if (d.accept(conn, prop)) {
				return d.define(conn, prop)
			}
		}
		error("NOT Support $prop ")
	}

	companion object {
		val definer: List<FieldDefine> = listOf(
			StringFieldDefine,
			IntegerFieldDefine,
			RealFieldDefine,
			DateTimeFieldDefine,
			BoolFieldDefine,
			JsonFieldDefine,
			ByteArrayFieldDefine,
		)
	}
}


interface FieldDefine {
	fun accept(conn: Connection, p: Prop): Boolean
	fun define(conn: Connection, p: Prop): String
}

abstract class BaseFieldDefine : FieldDefine {
	abstract val clsSet: Set<KClass<*>>
	override fun accept(conn: Connection, p: Prop): Boolean {
		return p.returnType.classifier in clsSet
	}

	final override fun define(conn: Connection, p: Prop): String {
		val sb = StringBuilder()
		sb += processName(conn, p)
		sb += " "
		sb += processType(conn, p)
		if (p.hasAnnotation<NotNull>()) {
			if (!p.hasAnnotation<PrimaryKey>() && !p.hasAnnotation<AutoInc>()) {
				sb += " "
				sb += processNotNULL(conn, p)
			}
		}
		val dv = p.findAnnotation<NullValue>()
		if (dv != null) {
			val s = processDefault(conn, p, dv)
			if (s.isNotEmpty()) {
				sb += " DEFAULT "
				sb += s
			}
		}
		val lb = p.findAnnotation<Comment>()?.value ?: p.findAnnotation<Label>()?.value
		if (lb != null) {
			sb += " COMMENT '$lb'"
		}
		return sb.toString()
	}

	open fun processDefault(conn: Connection, p: Prop, dv: NullValue): String {
		return dv.value
	}

	open fun processNotNULL(conn: Connection, p: Prop): String {
		return "NOT NULL"
	}

	abstract fun processType(conn: Connection, p: Prop): String
	open fun processName(conn: Connection, p: Prop): String {
		return p.nameSQL
	}

	protected fun sqlText(s: String): String {
		if (s.isEmpty()) return ""
		if (s.startsWith("'") && s.endsWith("'")) return s
		return "'${s}'"
	}
}

object JsonFieldDefine : BaseFieldDefine() {
	override val clsSet: Set<KClass<*>> = setOf(YsonArray::class, YsonObject::class)
	override fun processType(conn: Connection, p: Prop): String {
		return "JSON"
	}

	override fun processDefault(conn: Connection, p: Prop, dv: NullValue): String {
		return sqlText(dv.value)
	}

}

object ByteArrayFieldDefine : BaseFieldDefine() {
	override val clsSet: Set<KClass<*>> = setOf(ByteArray::class)
	override fun processType(conn: Connection, p: Prop): String {
		val maxLengthValue: Int = p.findAnnotation<MaxLength>()?.value ?: 255
		return if (maxLengthValue < 65535) {
			"BLOB"
		} else {
			"LONGBLOB"
		}

	}

}

object BoolFieldDefine : BaseFieldDefine() {
	override val clsSet: Set<KClass<*>> = setOf(Boolean::class)
	private val trueList: List<String> = listOf("true", "on", "yes", "1")
	override fun processType(conn: Connection, p: Prop): String {
		return "BOOLEAN"
	}

	override fun processDefault(conn: Connection, p: Prop, dv: NullValue): String {
		if (dv.value in trueList) {
			return "1"
		}
		return "0"
	}

}

object IntegerFieldDefine : BaseFieldDefine() {
	override val clsSet: Set<KClass<*>> = setOf(Long::class, Int::class, Short::class, Byte::class)

	override fun processType(conn: Connection, p: Prop): String {
		val autoInc: Boolean = p.hasAnnotation<AutoInc>()
		return when (p.returnType.classifier) {
			Int::class, Short::class, Byte::class -> {
				if (autoInc) {
					"INTEGER UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE"
				} else {
					"INTEGER"
				}
			}
			Long::class -> {
				if (autoInc) {
					"SERIAL"
				} else {
					"BIGINT"
				}
			}
			else -> error("Type NOT Support")
		}
	}

}

object RealFieldDefine : BaseFieldDefine() {
	override val clsSet: Set<KClass<*>> = setOf(Float::class, Double::class)


	override fun processType(conn: Connection, p: Prop): String {
		val decimal = p.findAnnotation<Decimal>()
		return when (p.returnType.classifier) {
			Float::class -> {
				if (decimal != null) {
					"DECIMAL(${decimal.m},${decimal.d})"
				} else {
					"REAL"
				}
			}
			Double::class -> {
				if (decimal != null) {
					"DECIMAL(${decimal.m},${decimal.d})"
				} else {
					"DOUBLE PRECISION"
				}
			}
			else -> error("Type NOT Support")
		}
	}

}

object DateTimeFieldDefine : BaseFieldDefine() {
	override val clsSet: Set<KClass<*>> = setOf(java.util.Date::class, java.sql.Date::class, java.sql.Time::class, java.sql.Timestamp::class)

	override fun processType(conn: Connection, p: Prop): String {
		return when (p.returnType.classifier) {
			java.sql.Time::class -> "TIME"
			java.sql.Timestamp::class -> "DATETIME"
			java.sql.Date::class -> "DATE"
			java.util.Date::class -> "LONG"
			else -> error("error type")
		}
	}

	override fun processDefault(conn: Connection, p: Prop, dv: NullValue): String {
		return sqlText(dv.value)
	}
}

object StringFieldDefine : BaseFieldDefine() {
	override val clsSet: Set<KClass<*>> = setOf(String::class)

	override fun processType(conn: Connection, p: Prop): String {
		val fixLen = p.findAnnotation<FixLength>()
		val maxLengthValue: Int = p.findAnnotation<MaxLength>()?.value ?: 255
		return when {
			fixLen != null -> "CHAR(${fixLen.value})"
			maxLengthValue >= 65535 -> "longtext"
			else -> "varchar($maxLengthValue)"
		}
	}

	override fun processDefault(conn: Connection, p: Prop, dv: NullValue): String {
		return sqlText(dv.value)
	}

}
