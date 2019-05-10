package com.taganhorn.repositories

import com.taganhorn.entities.Ingredient
import com.taganhorn.entities.VisibleType
import com.taganhorn.repositories.UserRepository.UsersRole.index
import com.taganhorn.repositories.UserRepository.UsersRole.references
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object IngredientRepository {
    object Ingredients : Table() {
        val id = integer("id").primaryKey().autoIncrement()
        val name = varchar("name", length = 200)
        val fats = registerColumn<Float>("fats", Float4ColumnType())
        val squirrels = registerColumn<Float>("squirrels", Float4ColumnType())
        val carbohydrates = registerColumn<Float>("carbohydrates", Float4ColumnType())
        val ownerId = (integer("ownerId") references UserRepository.Users.id).index()
        val visible = varchar("visible", length = 100).default(VisibleType.PRIVATE.name).index()
    }

    init {
        transaction {
            SchemaUtils.apply {
                create(Ingredients)
            }
        }
    }

    suspend fun getIngredients(offset: Int = 0, limit: Int = 10, userIds: List<Int>? = null): List<Ingredient> =
        withContext(Dispatchers.IO) {
            transaction {
                return@transaction Ingredients.select {
                    if (userIds.isNullOrEmpty()) OP_TRUE else Ingredients.ownerId.inList(userIds)
                }.limit(limit, offset).map {
                    Ingredient(
                        id = it[Ingredients.id],
                        name = it[Ingredients.name],
                        fats = it[Ingredients.fats],
                        squirrels = it[Ingredients.squirrels],
                        carbohydrates = it[Ingredients.carbohydrates],
                        ownerId = it[Ingredients.ownerId],
                        visible = VisibleType.valueOf(it[Ingredients.visible])
                    )
                }
            }
        }

    suspend fun addIngredient(vararg ingredients: Ingredient) = withContext(Dispatchers.IO) {
        transaction {
            Ingredients.batchInsert(ingredients.asIterable()) {
                this[Ingredients.name] = it.name
                this[Ingredients.fats] = it.fats
                this[Ingredients.carbohydrates] = it.carbohydrates
                this[Ingredients.squirrels] = it.squirrels
                this[Ingredients.ownerId] = it.ownerId
                this[Ingredients.visible] = it.visible.name
            }
        }.map {
            Ingredient(
                id = it[Ingredients.id],
                name = it[Ingredients.name],
                fats = it[Ingredients.fats],
                carbohydrates = it[Ingredients.carbohydrates],
                squirrels = it[Ingredients.squirrels],
                ownerId = it[Ingredients.ownerId],
                visible = it[Ingredients.visible].let { VisibleType.valueOf(it) }
            )
        }
    }

    suspend fun totalIngredients(userIds: List<Int>? = null): Int = withContext(Dispatchers.IO) {
        transaction {
            Ingredients.select {
                if (userIds.isNullOrEmpty()) OP_TRUE else Ingredients.ownerId.inList(userIds)
            }.count()
        }
    }
}