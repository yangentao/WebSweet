package dev.entao.web.base

/**
 * Created by entaoyang@163.com on 2016/12/20.
 */

val PROGRESS_DELAY = 100

interface Progress {
    fun onStart(total: Int)

    fun onProgress(current: Int, total: Int, percent: Int)

    fun onFinish(success: Boolean)
}
