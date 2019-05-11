package com.taganhorn.tools

import kotlin.reflect.KVisibility
import kotlin.reflect.full.*

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class Mapper

fun <T : Any> T.createMap(): MutableMap<String, Any?> {
    val property = this::class.memberProperties
        .filter { it.visibility == KVisibility.PUBLIC && it.findAnnotation<Mapper>() != null }
        .map {
            it.name to it.getter.call(this@createMap)
        }.toMap()
    val function = this::class.memberFunctions
        .filter { it.visibility == KVisibility.PUBLIC && it.findAnnotation<Mapper>() != null }
        .mapNotNull {
            runCatching { it.name to it.call(this@createMap) }.getOrNull()
        }.toMap()
    return HashMap<String, Any?>().apply {
        putAll(property)
        putAll(function)
    }
}