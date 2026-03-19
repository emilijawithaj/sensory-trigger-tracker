package com.example.soverloadtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.util.TypedValue.applyDimension
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soverloadtracker.WatchListenerService.Companion.sendSettingsUpdate
import com.example.soverloadtracker.detailsViews.DetailsActivity
import com.example.soverloadtracker.detailsViews.EditLogActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {
    val database by lazy { SqLiteDatabase.getInstance(this) }

    //sync service
    private val syncReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshUI()
            Snackbar.make(findViewById(R.id.main), "Data Synced", Snackbar.LENGTH_SHORT).show()

            //launch edit after mark end
            val prefs = getSharedPreferences("SOverloadSettings", MODE_PRIVATE)
            if (prefs.getBoolean("launchingEdit", false)) {
                prefs.edit().apply {
                    putBoolean("launchingEdit", false)
                    apply()
                }

                val mostRecentLog = database.listLogRecords().maxByOrNull { it.dateTime }
                val intent = Intent(context, EditLogActivity::class.java)
                intent.putExtra("LOG_TIMESTAMP", mostRecentLog?.dateTime.toString())
                startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        registerReceiver(
            syncReceiver,
            IntentFilter("com.example.soverloadtracker.SYNC_COMPLETE"),
            RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(syncReceiver)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        //handle watch triggers
        val watchMessage = intent.getBooleanExtra("markEnd", false)
        val context = this
        val prefs = getSharedPreferences("SOverloadSettings", MODE_PRIVATE)

        if (watchMessage) {
            WatchListenerService.sendSyncRequest(context)

            prefs.edit().apply {
                putBoolean("launchingEdit", true)
                apply()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //database.onUpgrade(database.writableDatabase, 1, 1)

        //button listeners
        val syncButton: Button = findViewById(R.id.sync)
        syncButton.setOnClickListener {
            WatchListenerService.sendSyncRequest(this)
            Snackbar.make(findViewById(R.id.main), "Sync Requested", Snackbar.LENGTH_SHORT).show()
        }

        val triggerEditButton: Button = findViewById(R.id.trigger_list_button)
        triggerEditButton.setOnClickListener {
            val intent = Intent(this, EditTriggersActivity::class.java)
            startActivity(intent)
        }

        val detailsButton: Button = findViewById(R.id.details_button)
        detailsButton.setOnClickListener {
            val intent = Intent(this, DetailsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        refreshUI()

        //update settings if necessary
        val prefs = getSharedPreferences("SOverloadSettings", MODE_PRIVATE)
        if (prefs.getBoolean("autoTracking", true)) {
            autoSettingsSet()
        }
    }

    /**
     * Fully refreshes the UI
     */
    fun refreshUI() {
        //configure values on page
        val list = database.listLogRecords()
        val logText = findViewById<TextView>(R.id.stat_total_count)
        logText.text = getString(R.string.no_of_logs, list.size)

        generateTriggerChips()
        generateCommonFactorCards()
    }

    /**
     * Generates and places chips for the triggers in the trigger table in the database on the UI
     */
    private fun generateTriggerChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.factors_chip_group)
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
                val chip = Chip(this).apply {
                    text = trigger
                    //setChipDrawable(ChipDrawable.createFromAttributes(context, null, 0, com.google.android.material.R.style.Widget_Material3_Chip_Assist))
                    textSize = 15f
                }
                chip.isClickable = false
                chipGroup.addView(chip)
            }
        }
    }

    /**
     * Generates and places cards for the most common logged factors on the UI
     */
    private fun generateCommonFactorCards() {
        val commonFactors = getCommonFactorsAndTags()
        //sort to order by most prevalent
        val sortedFactors = commonFactors.toList().sortedByDescending { it.second }
            .toMap()

        val container = findViewById<LinearLayout>(R.id.common_factors_section)
        container.removeAllViews()

        for (factor in sortedFactors) {
            // Determine color based on frequency percentage
            val highColThreshold = 70
            val mediumColThreshold = 50

            val colorAttr = when {
                factor.value >= highColThreshold -> getColor(R.color.md_theme_primaryContainer)
                factor.value >= mediumColThreshold -> getColor(R.color.md_theme_secondaryContainer)
                else -> getColor(R.color.md_theme_tertiaryContainer)
            }

            val onColorAttr = when {
                factor.value >= highColThreshold -> getColor(R.color.md_theme_onPrimaryContainer_highContrast)
                factor.value >= mediumColThreshold -> getColor(R.color.md_theme_onSecondaryFixed)
                else -> getColor(R.color.md_theme_onTertiaryFixedVariant)
            }


            //card settings
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(
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
                radius =
                    applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics)
                strokeWidth = 0
                setCardBackgroundColor(colorAttr)
            }

            //layout within each card
            val innerLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val inPX20 = applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    20f,
                    resources.displayMetrics
                ).toInt()
                setPadding(inPX20, inPX20, inPX20, inPX20)
            }

            //title
            val title = TextView(this).apply {
                text = factor.key
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(onColorAttr)
            }

            //count
            val bodyText = TextView(this).apply {
                text =
                    getString(R.string.common_factors_frequency, factor.value)
                setTextColor(onColorAttr)
                alpha = 0.8f
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
            container.addView(card)
        }
    }

    /**
     * Gets and builds a map of factors and tags that occur >= 35% of the time
     * @return Map of factors/tags to their percentage frequency
     */
    private fun getCommonFactorsAndTags(): Map<String, Int> {
        //threshold
        val commonThreshold = 35

        val commonFactors = mutableMapOf<String, Int>()

        //get frequencies
        val logs = database.listLogRecords()
        val tags = database.listTagRecords()
        val factorFrequencies = FrequencyCalcHelper.calculateFactorPercentages(this, logs)
        val tagFrequencies = FrequencyCalcHelper.calculateTagPercentages(tags, logs.size)

        //filter
        for (factor in factorFrequencies) {
            if (factor.value >= commonThreshold) {
                commonFactors[factor.key] = factor.value

            }
        }
        for (tag in tagFrequencies) {
            if (tag.value >= commonThreshold) {
                commonFactors[tag.key] = tag.value
            }
        }

        return commonFactors
    }

    /**
     * Checks for factors to be autotracked and triggers a sending of them to the watch
     */
    fun autoSettingsSet() {
        val highFrequencyThreshold = 60

        val triggerList = database.getTriggers()
        val allLogs = database.listLogRecords()
        val frequencyMap = FrequencyCalcHelper.calculateFactorPercentages(this, allLogs)

        var brightLight = false
        var strobeLight = false
        var loudSound = false

        //check trigger db
        if (triggerList.contains(getString(R.string.factor_brightness))) {
            brightLight = true
        }
        if (triggerList.contains(getString(R.string.factor_strobing))) {
            strobeLight = true
        }
        if (triggerList.contains(getString(R.string.factor_loud))) {
            loudSound = true
        }

        //check by frequency
        if (frequencyMap[getString(R.string.factor_brightness)]!! > highFrequencyThreshold) {
            brightLight = true
        }
        if (frequencyMap[getString(R.string.factor_strobing)]!! > highFrequencyThreshold) {
            strobeLight = true
        }
        if (frequencyMap[getString(R.string.factor_loud)]!! > highFrequencyThreshold) {
            loudSound = true
        }

        sendSettingsUpdate(this, brightLight, strobeLight, loudSound)
    }

}