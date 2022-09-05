package dev.entao.web.core.slices

import dev.entao.web.base.Comment
import dev.entao.web.base.DateX
import dev.entao.web.base.MIN
import dev.entao.web.base.safe
import dev.entao.web.base.toArrayList
import dev.entao.web.log.LogX
import java.util.*


//每小时调用一次
fun interface TimerHourCallback {
    fun onTimerHour(hour: Int)
}

//每分钟调用一次
fun interface TimerMinuteCallback {
    fun onTimerMinute(@Comment(">=1") minute: Int)
}

//每分钟调用一次
fun interface TimerCallback {
    fun onTimer(@Comment("[1-31]") dayOfMonth: Int, @Comment("[0,23]") hour: Int, @Comment("[0,59]") minute: Int)
}

class TimerProvider {

    private var timer: Timer = Timer("everyMinute", true)
    private val timerList = ArrayList<TimerCallback>()
    private val hourList = ArrayList<TimerHourCallback>()
    private val minuteList = ArrayList<TimerMinuteCallback>()


    private val tmtask = object : TimerTask() {

        private var minN: Int = 0
        private var preHour = -1

        override fun run() {
            val date = DateX()
            val day = date.day
            val h = date.hour
            val minute = date.minute

            timerList.toArrayList().forEach {
                safe {
                    it.onTimer(day, h, minute)
                }
            }

            if (h != preHour) {
                preHour = h
                hourList.toArrayList().forEach {
                    safe {
                        it.onTimerHour(h)
                    }
                }
            }

            val n = minN++
            minuteList.toArrayList().forEach {
                safe {
                    it.onTimerMinute(n)
                }
            }
            safe {
                LogX.flush()
            }
        }
    }

    init {
        timer.scheduleAtFixedRate(tmtask, 1.MIN, 1.MIN)
    }

    fun destroy() {
        timer.cancel()
    }

    fun onMinute(callback: TimerMinuteCallback) {
        this.minuteList += callback
    }

    fun onHour(callback: TimerHourCallback) {
        this.hourList += callback
    }

    fun onTimer(callback: TimerCallback) {
        this.timerList += callback
    }
}
