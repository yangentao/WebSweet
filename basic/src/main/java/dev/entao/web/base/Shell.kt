package dev.entao.web.base


/**
 * Created by entaoyang@163.com on 2018/3/19.
 */

object Shell {

	fun exec(s: String): Pair<Int, List<String>> {
		val p = Runtime.getRuntime().exec(s)
		p.waitFor()
		val n = p.exitValue()
		val ls = p.inputStream.bufferedReader().readLines()
		return n to ls
	}
}