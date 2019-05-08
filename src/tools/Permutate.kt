package com.taganhorn.tools

inline fun <reified T : Any> Array<out T>.permutate(): List<List<T>> {
    val outSet = ArrayList<List<T>>()
    forEach { t ->
        var tmpList = listOf<T>(t)
        outSet.add(tmpList.toList())
        while (tmpList.size < size) {
            val f = filter { !tmpList.contains(it) }
            f.forEach {
                val l = listOf(*(tmpList.toTypedArray()), it)
                outSet.add(l)
            }
            tmpList = outSet.last()
        }
    }
    return outSet
}