@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.web.log

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by entaoyang@163.com on 2018/11/8.
 */
enum class LogLevel(val value: Int) {
    DEBUG(0), VERBOSE(1), INFO(2), WARN(3), ERROR(4), FATAIL(5);
}

data class LogItem(val level: LogLevel, val tag: String, val message: String) {
    val line: String = makeLine()

    private fun makeLine(): String {
        val sb = StringBuilder(message.length + 64)
        sb.append("TIM:")
        val date =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(System.currentTimeMillis()))
        sb.append(date)
        sb.append(" TID:")
        sb.append(String.format(Locale.getDefault(), "%6d", Thread.currentThread().id))
        sb.append(" LVL:")
        sb.append(level.name.first())
        sb.append(" TAG:")
        sb.append(tag)
        sb.append(" MSG:")
        sb.append(message)
        return sb.toString()
    }

}

object LogX {
    private val printer = LogTree(ConsoleLogPrinter())
    var enabled = true
    var TAG: String = "xlog"

    fun setPrinter(p: LogPrinter) {
        printer.clear()
        addPrinter(p)
    }

    @Synchronized
    fun addPrinter(p: LogPrinter) {
        printer.add(p)
    }

    @Synchronized
    fun clearPrinter() {
        printer.clear()
    }

    @Synchronized
    fun removePrinter(p: LogPrinter) {
        printer.remove(p)
    }


    @Synchronized
    fun flush() {
        printer.flush()
    }

    @Synchronized
    fun printItem(item: LogItem) {
        if (enabled) {
            printer.printItem(item)
        }
    }

    fun printItem(level: LogLevel, tag: String, vararg args: Any?) {
        printItem(LogItem(level, tag, anyArrayToString(args)))
    }

    fun d(vararg args: Any?) {
        printItem(LogItem(LogLevel.DEBUG, TAG, anyArrayToString(args)))
    }

    fun w(vararg args: Any?) {
        printItem(LogItem(LogLevel.WARN, TAG, anyArrayToString(args)))
    }

    fun e(vararg args: Any?) {
        printItem(LogItem(LogLevel.ERROR, TAG, anyArrayToString(args)))
        flush()
    }

    fun i(vararg args: Any?) {
        printItem(LogItem(LogLevel.INFO, TAG, anyArrayToString(args)))
    }

    fun fatal(vararg args: Any?) {
        printItem(LogItem(LogLevel.FATAIL, TAG, anyArrayToString(args)))
        flush()
        throw RuntimeException("fatal error!")
    }


    fun dx(tag: String, vararg args: Any?) {
        printItem(LogItem(LogLevel.DEBUG, tag, anyArrayToString(args)))
    }

    fun wx(tag: String, vararg args: Any?) {
        printItem(LogItem(LogLevel.WARN, tag, anyArrayToString(args)))
    }

    fun ex(tag: String, vararg args: Any?) {
        printItem(LogItem(LogLevel.ERROR, tag, anyArrayToString(args)))
        flush()
    }

    fun ix(tag: String, vararg args: Any?) {
        printItem(LogItem(LogLevel.INFO, tag, anyArrayToString(args)))
    }

    fun fatalX(tag: String, vararg args: Any?) {
        printItem(LogItem(LogLevel.FATAIL, tag, anyArrayToString(args)))
        flush()
        throw RuntimeException("fatal error!")
    }
}

interface LogPrinter {
    fun printItem(item: LogItem)
    fun flush() {}
    fun uninstall() {}
    fun install() {}
}

class LogTree(vararg ps: LogPrinter) : LogPrinter {
    private val list: ArrayList<LogPrinter> = arrayListOf(*ps)
    override fun printItem(item: LogItem) {
        for (p in list) {
            p.printItem(item)
        }
    }

    fun clear() {
        for (p in list) {
            p.flush()
            p.uninstall()
        }
        list.clear()
    }

    fun add(p: LogPrinter) {
        p.install()
        list.add(p)
    }

    fun remove(p: LogPrinter) {
        list.remove(p)
        p.uninstall()
    }

    override fun flush() {
        for (p in list) {
            p.flush()

        }
    }

    override fun uninstall() {
        for (p in list) {
            p.uninstall()

        }
    }

    override fun install() {
        for (p in list) {
            p.install()

        }
    }

}

class ConsoleLogPrinter : LogPrinter {
    override fun printItem(item: LogItem) {
        println(item.line)
    }

}