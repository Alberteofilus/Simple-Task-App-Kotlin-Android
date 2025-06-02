package com.example.simpletaskapp

data class TaskResponse(
    val tasks: List<Task>,
    val counts: Map<String, Int>
)