package dev.entao.web.sql.ext

import dev.entao.web.base.Prop
import dev.entao.web.base.closeSafe
import dev.entao.web.base.returnClass
import dev.entao.web.sql.LT
import dev.entao.web.sql.OrmModel
import dev.entao.web.sql.delete
import dev.entao.web.sql.firstInt
import dev.entao.web.sql.firstLong
import dev.entao.web.sql.namedConnection
import dev.entao.web.sql.querySQL
import kotlin.reflect.KClass


object TableLimit {
	fun limitTable(cls: KClass<out OrmModel>, pk: Prop, maxRow: Int) {
		if (maxRow <= 0) {
			return
		}
		val c = cls.namedConnection
		val r = c.querySQL {
			select(pk)
			from(cls)
			orderBy(pk.DESC)
			limit(1, maxRow)
		}
		when (pk.returnClass) {
			Int::class -> {
				val n = r.firstInt() ?: return
				c.delete(cls, pk LT n)
			}
			Long::class -> {
				val n = r.firstLong() ?: return
				c.delete(cls, pk LT n)
			}
			else -> r.closeSafe()
		}

	}

}