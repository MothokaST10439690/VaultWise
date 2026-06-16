package com.example.vaultwise.util

import android.content.Context
import com.example.vaultwise.R

object CategoryUtils {
    fun getCategoryColor(context: Context, category: String): Int {
        return when (category.lowercase()) {
            "food" -> context.getColor(R.color.cat_food)
            "rent" -> context.getColor(R.color.cat_rent)
            "shopping" -> context.getColor(R.color.cat_shopping)
            "transport" -> context.getColor(R.color.cat_transport)
            else -> context.getColor(R.color.cat_other)
        }
    }
}
