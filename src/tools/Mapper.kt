package com.taganhorn.tools

import com.google.gson.annotations.Expose
import kotlin.reflect.KVisibility
import kotlin.reflect.full.*

fun <T : Any> T.createMap(): MutableMap<String, Any?> {
    val property = this::class.memberProperties
        .filter { it.visibility == KVisibility.PUBLIC && it.findAnnotation<Expose>() != null }
        .map {
            it.name to it.getter.call(this@createMap)
        }.toMap()
    return HashMap<String, Any?>().apply {
        putAll(property)
    }
}