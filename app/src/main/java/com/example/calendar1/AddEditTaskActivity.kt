
package com.example.calendar1

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var taskViewModel: TaskViewModel

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var completedCheckBox: CheckBox
    private lateinit var saveButton: Button
    private lateinit var formTitle: TextView

    private var taskId = -1
    private var selectedDate = ""
    private var selectedTime = "12:00"
    private var isDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_task)

        val factory = TaskViewModelFactory(application)
        taskViewModel = ViewModelProvider(
            this, factory
        )[TaskViewModel::class.java]

        titleInput = findViewById(R.id.editTaskTitle)
        descriptionInput = findViewById(R.id.editTaskDescription)
        dateButton = findViewById(R.id.buttonSelectDate)
        timeButton = findViewById(R.id.buttonSelectTime)
        completedCheckBox = findViewById(R.id.checkTaskCompleted)
        saveButton = findViewById(R.id.buttonSaveTask)
        formTitle = findViewById(R.id.textFormTitle)

        taskId = intent.getIntExtra("task_id", -1)

        if (taskId != -1) {
            loadTaskData()
        } else {
            formTitle.text = "Новая задача"
            selectedDate = getCurrentDate()
            updateDateButton()
            updateTimeButton()
        }

        dateButton.setOnClickListener {
            showDatePicker()
        }

        timeButton.setOnClickListener {
            showTimePicker()
        }

        saveButton.setOnClickListener {
            saveTask()
        }
    }

    private fun loadTaskData() {
        formTitle.text = "Редактирование задачи"

        titleInput.setText(intent.getStringExtra("title") ?: "")
        descriptionInput.setText(
            intent.getStringExtra("description") ?: ""
        )

        selectedDate = intent.getStringExtra("date")
            ?: getCurrentDate()

        selectedTime = intent.getStringExtra("time")
            ?: "12:00"

        isDone = intent.getBooleanExtra("is_done", false)
        completedCheckBox.isChecked = isDone

        updateDateButton()
        updateTimeButton()
    }

    private fun getCurrentDate(): String {
        val format = SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        )
        return format.format(Calendar.getInstance().time)
    }

    private fun updateDateButton() {
        dateButton.text = "Дата: $selectedDate"
    }

    private fun updateTimeButton() {
        timeButton.text = "Время: $selectedTime"
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        try {
            val format = SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault()
            )
            val parsedDate = format.parse(selectedDate)

            if (parsedDate != null) {
                calendar.time = parsedDate
            }
        } catch (_: Exception) {
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = String.format(
                    Locale.getDefault(),
                    "%02d.%02d.%04d",
                    day,
                    month + 1,
                    year
                )
                updateDateButton()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val parts = selectedTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                selectedTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    selectedHour,
                    selectedMinute
                )
                updateTimeButton()
            },
            hour,
            minute,
            true
        ).show()
    }

    private fun saveTask() {
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        if (title.isEmpty()) {
            titleInput.error = "Введите название задачи"
            titleInput.requestFocus()
            return
        }

        if (selectedDate.isBlank() || selectedTime.isBlank()) {
            Toast.makeText(
                this,
                "Выберите дату и время",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val task = Task(
            id = if (taskId == -1) 0 else taskId,
            title = title,
            description = description,
            date = selectedDate,
            time = selectedTime,
            isDone = completedCheckBox.isChecked
        )

        if (taskId == -1) {
            taskViewModel.addTask(task)
            Toast.makeText(
                this,
                "Задача добавлена",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            taskViewModel.updateTask(task)
            Toast.makeText(
                this,
                "Задача обновлена",
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }
}
