package com.example.calendar1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var taskViewModel: TaskViewModel

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
        setupSwipeToDelete()

        taskViewModel.allTasks.observe(this) { updatedTasks ->
            adapter.updateTasks(updatedTasks)
        }

        if (savedInstanceState == null) {
            taskViewModel.seedInitialTasks()
        }

        val addTaskButton =
            findViewById<FloatingActionButton>(R.id.addTaskButton)

        addTaskButton.setOnClickListener {
            val intent = Intent(
                this,
                AddEditTaskActivity::class.java
            )
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TaskAdapter(emptyList()) { task ->

            val intent = Intent(
                this,
                AddEditTaskActivity::class.java
            )

            intent.putExtra("task_id", task.id)
            intent.putExtra("title", task.title)
            intent.putExtra("description", task.description)
            intent.putExtra("date", task.date)
            intent.putExtra("time", task.time)
            intent.putExtra("is_done", task.isDone)

            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }

    private fun setupSwipeToDelete() {

        val swipeCallback = object :
            ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.bindingAdapterPosition

                if (position == RecyclerView.NO_POSITION) {
                    return
                }

                val task = adapter.getTaskAt(position)

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Удаление задачи")
                    .setMessage(
                        "Вы действительно хотите удалить задачу \"${task.title}\"?"
                    )
                    .setPositiveButton("Удалить") { _, _ ->
                        taskViewModel.deleteTask(task)
                    }
                    .setNegativeButton("Отмена") { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }

        ItemTouchHelper(swipeCallback)
            .attachToRecyclerView(recyclerView)
    }
}
