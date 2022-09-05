package dev.entao.web.log

import dev.entao.web.base.closeSafe
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by entaoyang@163.com on 2016-10-28.
 */

@Suppress("unused")
class DirLogPrinter(
    private val logdir: File,
    private val keepDays: Int,
    private val tagSeprateFile: Boolean = false
) : LogPrinter {

    private val mapWriter = HashMap<String, BufferedWriter>()
    private val mapDay = HashMap<String, Int>()

    private var installed = false
    private val reg = Regex("\\d{4}-\\d{2}-\\d{2}.*\\.log")
    private var keyDefault = "XLOG"

    private val timer = Timer("LogDirTimer", true)
    private val timerTask = object : TimerTask() {
        override fun run() {
            mapWriter.forEach { (_, u) -> u.flush() }
        }

    }

    init {
        if (!logdir.exists()) {
            logdir.mkdirs()
        }
        timer.scheduleAtFixedRate(timerTask, 5000, 5000)
    }


    @Synchronized
    override fun install() {
        installed = true
    }

    @Synchronized
    override fun uninstall() {
        installed = false
        val m = HashMap<String, BufferedWriter>(mapWriter)
        mapWriter.clear()
        for ((_, v) in m) {
            v.flush()
            v.close()
        }
        m.clear()
    }

    @Synchronized
    override fun flush() {
        try {
            for (e in this.mapWriter) {
                e.value.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun keyOfTag(tag: String): String {
        return if (!tagSeprateFile || tag.isEmpty()) {
            this.keyDefault
        } else {
            tag
        }
    }

    @Synchronized
    private fun outOf(tag: String): BufferedWriter? {
        if (!installed) {
            return null
        }
        val tagKey = keyOfTag(tag)
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        if (mapDay[tagKey] == dayOfYear) {
            return mapWriter[tagKey]
        }
        val oldW = mapWriter.remove(tagKey)
        oldW?.flush()
        oldW?.close()

        deleteOldLogs()

        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val ds = fmt.format(Date(System.currentTimeMillis()))

        val filename: String = if (tagSeprateFile) {
            "$ds$tagKey.log"
        } else {
            "$ds.log"
        }
        try {
            val writer = BufferedWriter(FileWriter(File(logdir, filename), true), 16 * 1024)
            mapWriter[tagKey] = writer
            mapDay[tagKey] = dayOfYear
            return writer
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return null
    }

    private fun deleteOldLogs() {
        if (keepDays <= 0) {
            return
        }
        val n = keepDays
        val fs = logdir.listFiles() ?: return
        val ls = fs.filter { it.name.matches(reg) }.sortedByDescending { it.name }
        if (ls.size > n + 1) {
            for (i in (n + 1) until ls.size) {
                ls[i].delete()
            }
        }
    }


    override fun printItem(item: LogItem) {
        val w = outOf(item.tag) ?: return
        try {
            w.write(item.line)
            w.write("\n")
        } catch (e: IOException) {
            w.closeSafe()
            e.printStackTrace()
            mapWriter.remove(keyOfTag(item.tag))?.closeSafe()
        }
    }
}