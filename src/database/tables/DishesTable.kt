package com.taganhorn.tables

import com.taganhorn.entities.VisibleType
import database.tables.UsersTable
import org.jetbrains.exposed.sql.Table

object DishesTable : Table("dishes") {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", length = 200)
    val ownerId = (integer("ownerId") references UsersTable.id).index()
    val visible = varchar("visible", length = 100).default(VisibleType.PRIVATE.name).index()
}