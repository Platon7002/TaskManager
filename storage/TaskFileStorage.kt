package storage

import model.Task
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class TaskFileStorage {

    fun save(path: String, tasks: List<Task>) {
        try {
            val target = File(path)
            val temp = File("$path.tmp")
            temp.writeText(TaskJsonMapper.toJson(tasks), Charsets.UTF_8)
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            throw StorageException("Не удалось записать файл «$path»: ${e.message}", e)
        } catch (e: SecurityException) {
            throw StorageException("Нет прав на запись в файл «$path»", e)
        }
    }

    fun load(path: String): List<Task> {
        val file = File(path)
        if (!file.exists()) {
            throw StorageException("Файл не найден: ${file.absolutePath}")
        }
        if (!file.isFile) {
            throw StorageException("«$path» — это не файл")
        }
        val text = try {
            file.readText(Charsets.UTF_8).removePrefix("﻿")
        } catch (e: IOException) {
            throw StorageException("Не удалось прочитать файл «$path»: ${e.message}", e)
        } catch (e: SecurityException) {
            throw StorageException("Нет прав на чтение файла «$path»", e)
        }
        if (text.isBlank()) {
            throw StorageException("Файл «$path» пустой")
        }
        try {
            return TaskJsonMapper.fromJson(text)
        } catch (e: JsonFormatException) {
            throw StorageException("Файл «$path» повреждён или имеет неверный формат: ${e.message}", e)
        }
    }
}
