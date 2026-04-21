package com.example.soverloadtracker


import android.os.Bundle
import android.util.TypedValue
import android.util.TypedValue.applyDimension
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class EditTriggersActivity : AppCompatActivity() {
    val database by lazy { SqLiteDatabase.getInstance(this) }
    private lateinit var activityRootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_triggers)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        activityRootView = findViewById(R.id.main)

        //add button handler
        val addButton: Button = findViewById(R.id.add_trigger_button)
        addButton.setOnClickListener {
            showAddTriggerDialog()
        }
    }

    override fun onResume() {
        super.onResume()

        generateTriggerChips()
        generateSuggestedChips()
    }

    /**
     * Summons the add trigger dialog, collecting a String trigger and saving it to the trigger database
     */
    fun showAddTriggerDialog() {
        //inflate and identify input field
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_trigger, null)
        val triggerText = dialogView.findViewById<EditText>(R.id.trigger_input)

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_ok_text)) { dialog, _ ->

                //add trigger to db
                val trigger = triggerText.text.toString()

                if (trigger.isNotEmpty()) {
                    database.addTrigger(trigger)

                    //success and close
                    val snackbar =
                        Snackbar.make(activityRootView,
                            getString(R.string.data_saved), Snackbar.LENGTH_SHORT)
                    snackbar.show()
                    dialog.dismiss()
                    generateTriggerChips()
                    generateSuggestedChips()
                }
                else {
                    //show error
                    val snackbar =
                        Snackbar.make(activityRootView,
                            getString(R.string.trigger_cannot_be_empty), Snackbar.LENGTH_SHORT)
                    snackbar.show()
                }
            }
            .show()

    }

    /**
     * Generates and places chips for the triggers in the trigger table in the database on the UI
     */
    private fun generateTriggerChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.existing_factors_chip_group)
        chipGroup.removeAllViews()
        val triggerList = database.getTriggers()

        //generate triggers from db
        if (triggerList.isEmpty()) {
            val emptyChip = Chip(this).apply {
                text = context.getString(R.string.no_triggers_text)
                isEnabled = false
            }
            chipGroup.addView(emptyChip)
        } else {
            for (trigger in triggerList) {
                val chip = Chip(
                    ContextThemeWrapper(
                        this,
                        com.google.android.material.R.style.Widget_Material3_Chip_Input
                    )
                ).apply {
                    text = trigger
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { database.deleteTrigger(trigger); generateTriggerChips(); generateSuggestedChips() }

                    //adjust sizes using DP and SP scaling
                    chipMinHeight = applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics)
                    textSize = 16f
                    closeIconSize = applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, resources.displayMetrics)
                }
                chipGroup.addView(chip)
            }
        }
    }
    
    fun generateSuggestedChips() {
        //threshold to suggest
        val threshold = 50
        
        val factors = mutableMapOf<String, Int>()

        val chipGroup = findViewById<ChipGroup>(R.id.suggested_factors_chip_group)
        chipGroup.removeAllViews()

        //get values
        val logs = database.listLogRecords()
        val tags = database.listTagRecords()
        val factorFrequencies = FrequencyCalcHelper.calculateFactorPercentages(this, logs)
        val tagFrequencies = FrequencyCalcHelper.calculateTagPercentages(tags, logs.size)
        val existingTriggers = database.getTriggers()

        //filter by commonality
        for (factor in factorFrequencies) {
            if (factor.value >= threshold && !existingTriggers.contains(factor.key)) {
                factors[factor.key] = factor.value
            }
        }
        for (tag in tagFrequencies) {
            if (tag.value >= threshold && !existingTriggers.contains(tag.key)) {
                factors[tag.key] = tag.value
            }
        }

        //order
        val sortedFactors = factors.toList().sortedByDescending { it.second }
            .toMap()
        
        //generate chips
        if (sortedFactors.isEmpty()) {
            val emptyChip = Chip(this).apply {
                text = context.getString(R.string.no_suggestions_text)
                isEnabled = false
            }
            chipGroup.addView(emptyChip)
        } else {
            for (sug in sortedFactors) {
                val chip = Chip(
                    ContextThemeWrapper(
                        this,
                        com.google.android.material.R.style.Widget_Material3_Chip_Input
                    )
                ).apply {
                    text = sug.key
                    setOnClickListener { database.addTrigger(sug.key); generateTriggerChips(); generateSuggestedChips() }

                    //adjust sizes using DP and SP scaling
                    chipMinHeight = applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics)
                    textSize = 16f }
                chipGroup.addView(chip)
            }
        }
        
    }
}