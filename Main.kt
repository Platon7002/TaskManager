import menu.MenuController
import repository.TaskRepository
import storage.ErrorLogger
import storage.TaskFileStorage
fun main() {
    val logger = ErrorLogger()
    val repository = TaskRepository(TaskFileStorage())
    MenuController(repository, logger).run()
}
