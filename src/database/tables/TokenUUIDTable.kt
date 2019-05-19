package database.tables

import org.jetbrains.exposed.sql.Table

object TokenUUIDTable : Table("token_uuid") {
    val userId = (integer("userId") references UsersTable.id).index()
    val uuid = varchar("uuid", length = 36)
    val date = datetime("datetime")
    val userAgent = varchar("userAgent", 300).nullable()
}