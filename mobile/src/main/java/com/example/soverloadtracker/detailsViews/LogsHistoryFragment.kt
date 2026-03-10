package com.example.soverloadtracker.detailsViews

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.util.TypedValue.applyDimension
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.soverloadtracker.FrequencyCalcHelper
import com.example.soverloadtracker.R
import com.example.soverloadtracker.SqLiteDatabase
import com.google.android.material.card.MaterialCardView

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
                    .withZone(java.time.ZoneId.systemDefault())
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