package com.example.calendar1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var taskViewModel: TaskViewModel

    private val tasks = mutableListOf<Task>()
    private var selectedTaskId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val factory = TaskViewModelFactory(application)
        taskViewModel = ViewModelProvider(
            this,
            factory
        )[TaskViewModel::class.java]

        recyclerView = findViewById(R.id.tasksRecyclerView)
        setupRecyclerView()

        // Получаем обновления списка из базы данных Room.
        taskViewModel.allTasks.observe(this) { updatedTasks ->
            tasks.clear()
            tasks.addAll(updatedTasks)
            adapter.updateTasks(updatedTasks)

            android.util.Log.d(
                "ROOM_COUNT",
                "Количество задач в базе: ${updatedTasks.size}"
            )
        }

        // Начальные задачи создаются только при первом создании Activity.
        if (savedInstanceState == null) {
            taskViewModel.seedInitialTasks()
        }

        val addTaskButton =
            findViewById<FloatingActionButton>(R.id.addTaskButton)

        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_TASK)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TaskAdapter(emptyList()) { task ->
            // Запоминаем ID, чтобы обновить именно выбранную задачу.
            selectedTaskId = task.id

            val intent = Intent(this, TaskDetailActivity::class.java)
            intent.putExtra("title", task.title)
            intent.putExtra("description", task.description)
            intent.putExtra("date", task.date)
            intent.putExtra("completed", task.isDone)

            startActivityForResult(intent, REQUEST_TASK_DETAIL)
        }

        recyclerView.adapter = adapter
    }

    @Deprecated("Используется существующий механизм возврата результата")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK || data == null) {
            return
        }

        when (requestCode) {
            REQUEST_TASK_DETAIL -> {
                val completed =
                    data.getBooleanExtra("completed", false)

                if (completed) {
                    val selectedTask =
                        tasks.find { it.id == selectedTaskId }

                    if (selectedTask != null && !selectedTask.isDone) {
                        taskViewModel.updateTask(
                            selectedTask.copy(isDone = true)
                        )
                    }
                }
            }

            REQUEST_ADD_TASK -> {
                val title = data.getStringExtra("title") ?: ""
                val description =
                    data.getStringExtra("description") ?: ""
                val date = data.getStringExtra("date") ?: ""

                val newTask = Task(
                    title = title,
                    description = description,
                    isDone = false,
                    date = date,
                    time = "12:00"
                )

                taskViewModel.addTask(newTask)
            }
        }
    }

    companion object {
        private const val REQUEST_TASK_DETAIL = 100
        private const val REQUEST_ADD_TASK = 200
    }
}
