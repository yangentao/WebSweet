package dev.entao.web.core

import dev.entao.web.base.*
import dev.entao.web.core.render.sendBack
import dev.entao.web.core.render.sendFailed
import dev.entao.web.log.logd
import dev.entao.web.log.loge
import java.io.File
import javax.servlet.http.Part
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * Created by entaoyang@163.com on 2018/4/2.
 */

open class SweetError(msg: String) : RuntimeException(msg)
private class ParamError(val param: KParameter, val msg: String = "") : SweetError("Parameter Error : ${param.userName},  $msg")

private fun KParameter.err(msg: String): Nothing {
    throw ParamError(this, msg)
}

private fun KParameter.errorUnsupport(msg: String = "参数不被支持"): Nothing {
    this.err(msg)
}

private fun KParameter.errorMiss(msg: String = "参数缺失"): Nothing {
    this.err(msg)
}

class FilePart(val file: File, val part: Part)

//function 必须是类的成员, 不能是全局函数
class Router(val uri: String, val cls: KClass<*>, val function: KFunction<*>, obj: Any? = null) {

    private val inst: Any? = obj ?: function.ownerObject

    val allowMethods: Set<String> by lazy {
        val mSet = HashSet<String>()
        function.findAnnotation<HttpMethod>()?.also { m ->
            mSet += m.value.map { it.uppercase() }
        }
        if (mSet.isEmpty()) {
            cls.findAnnotation<HttpMethod>()?.also { m ->
                mSet += m.value.map { it.uppercase() }
            }
        }
        if (mSet.isEmpty()) {
            mSet += BaseService.METHOD_GET
            mSet += BaseService.METHOD_POST
            mSet += BaseService.METHOD_PUT
            mSet += BaseService.METHOD_DELETE
        }
        mSet
    }

    val functionFullName: String get() = "${cls.qualifiedName}.${function.name}"


    override fun toString(): String {
        return "Router{$uri, $cls, ${function.name}"
    }

    fun dispatch(context: HttpContext) {
        val a = inst ?: cls.createInstanceX(context)
        try {
            val map = prepareParams(context, a, function)
            function.callBy(map)
        } catch (ex: Exception) {
            loge(ex.realReason.message)
            ex.printStackTrace()
            if (context.hasReferer) {
                if (ex is ParamError) {
                    context.sendBack {
                        error("错误: ${ex.msg}${ex.param.name}")
                        errorFieldName(ex.param.name)
                    }
                } else {
                    context.response.contentTypeHtml()
                    ex.printStackTrace(context.response.writer)
                    context.response.flushBuffer()
                }
            } else {
                context.sendFailed(-1000, ex.message ?: "")
            }

        } finally {
            context.deleteTempPartFiles()
        }
    }

    companion object {
        private val mapperList: ArrayList<ParamMapper> = arrayListOf(TextMapper, PartMapper, ListMapper)
        private fun prepareValue(context: HttpContext, p: KParameter): Any? {
            for (m in mapperList) {
                if (m.accept(context, p)) {
                    try {
                        return m.toValue(context, p)
                    } catch (ex: Exception) {
                        logd("Parameter Error: $p , ${context[p]}")
                        throw ex
                    }
                }
            }
            p.errorUnsupport("不支持的参数")
        }

        fun prepareParams(context: HttpContext, inst: Any, func: KFunction<*>): HashMap<KParameter, Any?> {
            val map = HashMap<KParameter, Any?>()
            for (p in func.parameters) {
                if (p.kind == KParameter.Kind.EXTENSION_RECEIVER) {
                    map[p] = inst
                    continue
                }
                if (p.kind == KParameter.Kind.INSTANCE) {
                    map[p] = inst
                    continue
                }
                if (p.type.classifier == HttpContext::class) {
                    map[p] = context
                    continue
                }
                val exist = context.hasParam(p.userName)
                if (!exist) {
                    if (p.isOptional)
                        continue
                    if (p.type.isMarkedNullable) {
                        map[p] = null
                        continue
                    }
                    p.errorMiss()
                } else {
                    map[p] = prepareValue(context, p)
                }
            }
            return map
        }

    }
}

interface ParamMapper {
    fun accept(context: HttpContext, param: KParameter): Boolean
    fun toValue(context: HttpContext, param: KParameter): Any?
}

// file:Part
// fileList:List<Part>
// fileList:ArrayList<Part>
// map:Map<String,Part>
// map:HashMap<String,Part>
private object PartMapper : ParamMapper {
    private val filePartList: List<KClass<*>> = listOf(Part::class, File::class, FilePart::class)
    override fun accept(context: HttpContext, param: KParameter): Boolean {
        val pt = param.type
        return when (pt.classifier) {
            FilePart::class -> true
            File::class -> true
            Part::class -> true
            List::class, ArrayList::class -> pt.arguments[0].type?.classifier in filePartList
            Map::class, HashMap::class -> pt.arguments[0].type?.classifier == String::class && pt.arguments[1].type?.classifier in filePartList
            else -> false
        }
    }

    private fun part2File(context: HttpContext, part: Part): File? {
        val f = context.createTempPartFile()
        val os = f.outputStream()
        try {
            part.inputStream.use { it.copyTo(os) }
            return f
        } catch (ex: Exception) {
            return null
        } finally {
            os.closeSafe()
            part.delete()
        }
    }

    override fun toValue(context: HttpContext, param: KParameter): Any? {
        when (param.type.classifier) {
            FilePart::class -> {
                val p = context.partList.firstOrNull { it.name == param.userName } ?: return null
                val f = part2File(context, p) ?: return null
                return FilePart(f, p)
            }

            File::class -> {
                val p = context.partList.firstOrNull { it.name == param.userName } ?: return null
                return part2File(context, p)
            }

            Part::class -> {
                return context.partList.firstOrNull { it.name == param.userName }
            }

            List::class, ArrayList::class -> {
                val t = param.type.arguments[0].type ?: return null
                val pList = context.partList.filter { it.name == param.userName }
                when (t.classifier) {
                    Part::class -> return pList.toArrayList()
                    File::class -> pList.mapNotNull { part2File(context, it) }.toArrayList()
                    FilePart::class -> pList.mapNotNull {
                        val f = part2File(context, it)
                        if (f == null) null else FilePart(f, it)
                    }.toArrayList()

                    else -> return null
                }
            }

            Map::class, HashMap::class -> {
                val t = param.type.arguments[0].type ?: return null
                val ps = context.partList
                when (t.classifier) {
                    Part::class -> {
                        val map = HashMap<String, Part>()
                        for (p in ps) {
                            map[p.name] = p
                        }
                        return map
                    }

                    File::class -> {
                        val map = HashMap<String, File>()
                        for (p in ps) {
                            map[p.name] = part2File(context, p) ?: continue
                        }
                        return map
                    }

                    FilePart::class -> {
                        val map = HashMap<String, FilePart>()
                        for (p in ps) {
                            val f = part2File(context, p) ?: continue
                            map[p.name] = FilePart(f, p)
                        }
                        return map
                    }

                    else -> return null
                }
            }
        }
        param.errorUnsupport()
    }

}

private object ListMapper : ParamMapper {

    override fun accept(context: HttpContext, param: KParameter): Boolean {
        val c = param.type.classifier
        if (c == List::class || c == ArrayList::class) {
            val pt = param.type.arguments[0].type
            return typeCastContains(pt?.classifier as? KClass<*>)

        }
        return false
    }

    override fun toValue(context: HttpContext, param: KParameter): Any {
        val ls = ArrayList<Any>()
        param.type.arguments.firstOrNull()?.type ?: return ls
        val sls = context.paramMap[param.userName] ?: return ls
        for (s in sls) {
            val v: Any? = param.fromText(s)
            if (v != null) {
                ls.add(v)
            }
        }
        return ls
    }
}


private object TextMapper : ParamMapper {
    override fun accept(context: HttpContext, param: KParameter): Boolean {
        return typeCastContains(param.type.classifier as? KClass<*>)
    }

    override fun toValue(context: HttpContext, param: KParameter): Any? {
        var s = context[param.userName] ?: return null
        if (param.hasAnnotation<Trim>()) {
            s = s.trim()
        }
        if (s.isEmpty()) {
            if (param.type.classifier == String::class) {
                return if (param.type.isMarkedNullable) {
                    null
                } else {
                    ""
                }
            }
            return null
        }
        check(param, s)
        return param.fromText(s)
    }

    private fun check(param: KParameter, text: String) {
        if (text.isEmpty() && param.hasAnnotation<NotEmpty>()) {
            param.err("值不能为空")
        }
        if (text.trim().isEmpty() && param.hasAnnotation<NotBlank>()) {
            param.err("值不能为空")
        }
        for (an in param.annotations) {
            when (an) {
                is MaxLength -> if (text.length > an.value) {
                    param.err("参数长度须小于${an.value}")
                }

                is LengthRange -> if (text.length > an.maxValue) {
                    param.err("参数内容太长")
                } else if (text.length < an.minValue) {
                    param.err("参数 内容太短")
                }

                is Match -> if (!text.matches(an.value.toRegex())) {
                    param.err(an.msg.ifEmpty { "参数规则不匹配" })
                }

                is MinValue -> if (text.toDouble() < an.value.toDouble()) {
                    param.err(an.msg.ifEmpty { "参数太小" })
                }

                is MaxValue -> if (text.toDouble() > an.value.toDouble()) {
                    param.err(an.msg.ifEmpty { "参数太大" })
                }

                is ValueRange -> {
                    val n = text.toDouble()
                    if (n < an.minVal.toDouble()) {
                        param.err(an.msg.ifEmpty { "参数太小" })
                    }
                    if (n > an.maxVal.toDouble()) {
                        param.err(an.msg.ifEmpty { "参数太大" })
                    }
                }
            }
        }
    }
}


private fun KParameter.fromText(text: String): Any? {
    val v = this.decodeAndCast(text)
    if (v == null && !this.type.isMarkedNullable) {
        this.errorMiss()
    }
    return v
}

