package dev.entao.web.core

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.MultipartConfig
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@MultipartConfig
@WebFilter(urlPatterns = ["/*"])
class MainFilter : Filter {
    private var service: HttpService? = null
    override fun init(filterConfig: FilterConfig) {
        service = HttpService(filterConfig)
        service?.onCreate()
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest && response is HttpServletResponse) {
            request.characterEncoding = "UTF-8"
            response.characterEncoding = "UTF-8"
            service?.service(request, response, chain)
        } else {
            chain.doFilter(request, response)
        }
    }

    override fun destroy() {
        service?.onDestroy()
        service = null
    }
}

