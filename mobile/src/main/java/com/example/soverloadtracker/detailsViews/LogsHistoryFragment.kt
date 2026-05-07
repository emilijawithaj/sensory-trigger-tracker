package com.example.soverloadtracker.detailsViews

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.util.TypedValue.applyDimension
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.soverloadtracker.FrequencyCalcHelper
import com.example.soverloadtracker.R
import com.example.soverloadtracker.SqLiteDatabase
import com.google.android.material.card.MaterialCardView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class LogsHistoryFragment : Fragment() {
    val database by lazy { SqLiteDatabase.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        inflater.inflate(R.layout.fragment_details_logs, container, false)!!

    override fun onResume() {
        super.onResume()
        generateLogCards()
        fillCalendar()
    }

    /**
     * Insert a calendar and populate it with highlights of the days a log has been recorded
     */
    fun fillCalendar() {
        val calendarView =
            requireView().findViewById<com.kizitonwose.calendar.view.CalendarView>(R.id.calendarView)
        val monthText = requireView().findViewById<TextView>(R.id.calendarMonthText)

        val logs = database.listLogRecords().sortedByDescending { it.dateTime }
        val loggedDates =
            logs.map { LocalDate.ofInstant(it.dateTime, ZoneId.systemDefault()) }.toSet()

        //set up calendar binder for the days
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                //highlight if a log exists on this date
                if (loggedDates.contains(data.date)) {
                    container.textView.setBackgroundResource(R.drawable.calendar_highlight)
                } else {
                    container.textView.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }

        //initialise calendar
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12) // show up to 1 year back
        val endMonth = currentMonth.plusMonths(1)

        // set and change month title
        calendarView.monthScrollListener = { month ->
            val formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")
            monthText.text = month.yearMonth.format(formatter)
        }

        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)
    }

    /**
     * Generates Material Cards of each existing log into the logs_section layout summarising each recorded log
     */
    private fun generateLogCards() {
        val logs = database.listLogRecords().sortedByDescending { it.dateTime }
        val containter = requireView().findViewById<LinearLayout>(R.id.logs_section)
        containter.removeAllViews()
        for (log in logs) {

            //card settings
            val card = MaterialCardView(requireContext()).apply {
                //description for testing
                contentDescription = "${log.dateTime}"

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(
                        0,
                        0,
                        0,
                        applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            16f,
                            resources.displayMetrics
                        ).toInt()
                    )
                }
                radius = applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics)
                strokeWidth = applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1f,
                    resources.displayMetrics
                ).toInt()

                setOnClickListener {
                    val intent = Intent(requireContext(), EditLogActivity()::class.java)
                    intent.putExtra("LOG_TIMESTAMP", log.dateTime.toString())
                    startActivity(intent)
                }
                isClickable = true
                isFocusable = true
            }

            //layout within each card
            val innerLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                val inPX24 = applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    24f,
                    resources.displayMetrics
                ).toInt()
                setPadding(inPX24, inPX24, inPX24, inPX24)
            }

            //title
            val title = TextView(requireContext()).apply {
                val formatter = java.time.format.DateTimeFormatter
                    .ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM)
                    .withZone(ZoneId.systemDefault())
                text = formatter.format(log.dateTime)
            }

            //triggers
            val triggers = FrequencyCalcHelper.getTrueFactors(requireContext(), log)
            val triggerSummary = if (triggers.isEmpty()) {
                getString(R.string.log_no_factors)
            } else {
                triggers.joinToString(separator = ", ")
            }

            val bodyText = TextView(requireContext()).apply {
                text = triggerSummary
                alpha = 0.7f
                setPadding(
                    0,
                    applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        8f,
                        resources.displayMetrics
                    ).toInt(),
                    0,
                    0
                )
            }

            //complete
            innerLayout.addView(title)
            innerLayout.addView(bodyText)
            card.addView(innerLayout)
            containter.addView(card)
        }
    }
}

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.calendarDayText)
}