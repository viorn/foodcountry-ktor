package com.taganhorn.tables

import com.taganhorn.entities.VisibleType
import org.jetbrains.exposed.sql.Table

object DishesTable : Table("dishes") {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", length = 200)
    val visible = varchar("visible", length = 100).default(VisibleType.PRIVATE.name).index()
}