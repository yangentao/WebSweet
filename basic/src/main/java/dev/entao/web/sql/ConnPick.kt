@file:Suppress("MemberVisibilityCanBePrivate")

package dev.entao.web.sql

import java.sql.Connection
import javax.sql.DataSource
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


fun interface DataSourceDestroyCallback {
    fun onDestroy(source: DataSource)
}

class DataSourceInfo(val name: String, val source: DataSource, val destroyCallback: DataSourceDestroyCallback)

object ConnPick {
    var enableLog: Boolean = true
    var defaultConnectionName = "default"

    private val sourceMap: HashMap<String, DataSourceInfo> = HashMap()

    val connection: Connection
        get() = pick(null)

    @Synchronized
    fun pick(name: String?): Connection {
        if (sourceMap.isEmpty()) error("NO DataSouce registered!")
        if (name == null || name.isEmpty()) {
            if (sourceMap.size == 1) {
                return sourceMap.values.first().source.connection
            }
            if (defaultConnectionName.isNotEmpty()) {
                return sourceMap[defaultConnectionName]?.source?.connection ?: error("NO DataSource named: $defaultConnectionName!")
            }
            error("NO defaultConnectionName  providered.")
        } else {
            return sourceMap[name]?.source?.connection ?: error("NO DataSource named: $name!")
        }
    }

    @Synchronized
    fun register(info: DataSourceInfo) {
        sourceMap[info.name] = info
    }


    @Synchronized
    fun clean() {
        for (info in sourceMap.values) {
            info.destroyCallback.onDestroy(info.source)
        }
        sourceMap.clear()
    }


}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConnName(val value: String)

val KClass<*>.connectionName: String? get() = this.findAnnotation<ConnName>()?.value
val KClass<*>.namedConnection: Connection get() = ConnPick.pick(this.connectionName)


