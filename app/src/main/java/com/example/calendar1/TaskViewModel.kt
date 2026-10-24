package com.example.calendar1

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val allTasks: LiveData<List<Task>> =
        repository.allTasks

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun seedInitialTasks() {
        viewModelScope.launch {
            if (repository.getTaskCount() == 0) {
                val initialTasks = listOf(
                    Task(
                        title = "Подготовить отчёт",
                        description = "Закончить практическую работу",
                        isDone = false,
                        date = "25.09.2026",
                        time = "18:00"
                    ),
                    Task(
                        title = "Изучить RecyclerView",
                        description = "Разобраться с созданием списка задач",
                        isDone = false,
                        date = "26.09.2026",
                        time = "18:00"
                    ),
                    Task(
                        title = "Сделать домашнее задание",
                        description = "Выполнить задания по Android Studio",
                        isDone = false,
                        date = "27.09.2026",
                        time = "18:00"
                    ),
                    Task(
                        title = "Подготовить презентацию",
                        description = "Подготовить материалы для защиты",
                        isDone = false,
                        date = "28.09.2026",
                        time = "18:00"
                    ),
                    Task(
                        title = "Повторить материал",
                        description = "Повторить основные понятия",
                        isDone = false,
                        date = "29.09.2026",
                        time = "18:00"
                    )
                )

                for (task in initialTasks) {
                    repository.addTask(task)
                }
            }
        }
    }
}
