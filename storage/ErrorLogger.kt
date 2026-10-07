package storage

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ErrorLogger(private val file: File = File("errors.log")) {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun log(message: String, error: Throwable? = null) {
        val details = if (error != null) " | ${error::class.simpleName}: ${error.message}" else ""
        val line = "[${LocalDateTime.now().format(formatter)}] $message$details\n"
        try {
            file.appendText(line, Charsets.UTF_8)
        } catch (ignored: Exception) {
        }
    }
}
