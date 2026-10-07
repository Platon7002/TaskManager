package menu

import model.Task
import java.time.format.DateTimeFormatter

object TableFormatter {

    private const val ID_WIDTH = 5
    private const val TITLE_WIDTH = 28
    private const val PRIORITY_WIDTH = 18
    private const val DONE_WIDTH = 6
    private const val DATE_WIDTH = 16

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun format(tasks: List<Task>): String {
        if (tasks.isEmpty()) {
            return "Список пуст."
        }
        val lines = mutableListOf<String>()
        lines.add(row("ID", "Title", "Priority", "Done", "CreatedAt"))
        lines.add("-".repeat(ID_WIDTH + TITLE_WIDTH + PRIORITY_WIDTH + DONE_WIDTH + DATE_WIDTH))
        for (task in tasks) {
            lines.add(
                row(
                    task.id.toString(),
                    cut(task.title, TITLE_WIDTH - 1),
                    "${task.priority.level} (${task.priority.title})",
                    if (task.isDone) "Yes" else "No",
                    task.createdAt.format(dateFormat)
                )
            )
        }
        lines.add("Всего задач: ${tasks.size}")
        return lines.joinToString("\n")
    }

    private fun row(id: String, title: String, priority: String, done: String, date: String): String =
        id.padEnd(ID_WIDTH) + title.padEnd(TITLE_WIDTH) + priority.padEnd(PRIORITY_WIDTH) +
            done.padEnd(DONE_WIDTH) + date

    private fun cut(text: String, maxLength: Int): String {
        val oneLine = text.replace('\n', ' ')
        return if (oneLine.length <= maxLength) oneLine else oneLine.take(maxLength - 1) + "…"
    }
}
