package com.example.simpletaskapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val updated_at: String
) : Parcelable {

    companion object {
        const val STATUS_ACTIVE = "active"
        const val STATUS_DONE = "done"
        const val STATUS_DELETED = "deleted"

        const val CATEGORY_IMPORTANT = "Important"
        const val CATEGORY_URGENT = "Urgent"
        const val CATEGORY_REGULAR = "Regular"
    }
}