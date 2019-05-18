package tables

import org.jetbrains.exposed.sql.Table
import tables.Users

object TokenUUID : Table() {
    val userId = (integer("userId") references Users.id).index()
    val uuid = varchar("uuid", length = 36)
    val date = datetime("datetime")
    val userAgent = varchar("userAgent", 300).nullable()
}