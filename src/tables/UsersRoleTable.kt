package tables

import org.jetbrains.exposed.sql.Table

object UsersRoleTable : Table("users_role") {
    val userId = (integer("userId") references UsersTable.id).index()
    val role = varchar("role", 50).index()
}