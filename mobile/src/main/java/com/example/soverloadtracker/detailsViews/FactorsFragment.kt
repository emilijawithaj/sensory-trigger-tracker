package com.example.soverloadtracker.detailsViews

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
import com.example.soverloadtracker.dataStorage.LogData
import com.google.android.material.card.MaterialCardView

class FactorsFragment : Fragment() {
    val database by lazy { SqLiteDatabase.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        inflater.inflate(R.layout.fragment_details_factors, container, false)!!


    override fun onResume() {
        super.onResume()
        generateTagCards()
        populateFactorFrequency(database.listLogRecords())
    }

    /**
     * Generates cards for each tag in the database to be placed under the others
     */
    private fun generateTagCards() {
        val tags = database.listTagRecords()
        val containter = requireView().findViewById<LinearLayout>(R.id.tags_section)
        containter.removeAllViews()
        val tagMap = FrequencyCalcHelper.calculateTagFrequency(tags)

        for (tag in tagMap) {

                //card settings
                val card = MaterialCardView(requireContext()).apply {
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
                    strokeWidth = applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        1f,
                        resources.displayMetrics
                    ).toInt()
                }

                //layout within each card
                val innerLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    val inPX20 = applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        20f,
                        resources.displayMetrics
                    ).toInt()
                    setPadding(inPX20, inPX20, inPX20, inPX20)
                }

                //title
                val title = TextView(requireContext()).apply {
                    text = tag.key
                }

                //count
                val bodyText = TextView(requireContext()).apply {
                    text =
                        getString(R.string.tags_frequency, tag.value)
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

    fun populateFactorFrequency(logs: List<LogData>) {
        //get percentages
        val factorMap = FrequencyCalcHelper.calculateFactorPercentages(requireContext(), logs)


        var brightLightPercentage = 0
        var strobeLightPercentage = 0
        var lightOtherPercentage = 0
        var loudSoundPercentage = 0
        var otherSoundPercentage = 0
        var smellStrongPercentage = 0
        var smellOtherPercentage = 0
        var textureTouchPercentage = 0
        var personalSpacePercentage = 0
        var touchOtherPercentage = 0
        var strongTastePercentage = 0
        var badTastePercentage = 0
        var otherTastePercentage = 0

        if (!logs.isEmpty()) {
            brightLightPercentage = factorMap.getValue(getString(R.string.factor_brightness))
            strobeLightPercentage = factorMap.getValue(getString(R.string.factor_strobing))
            lightOtherPercentage = factorMap.getValue(getString(R.string.factor_light_manual))
            loudSoundPercentage = factorMap.getValue(getString(R.string.factor_loud))
            otherSoundPercentage = factorMap.getValue(getString(R.string.factor_noise_manual))
            smellStrongPercentage = factorMap.getValue(getString(R.string.factor_smell_strong))
            smellOtherPercentage = factorMap.getValue(getString(R.string.factor_smell_other))
            textureTouchPercentage =
                factorMap.getValue(getString(R.string.factor_touch_texture))
            personalSpacePercentage =
                factorMap.getValue(getString(R.string.factor_personal_space))
            touchOtherPercentage = factorMap.getValue(getString(R.string.factor_touch_other))
            strongTastePercentage = factorMap.getValue(getString(R.string.factor_taste_strong))
            badTastePercentage = factorMap.getValue(getString(R.string.factor_taste_bad))
            otherTastePercentage = factorMap.getValue(getString(R.string.factor_taste_other))
        }

        //populate
        val brightLightFreq = requireView().findViewById<TextView>(R.id.bright_light_freq)
        brightLightFreq.text = getString(R.string.common_factors_frequency, brightLightPercentage)
        val strobeLightFreq = requireView().findViewById<TextView>(R.id.strobe_light_freq)
        strobeLightFreq.text = getString(R.string.common_factors_frequency, strobeLightPercentage)
        val lightOtherFreq = requireView().findViewById<TextView>(R.id.other_light_freq)
        lightOtherFreq.text = getString(R.string.common_factors_frequency, lightOtherPercentage)
        val loudSoundFreq = requireView().findViewById<TextView>(R.id.loud_sound_freq)
        loudSoundFreq.text = getString(R.string.common_factors_frequency, loudSoundPercentage)
        val otherSoundFreq = requireView().findViewById<TextView>(R.id.other_sound_freq)
        otherSoundFreq.text = getString(R.string.common_factors_frequency, otherSoundPercentage)
        val strongSmellFreq = requireView().findViewById<TextView>(R.id.strong_smell_freq)
        strongSmellFreq.text = getString(R.string.common_factors_frequency, smellStrongPercentage)
        val otherSmellFreq = requireView().findViewById<TextView>(R.id.other_smell_freq)
        otherSmellFreq.text = getString(R.string.common_factors_frequency, smellOtherPercentage)
        val badTextureFreq = requireView().findViewById<TextView>(R.id.bad_texture_freq)
        badTextureFreq.text = getString(R.string.common_factors_frequency, textureTouchPercentage)
        val personalSpaceFreq = requireView().findViewById<TextView>(R.id.personal_space_freq)
        personalSpaceFreq.text = getString(R.string.common_factors_frequency, personalSpacePercentage)
        val otherTouchFreq = requireView().findViewById<TextView>(R.id.other_touch_freq)
        otherTouchFreq.text = getString(R.string.common_factors_frequency, touchOtherPercentage)
        val strongTasteFreq = requireView().findViewById<TextView>(R.id.strong_taste_freq)
        strongTasteFreq.text = getString(R.string.common_factors_frequency, strongTastePercentage)
        val badTasteFreq = requireView().findViewById<TextView>(R.id.bad_taste_freq)
        badTasteFreq.text = getString(R.string.common_factors_frequency, badTastePercentage)
        val otherTasteFreq = requireView().findViewById<TextView>(R.id.other_taste_freq)
        otherTasteFreq.text = getString(R.string.common_factors_frequency, otherTastePercentage)
    }
}