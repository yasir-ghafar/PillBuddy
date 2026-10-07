package com.techlad.pillbuddy.data.model

data class Remed(
    val id: Int,
    val name: String,
    val instructions: String,
    val time: String,
    val dueNow: Boolean = false,
    val completed: Boolean = false,
)

const val NexiumInstructions =
    "One capsule daily, one hour before meals, with one cup of water."

fun sampleRemeds(): List<Remed> = listOf(
    Remed(
        id = 1,
        name = "Nexium",
        instructions = NexiumInstructions,
        time = "9:00 AM",
        dueNow = true,
    ),
    Remed(
        id = 2,
        name = "Nexium",
        instructions = NexiumInstructions,
        time = "9:00 AM",
    ),
    Remed(
        id = 3,
        name = "Nexium",
        instructions = NexiumInstructions,
        time = "9:00 AM",
    ),
)
