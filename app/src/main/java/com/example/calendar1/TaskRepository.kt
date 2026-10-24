package com.example.calendar1

import androidx.lifecycle.LiveData

class TaskRepository(
    private val taskDao: TaskDao
) {

    val allTasks: LiveData<List<Task>> =
        taskDao.getAllTasks()

    suspend fun getTaskCount(): Int {
        return taskDao.getTaskCount()
    }

    suspend fun addTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun getTasksByDate(date: String): List<Task> {
        return taskDao.getTasksByDate(date)
    }
}
