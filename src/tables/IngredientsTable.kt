package tables

import com.taganhorn.entities.VisibleType
import com.taganhorn.repositories.Float4ColumnType
import org.jetbrains.exposed.sql.Table

object IngredientsTable : Table("ingredients") {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", length = 200)
    val fats = registerColumn<Float>("fats", Float4ColumnType())
    val squirrels = registerColumn<Float>("squirrels", Float4ColumnType())
    val carbohydrates = registerColumn<Float>("carbohydrates", Float4ColumnType())
    val ownerId = (integer("ownerId") references UsersTable.id).index()
    val visible = varchar("visible", length = 100).default(VisibleType.PRIVATE.name).index()
}