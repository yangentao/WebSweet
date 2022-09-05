package dev.entao.web.base

/**
 * Created by entaoyang@163.com on 2018/3/20.
 */

object OS {

	val isWin: Boolean by lazy {
		val a = System.getProperty("os.name").lowerCased
		a.contains("windows")
	}
}