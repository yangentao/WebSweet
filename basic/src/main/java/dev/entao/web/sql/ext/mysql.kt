@file:Suppress("unused")

package dev.entao.web.sql.ext

import dev.entao.web.log.logd
import dev.entao.web.sql.ColumnInfo
import dev.entao.web.sql.nameSQL
import dev.entao.web.sql.tableDesc
import java.sql.Connection
import java.sql.DriverManager

/**
 * Created by entaoyang@163.com on 2016/12/22.
 */

private const val DriverName = "com.mysql.cj.jdbc.Driver"

fun OpenMySQL(db: String, user: String, pwd: String, ip: String = "localhost", port: Int = 3306): Connection? {
	try {
		Class.forName(DriverName)
		return DriverManager.getConnection("jdbc:mysql://$ip:$port/$db", user, pwd)
	} catch (e: Exception) {
		e.printStackTrace()
	}
	return null
}

fun OpenUrl(url: String, user: String, pwd: String): Connection? {
	try {
		Class.forName(DriverName)
		return DriverManager.getConnection(url, user, pwd)
	} catch (e: Exception) {
		e.printStackTrace()
	}
	return null
}

fun main() {
	val con = OpenMySQL("safe", "safe", "safe") ?: return
	val ls = con.tableDesc("account")
	logd(ColumnInfo::COLUMN_NAME.nameSQL)
	ls.forEach {
		logd(it.COLUMN_NAME)
	}
}