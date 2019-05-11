package com.taganhorn.entities

import com.google.gson.annotations.Expose

data class Ingredient(
    @Expose val id: Int = -1,
    @Expose val name: String,
    @Expose val fats: Float = 0f,
    @Expose val squirrels: Float = 0f,
    @Expose val carbohydrates: Float = 0f,
    @Expose val ownerId: Int? = null,
    @Expose val visible: VisibleType = VisibleType.PRIVATE
)