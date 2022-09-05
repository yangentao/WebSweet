package dev.entao.web.core.render

import dev.entao.web.base.NodeBuild
import dev.entao.web.core.HttpContext
import javax.xml.parsers.DocumentBuilderFactory

class XMLRender(context: HttpContext) : Render(context) {
    var xmlDeclare: Boolean = true
    var indent: Boolean = true

    private lateinit var rootElement: NodeBuild

    fun root(rootName: String, vararg attrPairs: Pair<String, Any>, block: NodeBuild.() -> Unit): NodeBuild {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val e = doc.createElement(rootName)
        doc.appendChild(e)
        val n = NodeBuild(e)
        for (p in attrPairs) {
            n.attr(p.first, p.second.toString())
        }
        rootElement = n
        n.block()
        return n
    }

    override fun onSend() {
        val xml = rootElement.toXml(xmlDeclare, indent)
        context.sendXML(xml)
    }

}


