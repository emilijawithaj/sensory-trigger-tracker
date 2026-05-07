package com.example.soverloadtracker.detailsViews

import android.os.Bundle
import android.util.TypedValue
import android.util.TypedValue.applyDimension
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soverloadtracker.FrequencyCalcHelper
import com.example.soverloadtracker.R
import com.example.soverloadtracker.SqLiteDatabase
import com.example.soverloadtracker.WatchListenerService
import com.example.soverloadtracker.dataStorage.LogData
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.time.Instant

class EditLogActivity(): AppCompatActivity() {
    val database by lazy { SqLiteDatabase.getInstance(this) }
    private lateinit var log: LogData
    private lateinit var newLog: LogData
    private lateinit var activityRootView: View

    private lateinit var brightLightChk: CheckBox
    private lateinit var strobeLightChk: CheckBox
    private lateinit var lightOtherChk: CheckBox
    private lateinit var loudSoundChk: CheckBox
    private lateinit var otherSoundChk: CheckBox
    private lateinit var smellStrongChk: CheckBox
    private lateinit var smellOtherChk: CheckBox
    private lateinit var textureTouchChk: CheckBox
    private lateinit var personalSpaceChk: CheckBox
    private lateinit var touchOtherChk: CheckBox
    private lateinit var strongTasteChk: CheckBox
    private lateinit var badTasteChk: CheckBox
    private lateinit var otherTasteChk: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_log)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //get log data
        val timestampString = intent.getStringExtra("LOG_TIMESTAMP")
        if (timestampString != null) {
            val timestamp = Instant.parse(timestampString)
            //refetch the full log object from db
            log = database.retrieveLog(timestamp)!!
            newLog = database.retrieveLog(timestamp)!!
        } else {
            //finish activity if fail to get log
            finish()
            return
        }

        val title = findViewById<TextView>(R.id.edit_log_title)
        val formatter = java.time.format.DateTimeFormatter
            .ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM)
            .withZone(java.time.ZoneId.systemDefault())
        title.text = getString(R.string.edit_log_title, formatter.format(log.dateTime))

        activityRootView = findViewById(R.id.main)

        brightLightChk = findViewById(R.id.chk_bright_light)
        strobeLightChk = findViewById(R.id.chk_strobe_light)
        lightOtherChk = findViewById(R.id.chk_other_light)
        loudSoundChk = findViewById(R.id.chk_loud_sound)
        otherSoundChk = findViewById(R.id.chk_other_sound)
        smellStrongChk = findViewById(R.id.chk_strong_smell)
        smellOtherChk = findViewById(R.id.chk_other_smell)
        textureTouchChk = findViewById(R.id.chk_bad_touch)
        personalSpaceChk = findViewById(R.id.chk_personal_touch)
        touchOtherChk = findViewById(R.id.chk_other_touch)
        strongTasteChk = findViewById(R.id.chk_strong_taste)
        badTasteChk = findViewById(R.id.chk_bad_taste)
        otherTasteChk = findViewById(R.id.chk_other_taste)

        //save button handler
        val button = findViewById<View>(R.id.btn_save_log)
        button.setOnClickListener {
            updateDatabase()
            finish()
        }

        //add button handler
        val addButton: Button = findViewById(R.id.fab_add_tags)
        addButton.setOnClickListener {
            showAddTagDialog()
        }

        //delete button handler
        val deleteButton: Button = findViewById(R.id.btn_delete_log)
        deleteButton.setOnClickListener {
            database.deleteLog(log)
            WatchListenerService.sendDeleteRequest(this, log.dateTime.toString())
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        generateTagChips()
        updateCheckBoxes()
    }

    /**
     * Presets the checkboxes for factors to be checked if they are present in the log
     */
    fun updateCheckBoxes() {
        if (log.wasBright) {
            brightLightChk.isChecked = true
        }
        if (log.luxStdev > FrequencyCalcHelper.strobingLightDef) {
            strobeLightChk.isChecked = true
        }
        if (log.lightOther) {
            lightOtherChk.isChecked = true
        }
        if (log.wasLoud) {
            loudSoundChk.isChecked = true
        }
        if (log.noiseOther) {
            otherSoundChk.isChecked = true
        }
        if (log.smellStrong) {
            smellStrongChk.isChecked = true
        }
        if (log.smellOther) {
            smellOtherChk.isChecked = true
        }
        if (log.tactileBad) {
            textureTouchChk.isChecked = true
        }
        if (log.tactilePersonalContact) {
            personalSpaceChk.isChecked = true
        }
        if (log.tactileOther) {
            touchOtherChk.isChecked = true
        }
        if (log.tasteStrong) {
            strongTasteChk.isChecked = true
        }
        if (log.tasteBad) {
            badTasteChk.isChecked = true
        }
        if (log.tasteOther) {
            otherTasteChk.isChecked = true
        }
    }

    /**
     * Generates and places chips of tags
     */
    private fun generateTagChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.tags_chip_group)
        chipGroup.removeAllViews()

        //generate triggers from db
        if (newLog.tags.isEmpty()) {
            val emptyChip = Chip(this).apply {
                text = context.getString(R.string.no_tags_to_show)
                isEnabled = false
            }
            chipGroup.addView(emptyChip)
        } else {
            for (tag in newLog.tags) {
                //val tagObj = Tag(tag, log.dateTime)

                val chip = Chip(
                    ContextThemeWrapper(
                        this,
                        com.google.android.material.R.style.Widget_Material3_Chip_Input
                    )
                ).apply {
                    text = tag
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { newLog.tags.remove(tag); generateTagChips() }

                    //adjust sizes using DP and SP scaling
                    chipMinHeight = applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics)
                    textSize = 16f
                    closeIconSize = applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, resources.displayMetrics)
                }
                chipGroup.addView(chip)
            }
        }
    }

    fun updateDatabase() {
        //update factors
        if (strobeLightChk.isChecked && log.luxStdev != 999f) {
            newLog.luxStdev = 999f
        }
        else if (!strobeLightChk.isChecked && log.luxStdev > FrequencyCalcHelper.strobingLightDef) {
            newLog.luxStdev = -1f
        }
        else {
            newLog.luxStdev = log.luxStdev
        }


        //handle other
        newLog.wasBright = brightLightChk.isChecked
        newLog.wasLoud =  loudSoundChk.isChecked
        newLog.lightOther = lightOtherChk.isChecked
        newLog.noiseOther = otherSoundChk.isChecked
        newLog.smellOther = smellOtherChk.isChecked
        newLog.tactileOther = touchOtherChk.isChecked
        newLog.tasteOther = otherTasteChk.isChecked
        newLog.tactilePersonalContact = personalSpaceChk.isChecked
        newLog.tactileBad = textureTouchChk.isChecked
        newLog.smellStrong = smellStrongChk.isChecked
        newLog.tasteStrong = strongTasteChk.isChecked
        newLog.tasteBad = badTasteChk.isChecked

        //update log
        database.updateLogRecord(newLog.dateTime, newLog)
    }

    /**
     * Summons the add tag dialog, collecting a String tag saving it to the new Log object
     */
    fun showAddTagDialog() {
        //inflate and identify input field
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_tag, null)
        val tagText = dialogView.findViewById<EditText>(R.id.trigger_input)

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_ok_text)) { dialog, _ ->

                //add trigger to db
                val tag = tagText.text.toString()

                if (tag.isNotEmpty()) {
                    newLog.tags.add(tag)

                    //success and close
                    val snackbar =
                        Snackbar.make(activityRootView, "Data Saved", Snackbar.LENGTH_SHORT)
                    snackbar.show()
                    dialog.dismiss()
                    generateTagChips()
                }
                else {
                    //show error
                    val snackbar =
                        Snackbar.make(activityRootView, "Tag cannot be empty", Snackbar.LENGTH_SHORT)
                    snackbar.show()
                }
            }
            .show()

    }
}