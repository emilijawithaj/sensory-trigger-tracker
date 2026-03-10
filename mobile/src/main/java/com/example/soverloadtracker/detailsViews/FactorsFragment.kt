package com.example.soverloadtracker.detailsViews

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
import com.example.soverloadtracker.dataStorage.LogData
import com.example.soverloadtracker.dataStorage.Tag
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

        val brightLightPercentage = factorMap.getValue(getString(R.string.factor_brightness))
        val strobeLightPercentage = factorMap.getValue(getString(R.string.factor_strobing))
        val lightOtherPercentage = factorMap.getValue(getString(R.string.factor_light_manual))
        val loudSoundPercentage = factorMap.getValue(getString(R.string.factor_loud))
        val otherSoundPercentage = factorMap.getValue(getString(R.string.factor_noise_manual))
        val smellStrongPercentage = factorMap.getValue(getString(R.string.factor_smell_strong))
        val smellOtherPercentage = factorMap.getValue(getString(R.string.factor_smell_other))
        val textureTouchPercentage = factorMap.getValue(getString(R.string.factor_touch_texture))
        val personalSpacePercentage = factorMap.getValue(getString(R.string.factor_personal_space))
        val touchOtherPercentage = factorMap.getValue(getString(R.string.factor_touch_other))
        val strongTastePercentage = factorMap.getValue(getString(R.string.factor_taste_strong))
        val badTastePercentage = factorMap.getValue(getString(R.string.factor_taste_bad))
        val otherTastePercentage = factorMap.getValue(getString(R.string.factor_taste_other))

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