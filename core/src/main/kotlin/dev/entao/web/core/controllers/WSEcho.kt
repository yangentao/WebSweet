@file:Suppress("unused", "UNUSED_PARAMETER")
package dev.entao.web.core.controllers

import dev.entao.web.base.DateX
import dev.entao.web.log.logd
import javax.websocket.CloseReason
import javax.websocket.EndpointConfig
import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.PathParam
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/wsecho/{ident}")
class WSEcho {

    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig, @PathParam("ident") ident: String) {
        val id = session.pathParameters["ident"]

        logd("WSHello.onOpen", ident, " id=$id")
    }

    @OnMessage
    fun onMessage(session: Session, message: String, @PathParam("ident") ident: String) {
        logd("onMessage: ", message, ident)
        session.basicRemote.sendText("ECHO: " + message + " " + DateX().formatTime())
    }

    @OnError
    fun onError(session: Session, t: Throwable, @PathParam("ident") ident: String) {
        logd("onError: ", t.localizedMessage)
        t.printStackTrace()
    }

    @OnClose
    fun onClose(session: Session, reason: CloseReason, @PathParam("ident") ident: String) {
        logd("onClose: ", reason.toString())
    }

}