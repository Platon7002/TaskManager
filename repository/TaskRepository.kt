package repository

import model.Priority
import model.Task
import storage.TaskFileStorage

class TaskRepository(private val storage: TaskFileStorage) {

    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    fun add(title: String, description: String, priority: Priority): Task {
        val task = Task(nextId++, title.trim(), description.trim(), priority)
        tasks.add(task)
        return task
    }

    fun getAll(): List<Task> = tasks.toList()

    fun findById(id: Int): Task? = tasks.find { it.id == id }

    fun update(
        id: Int,
        title: String? = null,
        description: String? = null,
        priority: Priority? = null,
        isDone: Boolean? = null
    ): Task? {
        val index = tasks.indexOfFirst { it.id == id }
        if (index < 0) {
            return null
        }
        val old = tasks[index]
        val updated = old.copy(
            title = title?.trim() ?: old.title,
            description = description?.trim() ?: old.description,
            priority = priority ?: old.priority,
            isDone = isDone ?: old.isDone
        )
        tasks[index] = updated
        return updated
    }

    fun markDone(ids: Set<Int>): Set<Int> {
        val found = mutableSetOf<Int>()
        for (id in ids) {
            if (update(id, isDone = true) != null) {
                found.add(id)
            }
        }
        return found
    }

    fun delete(id: Int): Boolean = tasks.removeIf { it.id == id }

    fun search(criteria: SearchCriteria): List<Task> = tasks.filter { criteria.matches(it) }

    fun sort(field: SortField, order: SortOrder): List<Task> {
        val comparator: Comparator<Task> = when (field) {
            SortField.DATE -> compareBy<Task> { it.createdAt }.thenBy { it.id }
            SortField.PRIORITY -> compareBy<Task> { it.priority.level }.thenBy { it.id }
            SortField.TITLE -> compareBy<Task> { it.title.lowercase() }.thenBy { it.id }
        }
        tasks.sortWith(if (order == SortOrder.DESCENDING) comparator.reversed() else comparator)
        return getAll()
    }

    fun saveToFile(path: String): Int {
        storage.save(path, tasks)
        return tasks.size
    }

    fun loadFromFile(path: String): Int {
        val loaded = storage.load(path)
        tasks.clear()
        tasks.addAll(loaded)
        nextId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
        return tasks.size
    }
}
