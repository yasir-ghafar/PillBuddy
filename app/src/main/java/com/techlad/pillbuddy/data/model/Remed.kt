package com.techlad.pillbuddy.data.model

data class Remed(
    val id: Long,
    val name: String,
    val instructions: String,
    val time: String,
    val dueNow: Boolean = false,
    val completed: Boolean = false,
)
