package model

import java.time.LocalDateTime

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val priority: Priority,
    val isDone: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now().withNano(0)
)
