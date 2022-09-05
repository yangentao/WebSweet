package dev.entao.web.base


fun <T> Collection<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}


fun <T> MutableList<T>.shift(n: Int) {
    if (n <= 0 || n > this.size) {
        return
    }

    for (i in 1..n) {
        this.removeAt(0)
    }
}

fun <T> MutableList<T>.replaceFirst(oldVal: T, newVal: T) {
    this.forEachIndexed { n, v ->
        if (v == oldVal) {
            this[n] = newVal
            return
        }
    }
}

fun <T> MutableIterable<T>.removeAllIf(predicate: (T) -> Boolean): ArrayList<T> {
    val ls = ArrayList<T>()
    with(iterator()) {
        while (hasNext()) {
            val a: T = this.next()
            if (predicate(a)) {
                remove()
                ls += a
            }
        }
    }
    return ls
}

fun <T> MutableList<T>.removeAllIf(predicate: (T) -> Boolean): ArrayList<T> {
    if (this !is RandomAccess) {
        return (this as MutableIterable<T>).removeAllIf(predicate)
    }

    val ls = ArrayList<T>()
    var writeIndex: Int = 0
    for (readIndex in 0..lastIndex) {
        val element = this[readIndex]
        if (predicate(element)) {
            ls += element
            continue
        }
        if (writeIndex != readIndex) {
            this[writeIndex] = element
        }
        writeIndex++
    }
    if (writeIndex < size) {
        for (removeIndex in lastIndex downTo writeIndex) {
            removeAt(removeIndex)
        }
    }
    return ls
}

fun <T> List<T>.secondOrNull(): T? {
    return if (this.size < 2) null else this[1]
}

fun <T> List<T>.second(): T {
    return this[1]
}


operator fun StringBuilder.plusAssign(s: String) {
    this.append(s)
}

operator fun StringBuilder.plusAssign(ch: Char) {
    this.append(ch)
}

