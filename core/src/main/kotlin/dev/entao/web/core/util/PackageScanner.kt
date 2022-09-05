package dev.entao.web.core.util

import java.io.File
import java.net.JarURLConnection
import java.util.jar.JarFile

class PackageScanner(val pkg: String) {
    private val classNameList = ArrayList<String>()
    private val loader: ClassLoader = this::class.java.classLoader

    fun dump() {
        for (s in classNameList) {
            println(s)
        }
    }

    val classFullNames: List<String> get() = ArrayList(classNameList)
    val classList: List<Class<*>> get() = classNameList.map { loader.loadClass(it) }

    fun scan(): PackageScanner {
        classNameList.clear()
        val url = loader.getResource(pkg.replace('.', File.separatorChar)) ?: return this
        if (url.protocol == "file") {
            scanFile(pkg, File(url.toURI()))
        } else if (url.protocol == "jar") {
            scanJar((url.openConnection() as JarURLConnection).jarFile)
        }
        return this
    }

    private fun scanFile(currentPkg: String, file: File) {
        file.listFiles { ff: File ->
            if (ff.isFile && ff.name.endsWith(".class")) {
                classNameList += currentPkg + "." + ff.name.substringBeforeLast(".class")
            } else if (ff.isDirectory) {
                scanFile(currentPkg + "." + ff.name, ff)
            }
            false
        }
    }

    private fun scanJar(jf: JarFile) {
        val p = pkg.replace('.', '/')
        for (e in jf.entries()) {
            if (e.name.startsWith(p) && e.name.endsWith(".class")) {
                classNameList += e.name.replace('/', '.').substringBeforeLast(".class")
            }
        }
    }
}

fun main() {
    val a = PackageScanner("dev.entao.core.util")
    a.scan()
    a.dump()
}