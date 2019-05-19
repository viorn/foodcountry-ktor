package database.tables

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", length = 100).uniqueIndex()
    val password = varchar("password", length = 100)
    val locked = bool("locked").default(false)
}