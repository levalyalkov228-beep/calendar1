package com.example.calendar1

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate

class CalendarAdapter(
    private var dates: List<LocalDate?>,
    private var selectedDate: LocalDate,
    private var datesWithTasks: Set<String>,
    private val onDateClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val dayText: TextView =
            itemView.findViewById(R.id.textCalendarDay)

        val taskDot: View =
            itemView.findViewById(R.id.calendarTaskDot)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CalendarViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)

        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CalendarViewHolder,
        position: Int
    ) {
        val date = dates[position]

        if (date == null) {
            holder.dayText.text = ""
            holder.taskDot.visibility = View.INVISIBLE
            holder.itemView.isClickable = false
            holder.dayText.background = null
            return
        }

        holder.itemView.isClickable = true
        holder.dayText.text = date.dayOfMonth.toString()

        val dateString = date.format(
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.uuuu")
        )

        holder.taskDot.visibility =
            if (datesWithTasks.contains(dateString)) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

        val background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(
                if (date == selectedDate) {
                    Color.parseColor("#6750A4")
                } else {
                    Color.TRANSPARENT
                }
            )
        }

        holder.dayText.background = background

        holder.dayText.setTextColor(
            if (date == selectedDate) {
                Color.WHITE
            } else {
                Color.BLACK
            }
        )

        holder.itemView.setOnClickListener {
            onDateClick(date)
        }
    }

    override fun getItemCount(): Int = dates.size

    fun updateCalendar(
        newDates: List<LocalDate?>,
        newSelectedDate: LocalDate,
        newDatesWithTasks: Set<String>
    ) {
        dates = newDates
        selectedDate = newSelectedDate
        datesWithTasks = newDatesWithTasks
        notifyDataSetChanged()
    }
}
