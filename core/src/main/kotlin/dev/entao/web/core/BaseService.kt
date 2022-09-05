@file:Suppress("unused", "LocalVariableName", "MemberVisibilityCanBePrivate")

package dev.entao.web.core

import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class BaseService(val filterConfig: FilterConfig) {
    val servletContext: ServletContext get() = filterConfig.servletContext
    val contextPath: String get() = servletContext.contextPath


    open fun onCreate() {
    }


    open fun doGet(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    }


    open fun doPost(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {

    }

    open fun doPut(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    }

    open fun doDelete(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    }

    open fun doHead(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val responseNone = NoBodyResponse(response)
        doGet(request, responseNone, chain)
        responseNone.setContentLength()
    }

    open fun allowMethods(request: HttpServletRequest): Set<String> {
        return setOf(METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE)
    }

    fun allowCross(request: HttpServletRequest, response: HttpServletResponse) {
        val origin = request.header("Origin")
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin)
            response.setHeader("Access-Control-Allow-Credentials", "true")
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,HEAD,OPTIONS")
            response.setHeader(
                    "Access-Control-Allow-Headers",
                    "Origin,Accept,Content-Type,Content-Length,X-Requested-With,Key,Token,Authorization"
            )

        }
    }

    open fun doOptions(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val ls = ArrayList<String>()
        val set = allowMethods(request)
        if (METHOD_GET in set) {
            ls += METHOD_GET
            ls += METHOD_HEAD
        }
        if (METHOD_POST in set) ls += METHOD_POST
        if (METHOD_PUT in set) ls += METHOD_PUT
        if (METHOD_DELETE in set) ls += METHOD_DELETE

        ls += METHOD_OPTIONS
        ls += METHOD_TRACE

        response.setHeader("Allow", ls.joinToString(","))
    }

    open fun doTrace(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val responseLength: Int
        val CRLF = "\r\n"
        val buffer = StringBuilder("TRACE ").append(request.requestURI).append(" ").append(request.protocol)
        val reqHeaderEnum = request.headerNames
        while (reqHeaderEnum.hasMoreElements()) {
            val headerName = reqHeaderEnum.nextElement()
            buffer.append(CRLF).append(headerName).append(": ").append(request.getHeader(headerName))
        }
        buffer.append(CRLF)
        responseLength = buffer.length
        response.contentType = "message/http"
        response.setContentLength(responseLength)
        val out = response.outputStream
        out.print(buffer.toString())
    }

    @Throws(ServletException::class, IOException::class)
    internal open fun service(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        val method = req.method.uppercase()
        if (method == METHOD_GET) {
            val lastModified: Long = getLastModified(req)
            if (lastModified == -1L) {
                doGet(req, resp, chain)
            } else {
                val ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE)
                if (ifModifiedSince < lastModified) {
                    maybeSetLastModified(resp, lastModified)
                    doGet(req, resp, chain)
                } else {
                    resp.status = HttpServletResponse.SC_NOT_MODIFIED
                }
            }
        } else if (method == METHOD_HEAD) {
            val lastModified: Long = getLastModified(req)
            maybeSetLastModified(resp, lastModified)
            doHead(req, resp, chain)
        } else if (method == METHOD_POST) {
            doPost(req, resp, chain)
        } else if (method == METHOD_PUT) {
            doPut(req, resp, chain)
        } else if (method == METHOD_DELETE) {
            doDelete(req, resp, chain)
        } else if (method == METHOD_OPTIONS) {
            doOptions(req, resp, chain)
        } else if (method == METHOD_TRACE) {
            doTrace(req, resp, chain)
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Method NOT implemented")
        }
    }

    protected open fun getLastModified(req: HttpServletRequest): Long {
        return -1
    }

    private fun maybeSetLastModified(resp: HttpServletResponse, lastModified: Long) {
        if (resp.containsHeader(HEADER_LASTMOD)) return
        if (lastModified >= 0) resp.setDateHeader(HEADER_LASTMOD, lastModified)
    }

    open fun onDestroy() {
    }


    companion object {
        const val METHOD_DELETE = "DELETE"
        const val METHOD_HEAD = "HEAD"
        const val METHOD_GET = "GET"
        const val METHOD_OPTIONS = "OPTIONS"
        const val METHOD_POST = "POST"
        const val METHOD_PUT = "PUT"
        const val METHOD_TRACE = "TRACE"

        const val HEADER_IFMODSINCE = "If-Modified-Since"
        const val HEADER_LASTMOD = "Last-Modified"

        private val lStrings = ResourceBundle.getBundle("javax.servlet.http.LocalStrings")
    }
}
