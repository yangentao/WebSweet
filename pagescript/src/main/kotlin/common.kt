@file:Suppress("MemberVisibilityCanBePrivate", "FunctionName", "PropertyName", "EXPERIMENTAL_API_USAGE", "unused")

import org.w3c.dom.HTMLOptionsCollection
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.url.URLSearchParams

const val KB: Int = 1024
const val MB: Int = 1024 * 1024
const val GB: Int = 1024 * 1024 * 1024

fun HTMLSelectElement.fillOptions(url: String, textField: String = "text", valueField: String = "value", firstOption: String = "") {
    val eleSelect = this
    HttpGet(url).requestJsonResult { r ->
        if (r.OK) {
            eleSelect.options.clear()
            if (firstOption.isNotEmpty()) {
                eleSelect.option(firstOption, "")
            }
            for (kv in r.dataArray) {
                eleSelect.option(kv.getString(textField), kv[valueField].toString(), false)
            }
        }
    }
}

fun HTMLOptionsCollection.clear() {
    while (this.length > 0) {
        this.remove(0)
    }
}


// "1.2345".keep(2) => "1.23"
fun String.keep(n: Int): String {
    val idx = this.indexOf('.')
    if (idx < 0 || idx + n + 1 >= this.length) {
        return this
    }
    return this.substring(0, idx + n + 1)
}

//fun buildURL(url:String, )
class URLBuilder(urlString: String) {
    val url: String
    val searchParams: URLSearchParams

    init {
        val idx = urlString.indexOf('?')
        searchParams = if (idx > 0) {
            url = urlString.substring(0, idx)
            URLSearchParams(urlString.substring(idx + 1))
        } else {
            url = urlString
            URLSearchParams()
        }
    }

    fun append(name: String, value: String) {
        searchParams.append(name, value)
    }

    fun delete(name: String) {
        searchParams.delete(name)
    }

    fun get(name: String): String? {
        return searchParams.get(name)
    }

    fun getAll(name: String): Array<String> {
        return searchParams.getAll(name)
    }

    fun has(name: String): Boolean {
        return searchParams.has(name)
    }

    fun set(name: String, value: String) {
        searchParams.set(name, value)
    }

    override fun toString(): String {
        val p = searchParams.toString()
        return if (p.isEmpty()) {
            url
        } else {
            "$url?$p"
        }
    }
}


