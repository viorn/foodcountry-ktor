package tables

import org.jetbrains.exposed.sql.Table

object UsersRole : Table() {
    val userId = (integer("userId") references Users.id).index()
    val role = varchar("role", 50).index()
}