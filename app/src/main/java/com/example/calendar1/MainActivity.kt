package com.example.calendar1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var tasksRecyclerView: RecyclerView

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskViewModel: TaskViewModel

    private lateinit var monthTitle: TextView
    private lateinit var selectedDateTitle: TextView

    private var currentMonth: YearMonth = YearMonth.now()
    private var selectedDate: LocalDate = LocalDate.now()

    private var datesWithTasks: Set<String> = emptySet()

    private val databaseDateFormatter =
        DateTimeFormatter.ofPattern("dd.MM.uuuu")

    private val monthFormatter =
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val factory = TaskViewModelFactory(application)
        taskViewModel = ViewModelProvider(
            this,
            factory
        )[TaskViewModel::class.java]

        calendarRecyclerView =
            findViewById(R.id.calendarRecyclerView)

        tasksRecyclerView =
            findViewById(R.id.tasksRecyclerView)

        monthTitle = findViewById(R.id.monthTitle)
        selectedDateTitle = findViewById(R.id.selectedDateTitle)

        setupTaskList()
        setupCalendar()
        setupMonthNavigation()
        setupSwipeToDelete()
        setupAddButton()

        taskViewModel.allTasks.observe(this) { tasks ->
            taskAdapter.updateTasks(
                tasks.filter { it.date == selectedDate.format(databaseDateFormatter) }
            )

            updateDatesWithTasks()
        }

        if (savedInstanceState == null) {
            taskViewModel.seedInitialTasks()
        }

        updateCalendar()
        loadTasksForSelectedDate()
    }

    private fun setupTaskList() {
        tasksRecyclerView.layoutManager =
            LinearLayoutManager(this)

        taskAdapter = TaskAdapter(emptyList()) { task ->
            openEditTask(task)
        }

        tasksRecyclerView.adapter = taskAdapter
    }

    private fun setupCalendar() {
        calendarRecyclerView.layoutManager =
            GridLayoutManager(this, 7)

        calendarAdapter = CalendarAdapter(
            dates = emptyList(),
            selectedDate = selectedDate,
            datesWithTasks = emptySet()
        ) { date ->
            selectedDate = date
            updateCalendar()
            loadTasksForSelectedDate()
        }

        calendarRecyclerView.adapter = calendarAdapter
    }

    private fun setupMonthNavigation() {
        findViewById<TextView>(R.id.previousMonthButton)
            .setOnClickListener {
                currentMonth = currentMonth.minusMonths(1)
                updateCalendar()
            }

        findViewById<TextView>(R.id.nextMonthButton)
            .setOnClickListener {
                currentMonth = currentMonth.plusMonths(1)
                updateCalendar()
            }
    }

    private fun setupAddButton() {
        findViewById<FloatingActionButton>(R.id.addTaskButton)
            .setOnClickListener {
                val intent = Intent(
                    this,
                    AddEditTaskActivity::class.java
                )

                intent.putExtra(
                    "date",
                    selectedDate.format(databaseDateFormatter)
                )

                startActivity(intent)
            }
    }

    private fun updateCalendar() {
        monthTitle.text =
            currentMonth.format(monthFormatter)
                .replaceFirstChar { it.uppercase(Locale("ru")) }

        val firstDay = currentMonth.atDay(1)

        // В календаре неделя начинается с понедельника.
        val emptyCells = firstDay.dayOfWeek.value - 1

        val calendarDates = mutableListOf<LocalDate?>()

        repeat(emptyCells) {
            calendarDates.add(null)
        }

        for (day in 1..currentMonth.lengthOfMonth()) {
            calendarDates.add(currentMonth.atDay(day))
        }

        // Добавляем пустые ячейки, чтобы сетка состояла из полных недель.
        while (calendarDates.size % 7 != 0) {
            calendarDates.add(null)
        }

        calendarAdapter.updateCalendar(
            newDates = calendarDates,
            newSelectedDate = selectedDate,
            newDatesWithTasks = datesWithTasks
        )

        selectedDateTitle.text =
            "Задачи на ${selectedDate.format(databaseDateFormatter)}"
    }

    private fun loadTasksForSelectedDate() {
        val dateString =
            selectedDate.format(databaseDateFormatter)

        selectedDateTitle.text = "Задачи на $dateString"

        taskViewModel.getTasksByDate(dateString) { tasks ->
            taskAdapter.updateTasks(tasks)
        }
    }

    private fun updateDatesWithTasks() {
        taskViewModel.getDatesWithTasks { dates ->
            datesWithTasks = dates
            updateCalendar()
        }
    }

    private fun openEditTask(task: Task) {
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
            ): Boolean = false

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.bindingAdapterPosition

                if (position == RecyclerView.NO_POSITION) {
                    return
                }

                val task = taskAdapter.getTaskAt(position)

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Удаление задачи")
                    .setMessage(
                        "Вы действительно хотите удалить задачу \"${task.title}\"?"
                    )
                    .setPositiveButton("Удалить") { _, _ ->
                        taskViewModel.deleteTask(task)
                    }
                    .setNegativeButton("Отмена") { _, _ ->
                        taskAdapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        taskAdapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }

        ItemTouchHelper(swipeCallback)
            .attachToRecyclerView(tasksRecyclerView)
    }
}
