package dev.entao.web.sql

import org.apache.tomcat.jdbc.pool.DataSourceProxy
import org.apache.tomcat.jdbc.pool.PoolProperties
import javax.naming.InitialContext
import javax.sql.DataSource

fun ConnPick.addSourceContextJDBC() {
    val ctx = InitialContext()
    val ls = ctx.list("java:comp/env/jdbc")
    while (ls.hasMore()) {
        val a = ls.next()
        val ds = ctx.lookup("java:comp/env/jdbc/${a.name}") as DataSource
        this.register(DataSourceInfo(a.name, ds) {
            if (it is DataSourceProxy) {
                it.close(true)
            }
        })
    }
}


fun ConnPick.addSourceMySQL(
    name: String,
    user: String,
    pwd: String,
    url: String,
    block: PoolProperties.() -> Unit = {}
) {
    val p = PoolProperties()
    p.username = user
    p.password = pwd
    p.url = url
    p.driverClassName = "com.mysql.cj.jdbc.Driver"
    p.isJmxEnabled = true
    p.isTestWhileIdle = false
    p.isTestOnBorrow = true
    p.validationQuery = "SELECT 1"
    p.validationInterval = 20000
    p.maxActive = 100
    p.initialSize = 2
    p.maxWait = 20000
    p.minIdle = 5
    p.removeAbandonedTimeout = 60
    p.isRemoveAbandoned = true
    p.jdbcInterceptors =
        "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer"
    p.block()

    val ds = org.apache.tomcat.jdbc.pool.DataSource()
    ds.poolProperties = p
    this.register(DataSourceInfo(name, ds) {
        it as org.apache.tomcat.jdbc.pool.DataSource
        it.close(true)
    })

}