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


fun <T> List<T>.second(): T {
    return this[1]
}


operator fun StringBuilder.plusAssign(s: String) {
    this.append(s)
}

operator fun StringBuilder.plusAssign(ch: Char) {
    this.append(ch)
}

