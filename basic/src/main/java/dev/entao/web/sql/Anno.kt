package dev.entao.web.sql

import dev.entao.web.base.Exclude
import dev.entao.web.base.Prop
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Created by yangentao on 2016/12/14.
 */
//@Target(AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class ModelTable(val name: String = "", val connName: String = "")

//@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class ModelField(val name: String = "", val autoInc: Boolean = false, val unique: String = "")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SQLFunction(val value: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SQLProcedure(val value: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParamIn

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParamOut

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParamInOut


//主键
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrimaryKey


//外键
//@RefModel(Person::class,  "name")
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ForeignKey(val foreignTable: KClass<out OrmModel>, val displayField: String = "", val where: String = "")

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RowsFilter(val where: String, val orderBy: String = "")


//自增, 仅用于整形主键
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoInc(val value: Int = 1)


//是否唯一约束
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Unique(val value: String = "")

//是否在该列建索引
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Index

//是否非空
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotNull


//自动创建表
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoCreateTable(val value: Boolean = true)


//@Decimal(11, 2, "%06.2f")
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Decimal(val m: Int = 11, val d: Int = 2, val format: String = "")


val KProperty<*>.isExcluded: Boolean
    get() {
        return this.findAnnotation<Exclude>() != null
    }
val KProperty<*>.isPrimaryKey: Boolean
    get() {
        return this.findAnnotation<PrimaryKey>() != null
    }

fun Decimal.display(v: Any): String {
    return when (v) {
        is Float, is Double -> String.format("%.${d}f", v)
        else -> v.toString()
    }
}

fun main() {
    val v = 123.456
    val s = String.format("%05.0f", v)
    println(s)
}

val ForeignKey.keyProp: KProperty<*> get() = this.foreignTable._PrimaryKeys.first()
val ForeignKey.displayProp: Prop get() = this.foreignTable._SQLProperties.first { it.name == this.displayField }

val Prop.refModel: ForeignKey?
    get() {
        val r = this.findAnnotation<ForeignKey>()
        r?.foreignTable?.java
        return r
    }


fun ForeignKey.display(v: Any): String {
    val tableName = this.foreignTable.nameSQL
    val keyCol = this.keyProp.nameSQL
    val labelCol = this.displayField
    val where = this.where
    val w: Where? = if (where.isEmpty()) null else Where(where)
    if (keyCol == labelCol || labelCol.isEmpty()) {
        return ConnPick.connection.querySQL {
            selectDistinct(keyCol)
            from(tableName)
            where(w, keyCol EQ v)
            orderBy(keyCol.ASC)
            limit(1)
        }.firstRow {
            it.getObject(keyCol).toString()
        } ?: v.toString()
    } else {
        return ConnPick.connection.querySQL {
            select(keyCol, labelCol)
            from(tableName)
            where(w, keyCol EQ v)
            orderBy(keyCol.ASC)
            limit(1)
        }.firstRow {
            it.getObject(labelCol).toString()
        } ?: v.toString()
    }
}

fun ForeignKey.toMap(): Map<String, String> {
    val map = LinkedHashMap<String, String>()
    val tableName = this.foreignTable.nameSQL
    val keyCol = this.keyProp.nameSQL
    val labelCol = this.displayField
    val where = this.where
    val w: Where? = if (where.isEmpty()) null else Where(where)
    if (keyCol == labelCol || labelCol.isEmpty()) {
        ConnPick.connection.querySQL {
            selectDistinct(keyCol)
            from(tableName)
            where(w)
            orderBy(keyCol.ASC)
            limit(100)
        }.toListOrmMap().forEach {
            val k = it[keyCol]
            if (k != null) {
                map[k.toString()] = k.toString()
            }
        }
    } else {
        ConnPick.connection.querySQL {
            selectDistinct(keyCol, labelCol)
            from(tableName)
            where(w)
            orderBy(labelCol.ASC)
            limit(100)
        }.toListOrmMap().forEach {
            val k = it[keyCol]
            val v = it[labelCol]
            if (k != null && v != null) {
                map[k.toString()] = v.toString()
            }
        }
    }
    return map
}
