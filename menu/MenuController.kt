package menu

import model.Priority
import model.Task
import repository.SearchCriteria
import repository.SortField
import repository.SortOrder
import repository.TaskRepository
import storage.ErrorLogger
import storage.StorageException

class MenuController(
    private val repository: TaskRepository,
    private val logger: ErrorLogger,
    private val io: ConsoleIO = ConsoleIO()
) {

    private val defaultFile = "tasks.json"

    fun run() {
        println("=== Менеджер задач ===")
        try {
            while (true) {
                printMenu()
                val choice = io.readLine("Ваш выбор: ")
                if (choice == "0") {
                    break
                }
                handle(choice)
            }
        } catch (e: InputClosedException) {
            println()
        }
        println("До свидания!")
    }

    private fun printMenu() {
        println()
        println("1 — Создать задачу")
        println("2 — Показать все задачи")
        println("3 — Найти задачу")
        println("4 — Редактировать задачу")
        println("5 — Удалить задачу")
        println("6 — Сортировка")
        println("7 — Сохранить в файл")
        println("8 — Загрузить из файла")
        println("9 — Отметить несколько задач выполненными")
        println("0 — Выход")
    }

    private fun handle(choice: String) {
        try {
            when (choice) {
                "1" -> createTask()
                "2" -> showAll()
                "3" -> searchTasks()
                "4" -> editTask()
                "5" -> deleteTask()
                "6" -> sortTasks()
                "7" -> saveToFile()
                "8" -> loadFromFile()
                "9" -> markSeveralDone()
                else -> println("Нет такого пункта меню. Введите число от 0 до 9.")
            }
        } catch (e: InputClosedException) {
            throw e
        } catch (e: StorageException) {
            println("Ошибка: ${e.message}")
            logger.log("Ошибка работы с файлом", e)
        } catch (e: Exception) {
            println("Непредвиденная ошибка: ${e.message}. Подробности записаны в errors.log")
            logger.log("Непредвиденная ошибка в пункте меню «$choice»", e)
        }
    }


    private fun createTask() {
        println("--- Новая задача ---")
        val title = io.readNonEmpty("Название: ")
        val description = io.readLine("Описание (можно пустое): ")
        val priority = readPriority("Приоритет (1–5): ")
        val task = repository.add(title, description, priority)
        println("Задача создана, её ID: ${task.id}")
    }

    private fun showAll() {
        println(TableFormatter.format(repository.getAll()))
    }

    private fun searchTasks() {
        println("--- Поиск (пустое поле = условие не учитывается) ---")
        val titlePart = io.readLine("Часть названия: ").ifEmpty { null }
        val priority = io.readOptionalInt("Приоритет (1–5): ", Priority.MIN_LEVEL..Priority.MAX_LEVEL)
            ?.let { Priority.fromLevel(it) }
        val isDone = io.readOptionalBoolean("Выполнена? (y — да, n — нет): ")

        val criteria = SearchCriteria(titlePart, priority, isDone)
        if (criteria.isEmpty) {
            println("Условия не заданы, показываю все задачи.")
        }
        println(TableFormatter.format(repository.search(criteria)))
    }

    private fun editTask() {
        val task = askExistingTask("ID задачи для редактирования: ") ?: return
        println("Текущая задача:")
        println(TableFormatter.format(listOf(task)))
        println("Пустое поле = оставить старое значение. В описании «-» очищает его.")

        val title = io.readLine("Название [${task.title}]: ").ifEmpty { null }
        val descriptionInput = io.readLine("Описание [${task.description}]: ")
        val description = when (descriptionInput) {
            "" -> null
            "-" -> ""
            else -> descriptionInput
        }
        val priority = io.readOptionalInt(
            "Приоритет 1–5 [${task.priority.level}]: ",
            Priority.MIN_LEVEL..Priority.MAX_LEVEL
        )?.let { Priority.fromLevel(it) }
        val isDone = io.readOptionalBoolean(
            "Выполнена? y/n [${if (task.isDone) "y" else "n"}]: "
        )

        val updated = repository.update(task.id, title, description, priority, isDone)
        if (updated == null) {
            println("Задача уже не существует.")
        } else {
            println("Задача изменена:")
            println(TableFormatter.format(listOf(updated)))
        }
    }

    private fun deleteTask() {
        val task = askExistingTask("ID задачи для удаления: ") ?: return
        println("Будет удалена: #${task.id} «${task.title}»")
        if (io.confirm("Вы уверены, что хотите удалить? (y/n): ")) {
            repository.delete(task.id)
            println("Задача удалена.")
        } else {
            println("Удаление отменено.")
        }
    }

    private fun sortTasks() {
        println("Сортировать по: 1 — дате создания, 2 — приоритету, 3 — названию")
        val field = when (io.readInt("Ваш выбор: ", 1..3)) {
            1 -> SortField.DATE
            2 -> SortField.PRIORITY
            else -> SortField.TITLE
        }
        println("Порядок: 1 — по возрастанию, 2 — по убыванию")
        val order = if (io.readInt("Ваш выбор: ", 1..2) == 1) SortOrder.ASCENDING else SortOrder.DESCENDING
        println(TableFormatter.format(repository.sort(field, order)))
    }

    private fun saveToFile() {
        val path = readFileName()
        val count = repository.saveToFile(path)
        println("Сохранено задач: $count. Файл: ${java.io.File(path).absolutePath}")
    }

    private fun loadFromFile() {
        val path = readFileName()
        if (repository.getAll().isNotEmpty() &&
            !io.confirm("Текущие задачи будут заменены. Продолжить? (y/n): ")
        ) {
            println("Загрузка отменена.")
            return
        }
        val count = repository.loadFromFile(path)
        println("Загружено задач: $count")
    }

    private fun markSeveralDone() {
        println(TableFormatter.format(repository.getAll()))
        val ids = io.readIds("ID задач через пробел или запятую (например: 1 3 5): ")
        if (ids == null) {
            println("Нужно ввести только числа, например: 1 3 5")
            return
        }
        val found = repository.markDone(ids)
        println("Отмечено выполненными: ${found.size}")
        val missing = ids - found
        if (missing.isNotEmpty()) {
            println("Не найдены задачи с ID: ${missing.sorted().joinToString(", ")}")
        }
    }


    private fun readPriority(prompt: String): Priority {
        val level = io.readInt(prompt, Priority.MIN_LEVEL..Priority.MAX_LEVEL)
        return Priority.fromLevel(level) ?: Priority.MEDIUM
    }

    private fun askExistingTask(prompt: String): Task? {
        val id = io.readLine(prompt).toIntOrNull()
        if (id == null) {
            println("ID должен быть числом.")
            return null
        }
        val task = repository.findById(id)
        if (task == null) {
            println("Задача с ID $id не найдена.")
        }
        return task
    }

    private fun readFileName(): String {
        val name = io.readLine("Имя файла [$defaultFile]: ")
        return name.ifEmpty { defaultFile }
    }
}
