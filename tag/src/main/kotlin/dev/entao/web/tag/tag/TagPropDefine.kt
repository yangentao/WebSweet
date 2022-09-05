package dev.entao.web.tag.tag

import dev.entao.web.base.userName
import kotlin.reflect.KProperty

internal typealias TagPropMap = LinkedHashMap<String, String>

object TagProp {
    operator fun <T> setValue(thisRef: Tag, property: KProperty<*>, value: T) {
        val k = property.userName
        when (value) {
            is String -> thisRef.setAttr(k, value)
            is Boolean -> {
                if (value) {
                    thisRef.setAttr(k, k)
                } else {
                    thisRef.removeAttr(k)
                }
            }

            else -> thisRef.setAttr(k, value.toString())
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> getValue(thisRef: Tag, property: KProperty<*>): T {
        val pname = property.userName
        val v = thisRef.getAttr(pname)
        return when (property.returnType.classifier) {
            String::class -> v as T
            Boolean::class -> (v == pname) as T
            Int::class -> {
                if (v.isEmpty()) {
                    0 as T
                } else {
                    v.toInt() as T
                }
            }

            else -> throw IllegalArgumentException("不支持的类型$property")
        }
    }
}

