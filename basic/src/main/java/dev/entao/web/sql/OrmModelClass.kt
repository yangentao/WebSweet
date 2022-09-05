@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.web.sql

import dev.entao.web.base.KClassValue
import dev.entao.web.base.Prop
import dev.entao.web.base.isPublic
import dev.entao.web.base.userName
import java.lang.Integer.min
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Created by entaoyang@163.com on 2017/4/5.
 */


open class OrmModelClass<T : OrmModel> {

    @Suppress("UNCHECKED_CAST")
    val tableClass: KClass<T> = javaClass.enclosingClass.kotlin as KClass<T>
    val conn: Connection get() = tableClass.namedConnection

    init {
        println("check table:${tableClass.userName}")
        TableMigrater(conn, tableClass)
    }

    val allProps: List<KMutableProperty<*>>
        get() {
            return this.tableClass._SQLProperties
        }


    @Suppress("UNCHECKED_CAST")
    open fun createModel(map: Map<String, Any?>): T {
        val m = tableClass.createInstance()
        m.model.putAll(map)
        return m
    }

    fun insert(vararg ps: Pair<Prop, Any?>): Boolean {
        return conn.insert(tableClass, ps.toList())
    }

    fun insertGenKey(vararg ps: Pair<Prop, Any?>): Long {
        return conn.insertGenKey(tableClass, ps.toList())
    }

    fun delete(w: Where?, vararg ws: Where): Int {
        return conn.delete(tableClass, AND_ALL(w, *ws))
    }

    fun deleteByKey(keyValue: Any): Int {
        return delete(keyEQ(keyValue))
    }

    fun updateByKey(keyValue: Any, vararg ps: Pair<Prop, Any?>): Int {
        return conn.update(tableClass, ps.toList(), keyEQ(keyValue))
    }

    fun update(map: Map<Prop, Any?>, w: Where?): Int {
        return conn.update(tableClass, map.map { it.key to it.value }, w)
    }

    fun update(p: Pair<Prop, Any?>, w: Where?): Int {
        return conn.update(tableClass, listOf(p), w)
    }

    fun update(p: Pair<Prop, Any?>, p2: Pair<Prop, Any?>, w: Where?): Int {
        return conn.update(tableClass, listOf(p, p2), w)
    }

    fun update(w: Where?, vararg ps: Pair<Prop, Any?>): Int {
        return conn.update(tableClass, ps.toList(), w)
    }

    fun update(vararg ps: Pair<Prop, Any?>, block: () -> Where?): Int {
        return conn.update(tableClass, ps.toList(), block())
    }


    fun keyEQ(pkValue: Any): Where {
        val pks = tableClass._PrimaryKeys
        assert(pks.size == 1)
        return pks.first() EQ pkValue
    }

    fun keysEQ(vararg pkValue: Any): Where {
        val pks = tableClass._PrimaryKeys
        assert(pks.isNotEmpty())
        var w: Where? = null
        val maxLen = min(pkValue.size, pks.size)
        for (i in 0 until maxLen) {
            w = w AND (pks[i] EQ pkValue[i])
        }
        return w!!
    }

    fun dumpTable() {
        conn.dump {
            select("*")
            from(tableClass)
        }
    }

    fun exist(vararg ws: Where): Boolean {
        return query {
            select("1")
            from(tableClass)
            where(*ws)
            limit(1)
        }.firstExists
    }

    fun maxBy(p: Prop, vararg ws: Where?): T? {
        return one(*ws) {
            orderBy(p.DESC)
        }
    }

    fun minBy(p: Prop, vararg ws: Where?): T? {
        return one(*ws) {
            orderBy(p.ASC)
            limit(1)
        }
    }

    fun oneByKey(pkValue: Any): T? {
        return this.one(keyEQ(pkValue))
    }

    fun one(vararg ws: Where?, block: SQLBuilder.() -> Unit = {}): T? {
        return list(*ws) {
            limit(1)
            block()
        }.firstOrNull()
    }


    fun list(vararg ws: Where?, block: SQLBuilder.() -> Unit = {}): List<T> {
        return query {
            from(tableClass)
            where(*ws)
            this.block()
        }.toListOrmModel { tableClass.createInstance() }
    }

    //单表
    fun query(block: SQLBuilder.() -> Unit): ResultSet {
        return conn.querySQL {
            from(tableClass)
            this.block()
        }
    }

    fun query(sa: String, args: List<Any?> = emptyList()): ResultSet {
        return conn.query(sa, args)
    }


    fun count(vararg ws: Where?): Int {
        return conn.countAll(tableClass, *ws)
    }

//	fun hasOne(relTable: KClass<*>, vararg ws: Where?, block: SQLBuilder.() -> Unit = {}): List<T> {
//		val thisPK = tableClass._PrimaryKeys.first()
//		val relFK = relTable._SQLProperties.first{ p ->
//			val fk = p.findAnnotation<ForeignKey>()
//			fk != null && fk.foreignTable == tableClass
//		}
//
//	}

    //table: user, goods, usergoods
    //goods.relateTo(usergoods::class, usergoods::userId EQ user_id)...
    //relateTable 关联表
    fun relateTo(relateTable: KClass<*>, vararg ws: Where?, block: SQLBuilder.() -> Unit = {}): List<T> {
        val thisPK = tableClass._PrimaryKeys.first()
        val relatePK = relateTable._PrimaryKeys.first {
            val fk = it.findAnnotation<ForeignKey>()
            fk != null && fk.foreignTable == tableClass
        }
        return this.query {
            select("${tableClass.nameSQL}.*")
            from(tableClass)
            join(relateTable)
            on { thisPK EQ relatePK }
            where(*ws)
            this.block()
        }.toListOrmModel { tableClass.createInstance() }
    }


    inline fun <reified R : Any> listCol(col: KProperty<R>, vararg ws: Where?): List<R> {
        return this.query {
            select(col)
            from(tableClass)
            where(*ws)
        }.toList {
            when (R::class) {
                String::class -> it.getString(1)
                Byte::class -> it.getByte(1)
                Short::class -> it.getShort(1)
                Int::class -> it.getInt(1)
                Long::class -> it.getLong(1)
                Float::class -> it.getFloat(1)
                Double::class -> it.getDouble(1)
                else -> error("Not support")
            } as R
        }
    }

    fun <R:Any> columnList(col: Prop, vararg ws: Where?, block: (ResultSet) -> R): List<R> {
        return this.query {
            select(col)
            from(tableClass)
            where(*ws)
        }.toList (block)
    }

    fun <R : Any> columnOneKey(col: Prop, keyVal: Any, block: (ResultSet) -> R): R? {
        return this.query {
            select(col)
            from(tableClass)
            where(keyEQ(keyVal))
            limit(1)
        }.firstRow(block)
    }

    fun columnOne(col: Prop, vararg ws: Where?): ResultSet {
        return this.query {
            select(col)
            from(tableClass)
            where(*ws)
            limit(1)
        }
    }

    fun columnInt(col: Prop, vararg ws: Where?): Int? {
        return columnOne(col, *ws).firstInt()
    }

    fun columnLong(col: Prop, vararg ws: Where?): Long? {
        return columnOne(col, *ws).firstLong()
    }

    fun columnDouble(col: Prop, vararg ws: Where?): Double? {
        return columnOne(col, *ws).firstDouble()
    }

    fun columnString(col: Prop, vararg ws: Where?): String? {
        return columnOne(col, *ws).firstString()
    }

    fun limitTable(maxRow: Int) {
        if (maxRow <= 0) {
            return
        }
        val pks = tableClass._PrimaryKeys
        assert(pks.size == 1)
        val pk = pks.first()
        assert(pk.returnType.classifier == Int::class || pk.returnType.classifier == Long::class)
        val r = this.query {
            select(pk)
            from(tableClass)
            orderBy(pk.DESC)
            limit(1, maxRow)
        }
        val n = r.firstLong() ?: return
        this.delete(pk LT n)
    }
}


val KClass<*>._SQLProperties: List<KMutableProperty<*>>
    get() {
        return classPropCache.getOrPut(this) {
            this.memberProperties.filter {
                if (it !is KMutableProperty<*>) {
                    false
                } else if (it.isAbstract || it.isConst || it.isLateinit) {
                    false
                } else if (!it.isPublic) {
                    false
                } else !it.isExcluded
            }.map { it as KMutableProperty<*> }
        }
    }

val KClass<*>._PrimaryKeys: List<KMutableProperty<*>> by KClassValue {
    it._SQLProperties.filter { p ->
        p.isPrimaryKey
    }
}

private val classPropCache = LinkedHashMap<KClass<*>, List<KMutableProperty<*>>>(64)



