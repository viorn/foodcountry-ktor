package com.taganhorn.repositories

import com.taganhorn.entities.Ingredient
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object IngredientRepository {
    class Ingredients: Table() {
        val id = integer("id").primaryKey()
        val name = varchar("name", length = 200)
        val fats = double("fats")
        val squirrels = double("squirrels")
        val carbohydrates = double("carbohydrates")
    }

    suspend fun getIngredients(): List<Ingredient> = transaction {

        return@transaction emptyList()
    }
}