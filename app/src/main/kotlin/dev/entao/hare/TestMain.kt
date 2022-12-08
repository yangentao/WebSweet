package dev.entao.hare

import dev.entao.web.http.httpMultipart
import dev.entao.web.log.logd


fun main() {

    val h = httpMultipart("http://localhost:8080/app/account/addjson"){
        "name" arg "yang"
        "age" arg 123
    }
    logd(h.OK, h.valueText)
}