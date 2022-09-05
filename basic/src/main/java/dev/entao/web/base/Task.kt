package dev.entao.web.base

import dev.entao.web.log.LogX
import dev.entao.web.log.loge
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


@Suppress("UNUSED_PARAMETER")
private fun uncaughtException(thread: Thread, ex: Throwable) {
    ex.printStackTrace()
    loge(ex)
    LogX.flush()
}

typealias CleanBlock = () -> Unit

object Task {
    private val cleanMap = LinkedHashMap<String, CleanBlock>()
    private val es: ScheduledExecutorService = Executors.newScheduledThreadPool(2) {
        val t = Thread(CleanRunnable(it))
        t.isDaemon = true
        t.priority = Thread.NORM_PRIORITY
        t.setUncaughtExceptionHandler(::uncaughtException)
        t
    }

    private class CleanRunnable(private val r: Runnable) : Runnable {
        override fun run() {
            try {
                r.run()
            } catch (ex: Throwable) {
                LogX.e(ex)
            } finally {
                if (cleanMap.isNotEmpty()) {
                    val ls = ArrayList<CleanBlock>(Task.cleanMap.values)
                    for (b in ls) {
                        try {
                            b()
                        } catch (ex: Throwable) {
                            LogX.e(ex)
                        }
                    }
                }
            }
        }
    }

    private class CleanCallback(private val callback: () -> Unit) : Runnable {
        override fun run() {
            try {
                callback()
            } catch (ex: Throwable) {
                LogX.e(ex)
            } finally {
                if (cleanMap.isNotEmpty()) {
                    val ls = ArrayList<CleanBlock>(Task.cleanMap.values)
                    for (b in ls) {
                        try {
                            b.invoke()
                        } catch (ex: Throwable) {
                            LogX.e(ex)
                        }
                    }
                }
            }
        }
    }

    val pool: ScheduledExecutorService get() = this.es

    fun setCleanBlock(name: String, block: CleanBlock) {
        cleanMap[name] = block
    }


    fun back(callback: () -> Unit): Future<*> {
        return es.submit(CleanCallback(callback))
    }

    fun afterMinutes(ms: Int, callback: () -> Unit): ScheduledFuture<*> {
        return es.schedule(CleanCallback(callback), ms.toLong(), TimeUnit.MINUTES)
    }

    fun afterSeconds(secs: Int, callback: () -> Unit): ScheduledFuture<*> {
        return es.schedule(CleanCallback(callback), secs.toLong(), TimeUnit.SECONDS)
    }
}

inline fun <R> sync(lock: Any, block: () -> R): R {
    return synchronized(lock, block)
}
