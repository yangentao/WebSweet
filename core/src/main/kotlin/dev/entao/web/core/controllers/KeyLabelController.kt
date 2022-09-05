package dev.entao.web.core.controllers

import dev.entao.web.base.Name
import dev.entao.web.base.Trim
import dev.entao.web.core.HttpContext
import dev.entao.web.core.HttpController
import dev.entao.web.core.render.sendResult
import dev.entao.web.json.ysonObject
import dev.entao.web.sql.ConnPick
import dev.entao.web.sql.EQ
import dev.entao.web.sql.LIKE
import dev.entao.web.sql.Where
import dev.entao.web.sql.eachRow
import dev.entao.web.sql.querySQL

@Name("kv")
class KeyLabelController(context: HttpContext) : HttpController(context) {

	//select keycol, labelcol from tablename where labelcol like  '%searchtext%'  limit 100
	@dev.entao.web.core.Action
	fun search(tablename: String, keycol: String, labelcol: String = "", @Trim searchtext: String = "") {
		val w: Where? = if (searchtext.isNotEmpty()) {
			if (labelcol.isEmpty() || labelcol == keycol) {
				keycol LIKE "%$searchtext%"
			} else {
				labelcol LIKE "%$searchtext%"
			}
		} else null
		query(tablename, keycol, labelcol, w)
	}

	//select keycol, labelcol from tablename where onclo = onval  limit 100
	@dev.entao.web.core.Action
	fun eq(tablename: String, keycol: String, labelcol: String = "", oncol: String = "", onval: String = "") {
		val w: Where? = if (oncol.isNotEmpty() && onval.isNotEmpty()) {
			oncol EQ onval
		} else null
		query(tablename, keycol, labelcol, w)
	}

	fun query(tablename: String, keycol: String, labelcol: String = "", where: Where?) {
		val resultList = ArrayList<Pair<String, String>>()
		if (keycol == labelcol || labelcol.isEmpty()) {
			ConnPick.connection.querySQL {
				selectDistinct(keycol)
				from(tablename)
				where(where)
				orderBy(keycol.ASC)
				limit(100)
			}.eachRow {
				val v = it.getObject(keycol).toString()
				resultList += v to v
			}

		} else {
			ConnPick.connection.querySQL {
				selectDistinct(keycol, labelcol)
				from(tablename)
				where(where)
				orderBy(labelcol.ASC)
				limit(100)
			}.eachRow {
				val k = it.getObject(keycol).toString()
				val v = it.getObject(labelcol).toString()
				resultList += k to v
			}
		}
		context.sendResult {
			dataArray(resultList) { p ->
				ysonObject {
					"key" TO p.first
					"label" TO p.second
				}
			}
		}
	}

}