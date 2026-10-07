package menu

class InputClosedException : Exception("Ввод закрыт")

class ConsoleIO {

    fun readLine(prompt: String): String {
        print(prompt)
        val line = readlnOrNull() ?: throw InputClosedException()
        return line.trim()
    }

    fun readNonEmpty(prompt: String): String {
        while (true) {
            val text = readLine(prompt)
            if (text.isNotEmpty()) {
                return text
            }
            println("  Поле не может быть пустым, попробуйте ещё раз.")
        }
    }

    fun readInt(prompt: String, range: IntRange): Int {
        while (true) {
            val value = readLine(prompt).toIntOrNull()
            if (value != null && value in range) {
                return value
            }
            println("  Введите целое число от ${range.first} до ${range.last}.")
        }
    }

    fun readOptionalInt(prompt: String, range: IntRange): Int? {
        while (true) {
            val text = readLine(prompt)
            if (text.isEmpty()) {
                return null
            }
            val value = text.toIntOrNull()
            if (value != null && value in range) {
                return value
            }
            println("  Введите целое число от ${range.first} до ${range.last} или нажмите Enter.")
        }
    }

    fun confirm(prompt: String): Boolean {
        while (true) {
            when (readLine(prompt).lowercase()) {
                "y", "yes", "д", "да" -> return true
                "n", "no", "н", "нет" -> return false
                else -> println("  Введите y или n.")
            }
        }
    }

    fun readOptionalBoolean(prompt: String): Boolean? {
        while (true) {
            when (readLine(prompt).lowercase()) {
                "" -> return null
                "y", "yes", "д", "да" -> return true
                "n", "no", "н", "нет" -> return false
                else -> println("  Введите y, n или нажмите Enter.")
            }
        }
    }

    fun readIds(prompt: String): Set<Int>? {
        val tokens = readLine(prompt).split(Regex("[,\\s]+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) {
            return null
        }
        val ids = tokens.map { it.toIntOrNull() ?: return null }
        return ids.toSet()
    }
}
