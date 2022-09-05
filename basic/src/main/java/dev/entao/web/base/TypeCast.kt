package dev.entao.web.base

import dev.entao.web.json.YsonArray
import dev.entao.web.json.YsonObject
import java.math.BigDecimal
import java.net.URI
import java.net.URL
import java.sql.Time
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation


@Suppress("unused")
fun addTypeCaster(c: ITypeCast) {
    propCastMap[c.thisClass] = c
}

fun typeCastContains(cls: KClass<*>?): Boolean {
    if (cls == null) return false
    return cls in propCastMap.keys
}

val KClass<*>.caster: ITypeCast
    get() {
        return propCastMap[this] ?: error("not support: $this ")
    }

val KProperty<*>.caster: ITypeCast
    get() {
        return propCastMap[this.returnType.classifier] ?: error("not support: $this ")
    }

val KParameter.caster: ITypeCast
    get() {
        return propCastMap[this.type.classifier] ?: error("not support: $this ")
    }

@Suppress("UNCHECKED_CAST")
fun <V> KProperty<*>.decodeAndCast(value: Any?): V {
    val c = this.caster
    if (value != null) {
        c.decodeFromAny(value, this.annotations)?.also { return it as V }
    }
    if (this.returnType.isMarkedNullable) {
        return c.decodeNull(this) as V
    }
    return (c.decodeNull(this) ?: c.defaultValue) as V
}

@Suppress("UNCHECKED_CAST")
fun KParameter.decodeAndCast(value: Any?): Any? {
    val c = this.caster
    if (value != null) {
        c.decodeFromAny(value, this.annotations)?.also { return it }
    }
    if (this.type.isMarkedNullable) {
        return c.decodeNull(this)
    }
    return c.decodeNull(this) ?: c.defaultValue
}

fun KProperty<*>.encodeToString(value: Any?): String? {
    if (value == null) {
        return null
    }
    return this.caster.encodeToString(value, this.annotations)
}

fun KParameter.encodeToString(value: Any?): String? {
    if (value == null) {
        return null
    }
    return this.caster.encodeToString(value, this.annotations)
}


fun ITypeCast.decodeNull(prop: Prop): Any? {
    return prop.findAnnotation<NullValue>()?.value?.let {
        decodeFromAny(it, prop.annotations)
    }
}

fun ITypeCast.decodeNull(param: KParameter): Any? {
    return param.findAnnotation<NullValue>()?.value?.let {
        decodeFromAny(it, param.annotations)
    }
}

private val castList: List<ITypeCast> = listOf(
    StringPropCast, BoolPropCast,
    DoublePropCast, FloatPropCast, LongPropCast, IntPropCast, ShortPropCast, BytePropCast,
    UUIDPropCast, URLPropCast, URIPropCast,
    YsonArrayPropCast, YsonObjectPropCast,
    DateUPropCast, DateSQLPropCast, TimePropCast, TimestampPropCast,
    LocalTimePropCast, LocalDatePropCast, LocalDateTimePropCast
)
private val propCastMap: HashMap<KClass<*>, ITypeCast> by lazy {
    val map = HashMap<KClass<*>, ITypeCast>()
    for (a in castList) {
        map[a.thisClass] = a
    }
    map
}

interface ITypeCast {
    val thisClass: KClass<*>
    val defaultValue: Any
    fun encodeToString(value: Any, annoList: List<Annotation>): String {
        return value.toString()
    }

    fun decodeFromAny(value: Any, annoList: List<Annotation>): Any?
}

abstract class TypeCast(override val thisClass: KClass<*>) : ITypeCast {

    override fun decodeFromAny(value: Any, annoList: List<Annotation>): Any? {
        if (thisClass == value::class) {
            return value
        }
        return decodeOtherType(value, annoList)
    }

    //类型不同
    protected abstract fun decodeOtherType(value: Any, annoList: List<Annotation>): Any?
}


private object LocalDateTimePropCast : TypeCast(LocalDateTime::class) {
    override val defaultValue: LocalDateTime get() = LocalDateTime.of(0, 0, 0, 0, 0, 0)

    override fun encodeToString(value: Any, annoList: List<Annotation>): String {
        return (value as LocalDateTime).format(annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_DATE_TIME)
    }

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): LocalDateTime? {
        return when (value) {
            is Time -> LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault())
            is Timestamp -> value.toLocalDateTime()
            is DateSQL -> LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault())
            is DateUtil -> LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault())
            is LocalDate -> value.atTime(0, 0)
            is LocalTime -> value.atDate(LocalDate.of(1970, 1, 1))
            is String -> {
                if (value.isEmpty()) return null
                val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_DATE_TIME
                LocalDateTime.parse(value, DateTimeFormatter.ofPattern(fmt))
            }
            else -> error("Unsupport value: $thisClass, $value  ${value::class}")
        }
    }
}

private object LocalDatePropCast : TypeCast(LocalDate::class) {
    override val defaultValue: LocalDate get() = LocalDate.of(0, 0, 0)

    override fun encodeToString(value: Any, annoList: List<Annotation>): String {
        return (value as LocalDate).format(annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_DATE)
    }

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): LocalDate? {
        return when (value) {
            is Timestamp -> value.toLocalDateTime().toLocalDate()
            is DateSQL -> LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()).toLocalDate()
            is DateUtil -> LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()).toLocalDate()
            is LocalDateTime -> value.toLocalDate()
            is String -> {
                if (value.isEmpty()) return null
                val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_DATE
                LocalDate.parse(value, DateTimeFormatter.ofPattern(fmt))
            }
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }

    }
}

private object LocalTimePropCast : TypeCast(LocalTime::class) {
    override val defaultValue: LocalTime get() = LocalTime.of(0, 0, 0)

    override fun encodeToString(value: Any, annoList: List<Annotation>): String {
        val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_TIME
        return DateTimeFormatter.ofPattern(fmt).format(value as LocalTime)
    }

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): LocalTime? {
        return when (value) {
            is Time -> value.toLocalTime()
            is Timestamp -> value.toLocalDateTime().toLocalTime()
            is DateSQL -> LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()).toLocalTime()
            is DateUtil -> LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()).toLocalTime()
            is LocalDateTime -> value.toLocalTime()
            is String -> {
                if (value.isEmpty()) return null
                val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_TIME
                LocalTime.parse(value, DateTimeFormatter.ofPattern(fmt))
            }
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object DateUPropCast : TypeCast(DateUtil::class) {
    override val defaultValue: DateUtil get() = DateUtil(0)

    override fun encodeToString(value: Any, annoList: List<Annotation>): String {
        val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_DATE
        return SimpleDateFormat(fmt).format(value as DateUtil)
    }

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): DateUtil? {
        return when (value) {
            is Time -> DateUtil(value.time)
            is Timestamp -> DateUtil(value.time)
            is DateSQL -> DateUtil(value.time)
            is LocalDate -> DateUtil(value.millSeconds)
            is LocalDateTime -> DateUtil(value.millSeconds)
            is String -> {
                if (value.isEmpty()) return null
                val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_DATE
                DateX.parse(fmt, value)?.date
            }
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object DateSQLPropCast : TypeCast(DateSQL::class) {
    override val defaultValue: DateSQL get() = DateSQL(0)

    override fun encodeToString(value: Any, annoList: List<Annotation>): String {
        val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_DATE
        return SimpleDateFormat(fmt).format(value as DateSQL)
    }

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): DateSQL? {
        return when (value) {
            is Time -> DateSQL(value.time)
            is Timestamp -> DateSQL(value.time)
            is DateUtil -> DateSQL(value.time)
            is LocalDateTime -> DateSQL(value.millSeconds)
            is LocalDate -> DateSQL(value.millSeconds)
            is String -> {
                if (value.isEmpty()) return null
                val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_DATE
                DateX.parse(fmt, value)?.dateSQL
            }
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}


private object TimePropCast : TypeCast(Time::class) {
    override val defaultValue: Time get() = Time(0)

    override fun encodeToString(value: Any, annoList: List<Annotation>): String {
        val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_TIME
        return SimpleDateFormat(fmt).format(value as Time)
    }

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): Time? {
        return when (value) {
            is Timestamp -> Time(value.time)
            is DateSQL -> Time(value.time)
            is DateUtil -> Time(value.time)
            is LocalDateTime -> Time(value.millSeconds)
            is LocalTime -> Time(value.atDate(LocalDate.of(1970, 1, 1)).millSeconds)
            is String -> {
                if (value.isEmpty()) return null
                val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_TIME
                DateX.parse(fmt, value)?.time
            }
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}


private object TimestampPropCast : TypeCast(Timestamp::class) {
    override val defaultValue: Timestamp get() = Timestamp(0)

    override fun encodeToString(value: Any, annoList: List<Annotation>): String {
        val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_DATE_TIME
        return SimpleDateFormat(fmt).format(value as Timestamp)
    }

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): Timestamp? {
        return when (value) {
            is Time -> Timestamp(value.time)
            is DateSQL -> Timestamp(value.time)
            is DateUtil -> Timestamp(value.time)
            is LocalDateTime -> Timestamp(value.millSeconds)
            is LocalDate -> Timestamp(value.millSeconds)
            is String -> {
                if (value.isEmpty()) return null
                val fmt = annoList.firstNotNullOfOrNull { it as? DatePattern }?.format ?: DateX.FORMAT_DATE_TIME
                DateX.parse(fmt, value)?.timestamp
            }
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object YsonObjectPropCast : TypeCast(YsonObject::class) {
    override val defaultValue: YsonObject get() = YsonObject()

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): YsonObject? {
        return when (value) {
            is String -> if (value.isNotEmpty()) YsonObject(value) else null
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object YsonArrayPropCast : TypeCast(YsonArray::class) {
    override val defaultValue: YsonArray get() = YsonArray()

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): YsonArray? {
        return when (value) {
            is String -> if (value.isNotEmpty()) YsonArray(value) else null
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object UUIDPropCast : TypeCast(UUID::class) {
    override val defaultValue: UUID get() = UUID(0, 0)

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): UUID? {
        return when (value) {
            is String -> if (value.isNotEmpty()) UUID.fromString(value) else null
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object URLPropCast : TypeCast(URL::class) {
    override val defaultValue: URL get() = URL("http", "localhost", "/")

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): URL? {
        return when (value) {
            is String -> if (value.isNotEmpty()) URL(value) else null
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object StringPropCast : TypeCast(String::class) {
    override val defaultValue: String get() = ""

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): String {
        return value.toString()
    }
}

private object LongPropCast : TypeCast(Long::class) {
    override val defaultValue: Long get() = 0L

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): Long? {
        return when (value) {
            is Int -> value.toLong()
            is Short -> value.toLong()
            is Byte -> value.toLong()
            is String -> if (value.isNotEmpty()) value.toLong() else null
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object IntPropCast : TypeCast(Int::class) {
    override val defaultValue: Int get() = 0

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): Int? {
        return when (value) {
            is Long -> value.toInt()
            is Short -> value.toInt()
            is Byte -> value.toInt()
            is String -> if (value.isNotEmpty()) value.toInt() else null
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object ShortPropCast : TypeCast(Short::class) {
    override val defaultValue: Short get() = 0

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): Short? {
        return when (value) {
            is Long -> value.toShort()
            is Int -> value.toShort()
            is Byte -> value.toShort()
            is String -> if (value.isNotEmpty()) value.toShort() else null
            else -> error("cast type error: $thisClass,  $value ")
        }
    }
}

private object BytePropCast : TypeCast(Byte::class) {
    override val defaultValue: Byte get() = 0

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): Byte? {
        return when (value) {
            is Long -> value.toByte()
            is Int -> value.toByte()
            is Short -> value.toByte()
            is String -> if (value.isNotEmpty()) value.toByte() else null
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}


private object FloatPropCast : TypeCast(Float::class) {
    override val defaultValue: Float get() = 0f


    override fun decodeOtherType(value: Any, annoList: List<Annotation>): Float? {
        return when (value) {
            is Double -> value.toFloat()
            is Long -> value.toFloat()
            is Int -> value.toFloat()
            is Short -> value.toFloat()
            is Byte -> value.toFloat()
            is BigDecimal -> value.toFloat()
            is String -> if (value.isNotEmpty()) value.toFloat() else null
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object DoublePropCast : TypeCast(Double::class) {
    override val defaultValue: Double get() = 0.0

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): Double? {
        return when (value) {
            is Float -> value.toDouble()
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is Short -> value.toDouble()
            is Byte -> value.toDouble()
            is BigDecimal -> value.toDouble()
            is String -> if (value.isNotEmpty()) value.toDouble() else null
            else -> error("cast type error: $thisClass,  $value ${value::class}")
        }
    }
}

private object BoolPropCast : TypeCast(Boolean::class) {
    override val defaultValue: Boolean get() = false

    private val trueSet = setOf("1", "true", "on")

    override fun encodeToString(value: Any, annoList: List<Annotation>): String {
        value as Boolean
        return if (value) "1" else "0"
    }

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): Boolean? {
        return when (value) {
            is Number -> value == 1
            is String -> if (value.isNotEmpty()) value in trueSet else null
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}

private object URIPropCast : TypeCast(URI::class) {
    override val defaultValue: URI get() = URI("http://localhost/")

    override fun decodeOtherType(value: Any, annoList: List<Annotation>): URI? {
        return when (value) {
            is String -> if (value.isNotEmpty()) URI(value) else null
            else -> error("cast type error: $thisClass,  $value  ${value::class}")
        }
    }
}