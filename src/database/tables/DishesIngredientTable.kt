package com.taganhorn.tables

import org.jetbrains.exposed.sql.Table
import database.tables.IngredientsTable

object DishesIngredientTable : Table("dishes_ingredient") {
    val dishId = (integer("dishId") references DishesTable.id).index()
    val ingredientId = (integer("ingredientId") references IngredientsTable.id)
    val weight = integer("weight")
}