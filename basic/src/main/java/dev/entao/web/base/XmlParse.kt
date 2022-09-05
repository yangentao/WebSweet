@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.entao.web.base

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


fun Node.dump() {
    val t = TransformerFactory.newInstance().newTransformer()
    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
    t.setOutputProperty(OutputKeys.INDENT, "yes")
    val w = StringWriter(1024)
    t.transform(DOMSource(this), StreamResult(w))
    val s = w.toString()
    println(s)
}

class NodeInfo(val node: Node) {

    fun element(tagName: String): NodeInfo? {
        if (node is Element) {
            val ls = node.getElementsByTagName(tagName) ?: return null
            if (ls.length > 0) {
                return NodeInfo(ls.item(0))
            }
        }
        return null
    }

    fun elements(tagName: String): List<NodeInfo> {
        val es = ArrayList<NodeInfo>()
        if (node is Element) {
            val ls = node.getElementsByTagName(tagName) ?: return es
            for (i in 0 until ls.length) {
                val e = ls.item(i)
                es += NodeInfo(e)
            }
        }
        return es
    }

    val elements: List<NodeInfo>
        get() {
            val ls = ArrayList<NodeInfo>()
            val nodes = node.childNodes
            for (i in 0 until nodes.length) {
                val node = nodes.item(i)
                if (node is Element) {
                    ls += NodeInfo(node)
                }
            }
            return ls
        }

    val nodes: List<NodeInfo>
        get() {
            val ls = ArrayList<NodeInfo>()
            val nodes = node.childNodes
            for (i in 0 until nodes.length) {
                val node = nodes.item(i)
                ls += NodeInfo(node)
            }
            return ls
        }

    fun attr(key: String): String? {
        if (node is Element) {
            return node.getAttribute(key)
        }
        return null
    }

    val text: String?
        get() {
            return this.node.textContent
        }
    //yyyy-MM-dd HH:mm:ss
    val textDateTime: Long
        get() {
            val s = this.text ?: return 0L
            return DateX.parseDateTime(s)?.longValue ?: 0L
        }
    //yyyy-MM-dd
    val textDate: Long
        get() {
            val s = this.text ?: return 0L
            return DateX.parseDate(s)?.longValue ?: 0L
        }
}

fun xmlParse(s: String): NodeInfo? {
    val fac = DocumentBuilderFactory.newInstance()
    val db = fac.newDocumentBuilder()
    val stream = ByteArrayInputStream(s.toByteArray(Charsets.UTF_8))
    val d = db.parse(stream) ?: return null
    val n = d.firstChild ?: return null
    return NodeInfo(n)
}


fun testXmlParse() {
    val a = xmlBuild("Person", "age" to 38) {
        element("child", "name" to "suo", "age" to 9) {
            element("school") {
                cdata("WenYuan")
            }
        }
        element("child", "name" to "Dou", "age" to 9) {
            +"Hello"
        }
    }
    val s = a.toXml(true, true)
    val n = xmlParse(s) ?: return

    println(n.attr("age"))
    println(n.element("child")?.attr("name"))
    println(n.element("child")?.element("school")?.text)

}

fun main() {
    testXmlParse()
}