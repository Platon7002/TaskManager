package repository

import model.Priority
import model.Task

data class SearchCriteria(
    val titlePart: String? = null,
    val priority: Priority? = null,
    val isDone: Boolean? = null
) {
    val isEmpty: Boolean
        get() = titlePart == null && priority == null && isDone == null

    fun matches(task: Task): Boolean =
        (titlePart == null || task.title.contains(titlePart, ignoreCase = true)) &&
            (priority == null || task.priority == priority) &&
            (isDone == null || task.isDone == isDone)
}
