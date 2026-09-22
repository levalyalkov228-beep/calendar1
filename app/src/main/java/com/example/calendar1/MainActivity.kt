package com.example.calendar1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tasks: MutableList<Task>
    private lateinit var adapter: TaskAdapter

    private var selectedTaskPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.tasksRecyclerView)

        tasks = mutableListOf(
            Task(
                id = 1,
                title = "Подготовить отчёт",
                description = "Закончить практическую работу",
                completed = false,
                date = "25.09.2026"
            ),
            Task(
                id = 2,
                title = "Изучить RecyclerView",
                description = "Разобраться с созданием списка задач",
                completed = false,
                date = "26.09.2026"
            ),
            Task(
                id = 3,
                title = "Сделать домашнее задание",
                description = "Выполнить задания по Android Studio",
                completed = false,
                date = "27.09.2026"
            ),
            Task(
                id = 4,
                title = "Подготовить презентацию",
                description = "Подготовить материалы для защиты",
                completed = false,
                date = "28.09.2026"
            ),
            Task(
                id = 5,
                title = "Повторить материал",
                description = "Повторить основные понятия",
                completed = false,
                date = "29.09.2026"
            )
        )

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TaskAdapter(tasks) { task ->

            selectedTaskPosition = tasks.indexOf(task)

            val intent = Intent(this, TaskDetailActivity::class.java)

            intent.putExtra("title", task.title)
            intent.putExtra("description", task.description)
            intent.putExtra("date", task.date)
            intent.putExtra("completed", task.completed)

            startActivityForResult(intent, 100)
        }

        recyclerView.adapter = adapter
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 &&
            resultCode == RESULT_OK &&
            data != null
        ) {
            val completed = data.getBooleanExtra("completed", false)

            if (completed && selectedTaskPosition != -1) {

                val oldTask = tasks[selectedTaskPosition]

                tasks[selectedTaskPosition] = oldTask.copy(
                    completed = true
                )

                adapter.notifyItemChanged(selectedTaskPosition)
            }
        }
    }
}