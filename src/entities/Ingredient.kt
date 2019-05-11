package com.taganhorn.entities

import com.google.gson.annotations.Expose
import com.taganhorn.tools.Mapper

data class Ingredient(
    @[Mapper Expose] val id: Int = -1,
    @[Mapper Expose] val name: String,
    @[Mapper Expose] val fats: Float = 0f,
    @[Mapper Expose] val squirrels: Float = 0f,
    @[Mapper Expose] val carbohydrates: Float = 0f,
    @[Mapper Expose] val ownerId: Int? = null,
    @[Mapper Expose] val visible: VisibleType = VisibleType.PRIVATE
) {
    @Mapper fun calorie() = fats*8+squirrels*4+carbohydrates*4
}