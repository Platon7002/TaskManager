package storage

import model.Priority
import model.Task
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object TaskJsonMapper {

    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME   // 2025-02-10T12:11:45


    fun toJson(tasks: List<Task>): String {
        if (tasks.isEmpty()) {
            return "[]"
        }
        return tasks.joinToString(separator = ",\n", prefix = "[\n", postfix = "\n]") { task ->
            """
            |  {
            |    "id": ${task.id},
            |    "title": ${quote(task.title)},
            |    "description": ${quote(task.description)},
            |    "priority": ${task.priority.level},
            |    "isDone": ${task.isDone},
            |    "createdAt": ${quote(task.createdAt.format(dateFormat))}
            |  }
            """.trimMargin()
        }
    }

    private fun quote(value: String): String {
        val sb = StringBuilder("\"")
        for (ch in value) {
            when {
                ch == '"' -> sb.append("\\\"")
                ch == '\\' -> sb.append("\\\\")
                ch == '\n' -> sb.append("\\n")
                ch == '\r' -> sb.append("\\r")
                ch == '\t' -> sb.append("\\t")
                ch < ' ' -> sb.append("\\u%04x".format(ch.code))
                else -> sb.append(ch)
            }
        }
        return sb.append('"').toString()
    }


    fun fromJson(text: String): List<Task> {
        val root = JsonParser(text).parse()
        if (root !is List<*>) {
            throw JsonFormatException("в файле должен быть список задач [ ... ]")
        }
        val tasks = mutableListOf<Task>()
        for ((index, element) in root.withIndex()) {
            val number = index + 1
            if (element !is Map<*, *>) {
                throw JsonFormatException("элемент №$number не является объектом { ... }")
            }
            tasks.add(parseTask(element, number))
        }
        val duplicate = tasks.groupBy { it.id }.entries.firstOrNull { it.value.size > 1 }
        if (duplicate != null) {
            throw JsonFormatException("повторяется ID ${duplicate.key}")
        }
        return tasks
    }

    private fun parseTask(map: Map<*, *>, number: Int): Task {
        val id = (map["id"] as? Long)?.toInt()
            ?: throw JsonFormatException("задача №$number: нет числового поля id")
        if (id < 1) {
            throw JsonFormatException("задача №$number: id должен быть больше 0")
        }
        val title = (map["title"] as? String)?.trim()
        if (title.isNullOrEmpty()) {
            throw JsonFormatException("задача №$number: нет названия (title)")
        }
        val description = when (val raw = map["description"]) {
            null -> ""
            is String -> raw
            else -> throw JsonFormatException("задача №$number: description должен быть строкой")
        }
        val level = (map["priority"] as? Long)?.toInt()
            ?: throw JsonFormatException("задача №$number: нет числового поля priority")
        val priority = Priority.fromLevel(level)
            ?: throw JsonFormatException("задача №$number: priority должен быть от 1 до 5, а не $level")
        val isDone = map["isDone"] as? Boolean
            ?: throw JsonFormatException("задача №$number: нет поля isDone (true/false)")
        val createdAt = try {
            LocalDateTime.parse(map["createdAt"] as? String ?: "")
        } catch (e: DateTimeParseException) {
            throw JsonFormatException("задача №$number: неверная дата createdAt")
        }
        return Task(id, title, description, priority, isDone, createdAt)
    }
}
