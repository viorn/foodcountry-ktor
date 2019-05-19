package com.taganhorn.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.BatchInsertStatement

object OP_TRUE : Op<Boolean>() {
    override fun toSQL(queryBuilder: QueryBuilder) = DataBaseProvider.database!!.dialect.dataTypeProvider?.booleanToStatementString(true)
}
object OP_FALSE : Op<Boolean>() {
    override fun toSQL(queryBuilder: QueryBuilder) = DataBaseProvider.database!!.dialect.dataTypeProvider.booleanToStatementString(true)
}

class PairExpression<L,R>(val first: Expression<L>, val second: Expression<R>) : Expression<Pair<L,R>>() {
    override fun toSQL(queryBuilder: QueryBuilder): String =
        "(${first.toSQL(queryBuilder)}, ${second.toSQL(queryBuilder)})"
}

fun <L,R> PairExpression<L, R>.inList(list: List<PairExpression<L,R>>) = object : Op<Boolean>() {
    override fun toSQL(queryBuilder: QueryBuilder): String {
        return when(list.size) {
            0 -> "FALSE"
            1 -> "${this.toSQL(queryBuilder)} = ${list[0].toSQL(queryBuilder)}"
            else -> list.joinToString(",", prefix = "${this.toSQL(queryBuilder)} in (", postfix = ")") {
                it.toSQL(queryBuilder)
            }
        }
    }
}

infix fun <L,R> Expression<L>.to(exp2: Expression<R>) = PairExpression(this, exp2)