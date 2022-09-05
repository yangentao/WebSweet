@file:Suppress("SqlNoDataSourceInspection")

package dev.entao.web.sql

import dev.entao.web.base.KClassValue
import dev.entao.web.base.KPropValue
import dev.entao.web.base.Name
import dev.entao.web.base.Prop
import dev.entao.web.base.lowerCased
import dev.entao.web.base.ownerClass
import dev.entao.web.base.useX
import dev.entao.web.base.userName
import dev.entao.web.base.valueParams
import dev.entao.web.json.YsonArray
import dev.entao.web.json.YsonBlob
import dev.entao.web.json.YsonBool
import dev.entao.web.json.YsonNull
import dev.entao.web.json.YsonNum
import dev.entao.web.json.YsonObject
import dev.entao.web.json.YsonString
import dev.entao.web.log.logd
import java.math.BigDecimal
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.Types
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty0
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation


/**
 * Created by entaoyang@163.com on 2017/6/10.
 */

typealias TabClass = KClass<*>
typealias SQLProc = KFunction<*>
typealias ConstraintException = SQLIntegrityConstraintViolationException

private const val mysqlKeywors =
    "ACCESSIBLE,ADD,ANALYZE,ASC,BEFORE,CASCADE,CHANGE,CONTINUE,DATABASE,DATABASES,DAY_HOUR,DAY_MICROSECOND,DAY_MINUTE,DAY_SECOND,DELAYED,DESC,DISTINCTROW,DIV,DUAL,ELSEIF,EMPTY,ENCLOSED,ESCAPED,EXIT,EXPLAIN,FIRST_VALUE,FLOAT4,FLOAT8,FORCE,FULLTEXT,GENERATED,GROUPS,HIGH_PRIORITY,HOUR_MICROSECOND,HOUR_MINUTE,HOUR_SECOND,IF,IGNORE,INDEX,INFILE,INT1,INT2,INT3,INT4,INT8,IO_AFTER_GTIDS,IO_BEFORE_GTIDS,ITERATE,JSON_TABLE,KEY,KEYS,KILL,LAG,LAST_VALUE,LEAD,LEAVE,LIMIT,LINEAR,LINES,LOAD,LOCK,LONG,LONGBLOB,LONGTEXT,LOOP,LOW_PRIORITY,MASTER_BIND,MASTER_SSL_VERIFY_SERVER_CERT,MAXVALUE,MEDIUMBLOB,MEDIUMINT,MEDIUMTEXT,MIDDLEINT,MINUTE_MICROSECOND,MINUTE_SECOND,NO_WRITE_TO_BINLOG,NTH_VALUE,NTILE,OPTIMIZE,OPTIMIZER_COSTS,OPTION,OPTIONALLY,OUTFILE,PURGE,READ,READ_WRITE,REGEXP,RENAME,REPEAT,REPLACE,REQUIRE,RESIGNAL,RESTRICT,RLIKE,SCHEMA,SCHEMAS,SECOND_MICROSECOND,SEPARATOR,SHOW,SIGNAL,SPATIAL,SQL_BIG_RESULT,SQL_CALC_FOUND_ROWS,SQL_SMALL_RESULT,SSL,STARTING,STORED,STRAIGHT_JOIN,TERMINATED,TINYBLOB,TINYINT,TINYTEXT,UNDO,UNLOCK,UNSIGNED,USAGE,USE,UTC_DATE,UTC_TIME,UTC_TIMESTAMP,VARBINARY,VARCHARACTER,VIRTUAL,WHILE,WRITE,XOR,YEAR_MONTH,ZEROFILL"
val mysqlKeySet: Set<String> = mysqlKeywors.lowerCased.split(',').toSet()

val KClass<*>.nameSQL: String by KClassValue {
    val s = it.findAnnotation<Name>()?.value ?: it.simpleName!!.lowerCased
    escSQLName(s)
}

//小写!!
val Prop.nameSQL: String by KPropValue {
    val s = it.findAnnotation<Name>()?.value ?: it.name.lowerCased
    escSQLName(s)
}
val Prop.fullNameSQL: String by KPropValue {
    "${it.ownerClass!!.nameSQL}.${it.nameSQL}"
}

private fun escSQLName(name: String): String {
    if (name in mysqlKeySet) {
        return "`$name`"
    }
    return name
}

val String.trimSQL: String get() = this.trim('`', '\"')


class SQLArgs(val sql: String, val args: List<Any?> = emptyList())


fun PreparedStatement.setParams(params: List<Any?>) {
    for ((i, v) in params.withIndex()) {
        val vv: Any? = when (v) {
            is YsonNull -> null
            is YsonObject -> v.toString()
            is YsonArray -> v.toString()
            is YsonString -> v.data
            is YsonNum -> v.data
            is YsonBool -> v.data
            is YsonBlob -> v.data
            else -> v

        }
        this.setObject(i + 1, vv)
    }
}

fun Connection.exec(sa: SQLArgs): Boolean {
    return this.exec(sa.sql, sa.args)
}

fun Connection.exec(sql: String, args: List<Any?> = emptyList()): Boolean {
    if (ConnPick.enableLog) {
        logd(sql)
    }
    val st = this.prepareStatement(sql)
    st.setParams(args)

    return st.useX {
        try {
            it.execute()
        } catch (ex: Exception) {
            logd("SQL ERROR: ", sql, args)
            throw ex
        }
    }
}


fun Connection.query(sql: String, args: List<Any?>): ResultSet {
    if (ConnPick.enableLog) {
        logd(sql)
        logd(args)
    }
    val st = this.prepareStatement(sql)
    st.setParams(args)
    return st.executeQuery()
}


fun Connection.update(sql: String, args: List<Any?> = emptyList()): Int {
    if (ConnPick.enableLog) {
        logd(sql)
        logd(args)
    }
    val st = this.prepareStatement(sql)
    st.setParams(args)
    return st.useX {
        it.executeUpdate()
    }
}


inline fun Connection.trans(block: (Connection) -> Unit) {
    try {
        this.autoCommit = false
        block(this)
        this.commit()
    } catch (ex: Exception) {
        this.rollback()
        throw ex
    } finally {
        this.autoCommit = true
    }
}

private fun defineProcParam(p: KParameter): String {
    val inoutStr = when {
        p.hasAnnotation<ParamIn>() -> {
            "IN "
        }

        p.hasAnnotation<ParamOut>() -> {
            "OUT "
        }

        p.hasAnnotation<ParamInOut>() -> {
            "INOUT "
        }

        else -> ""
    }
    val pt = classToSQLType(p).second
    return inoutStr + p.userName + " " + pt
}

fun Connection.procDefine(proc: SQLProc) {
    val argStr = proc.valueParams.joinToString(",") { p ->
        defineProcParam(p)
    }
    val bd = proc.findAnnotation<SQLProcedure>()?.value ?: return
    var bodyStr = bd.trimIndent().trim()
    if (bodyStr.isNotEmpty()) {
        if (!bodyStr.endsWith(';')) {
            bodyStr += ";"
        }
    }
    val decl = """
				CREATE PROCEDURE ${proc.userName}($argStr)
				BEGIN
					$bodyStr
				END;
			""".trimIndent()
    update(decl)
}

private fun Connection.procStatement(proc: SQLProc, args: List<Any?>): CallableStatement {
    val procName = proc.userName
    if (ConnPick.enableLog) {
        logd("call $procName ", args.joinToString(","))
    }
    if (!procExist(procName)) {
        procDefine(proc)
    }
    val params = args.joinToString(",") { "?" }
    val st: CallableStatement = this.prepareCall("call $procName($params)")
    st.setParams(proc, args)
    return st
}

private fun CallableStatement.setParams(proc: SQLProc, args: List<Any?>) {
    for ((n, p) in proc.valueParams.withIndex()) {
        this.setObject(p.userName, args[n])
    }
}

fun Connection.procCall(proc: SQLProc, args: List<Any?>): LinkedHashMap<String, Any?> {
    val st = procStatement(proc, args)
    for (p in proc.valueParams.filter { it.hasAnnotation<ParamOut>() || it.hasAnnotation<ParamInOut>() }) {
        val t = classToSQLType(p).first
        st.registerOutParameter(p.userName, t)
    }
    st.execute()
    val map = LinkedHashMap<String, Any?>()
    for (p in proc.valueParams) {
        if (p.hasAnnotation<ParamOut>() || p.hasAnnotation<ParamInOut>()) {
            val paramName = p.userName
            map[paramName] = st.getObject(paramName)
        }
    }
    return map
}

fun Connection.procQuery(proc: SQLProc, args: List<Any?>): ResultSet {
    val st = procStatement(proc, args)
    return st.executeQuery()
}


private val SQLProc.connection: Connection
    get() {
        val cls: KClass<*> = this.ownerClass ?: error("No owner class")
        return cls.namedConnection
    }

fun SQLProc.procQuery(vararg args: Any): ResultSet {
    return this.connection.procQuery(this, args.toList())
}

fun SQLProc.procCall(vararg args: Any): LinkedHashMap<String, Any?> {
    return this.connection.procCall(this, args.toList())
}


private fun classToSQLType(p: KParameter): Pair<Int, String> {
    val dc = p.findAnnotation<Decimal>()
    if (dc != null) {
        return Types.DECIMAL to "DECIMAL(${dc.m},${dc.d})"
    }
    return when (p.type.classifier) {
        String::class -> Types.VARCHAR to "VARCHAR"
        Byte::class -> Types.TINYINT to "TINYINT"
        Short::class -> Types.SMALLINT to "SMALLINT"
        Int::class -> Types.INTEGER to "INT"
        Long::class -> Types.BIGINT to "BIGINT"
        Float::class -> Types.FLOAT to "FLOAT"
        Double::class -> Types.DOUBLE to "DOUBLE"
        BigDecimal::class -> Types.DECIMAL to "DECIMAL"
        else -> error("unknown procedure out parameter")
    }
}

fun Connection.funCreate(funText: String) {
    val s = funText.trimIndent().trim()
    logd("Create Function: ")
    logd(s)
    val st = this.prepareStatement(s)
    st.execute()
}

fun Connection.funNeeded(prop: KProperty0<String>) {
    if (funExist(prop.userName)) return
    funCreate(prop.get())
}



