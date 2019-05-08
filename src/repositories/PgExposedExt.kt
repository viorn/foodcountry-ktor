package com.taganhorn.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.postgresql.jdbc.PgArray

fun <T : Comparable<T>, S : T?> ExpressionWithColumnType<in S>.pg_array_agg(): ExpressionWithColumnType<PgArray> =
    PgArrayAgg<T, S>(this, this.columnType)

class PgArrayAgg<T : Comparable<T>, in S : T?>(val expr: Expression<in S>, _columnType: IColumnType) :
    Function<PgArray>(_columnType) {
    override fun toSQL(queryBuilder: QueryBuilder): String = "array_agg(${expr.toSQL(queryBuilder)})"
}

fun <T : Comparable<T>, S : T?> ExpressionWithColumnType<in S>.pg_json_agg(): ExpressionWithColumnType<String> =
    PgJsonAgg<T, S>(this, this.columnType)

class PgJsonAgg<T : Comparable<T>, in S : T?>(val expr: Expression<in S>, _columnType: IColumnType) :
    Function<String>(_columnType) {
    override fun toSQL(queryBuilder: QueryBuilder): String = "json_agg(${expr.toSQL(queryBuilder)})"
}