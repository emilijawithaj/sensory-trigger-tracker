package com.example.soverloadtracker

import android.content.Context
import com.example.soverloadtracker.dataStorage.LogData
import com.example.soverloadtracker.dataStorage.Tag

/**
 * Class of static helper functions for log data processing
 */
class FrequencyCalcHelper {

    companion object {
        //default threshold values
        var loudSoundDef = 60
        var brightLightDef = 250f
        var strobingLightDef = 500

        /**
         * Extracts tag values from a list of tag objects
         * @param tags List of Tags
         * @return List of tag names as strings
         */
        fun extractTagNames(tags: List<Tag>): ArrayList<String> {
            val tagNames = arrayListOf<String>()

            for (tag in tags) {
                tagNames.add(tag.name)
            }

            return tagNames
        }

        /**
         * Calculates the number of times each tag has appeared, and returns a Map of this
         * @param tags The tags to be evaluated
         * @return Map of tag titles to their number of occurrences
         */
        fun calculateTagFrequency(tags: List<Tag>): Map<String, Int> {
            if (tags.isEmpty()) return emptyMap()

            return tags
                .map { it.name }
                .groupingBy { it.lowercase() } //lowercase for no case sensitivity
                .eachCount()
        }
        /**
         * Calculates the percentage of times each tag has appeared, and returns a Map of this
         * @param tags The tags to be evaluated
         * @param total Number of logs
         * @return Map of tag titles to their percentage of occurrences
         */
        fun calculateTagPercentages(tags: List<Tag>, total: Int): Map<String, Int> {
            val frequencies = mutableMapOf<String, Int>()
            if (tags.isEmpty()) return frequencies
            val map = calculateTagFrequency(tags)

            for (tag in map) {
                frequencies[tag.key] = (tag.value.toDouble() / total * 100).toInt()
            }

            return frequencies
        }

        /**
         * Calculates the number of times each non-tag factor has appeared, and returns a Map of this
         * @param context Context
         * @param logs The tags to be evaluated
         * @return Map of factor titles to their number of occurrences
         */
        fun calculateFactorFrequency(context: Context, logs: List<LogData>): Map<String, Int> {
            val frequencies = mutableMapOf<String, Int>()
            if (logs.isEmpty()) return frequencies

            //count all values
            var brightLightCount = 0
            var strobeLightCount = 0
            var lightOtherCount = 0
            var loudSoundCount = 0
            var otherSoundCount = 0
            var smellStrongCount = 0
            var smellOtherCount = 0
            var textureTouchCount = 0
            var personalSpaceCount = 0
            var touchOtherCount = 0
            var strongTasteCount = 0
            var badTasteCount = 0
            var otherTasteCount = 0


            for (log in logs) {
                if (log.luxStdev >= strobingLightDef) {
                    strobeLightCount++
                }
                if (log.avgLux >= brightLightDef) {
                    brightLightCount++
                }
                if (log.lightOther) {
                    lightOtherCount++
                }
                if (log.avgDecibels >= loudSoundDef) {
                    loudSoundCount++
                }
                if (log.noiseOther) {
                    otherSoundCount++
                }
                if (log.smellStrong) {
                    smellStrongCount++
                }
                if (log.smellOther) {
                    smellOtherCount++
                }
                if (log.tactileBad) {
                    textureTouchCount++
                }
                if (log.tactilePersonalContact) {
                    personalSpaceCount++
                }
                if (log.tactileOther) {
                    touchOtherCount++
                }
                if (log.tasteStrong) {
                    strongTasteCount++
                }
                if (log.tasteBad) {
                    badTasteCount++
                }
                if (log.tasteOther) {
                    otherTasteCount++
                }
            }

            frequencies[context.getString(R.string.factor_brightness)] = brightLightCount
            frequencies[context.getString(R.string.factor_strobing)] = strobeLightCount
            frequencies[context.getString(R.string.factor_light_manual)] = lightOtherCount
            frequencies[context.getString(R.string.factor_loud)] = loudSoundCount
            frequencies[context.getString(R.string.factor_noise_manual)] = otherSoundCount
            frequencies[context.getString(R.string.factor_smell_strong)] = smellStrongCount
            frequencies[context.getString(R.string.factor_smell_other)] = smellOtherCount
            frequencies[context.getString(R.string.factor_touch_texture)] = textureTouchCount
            frequencies[context.getString(R.string.factor_personal_space)] = personalSpaceCount
            frequencies[context.getString(R.string.factor_touch_other)] = touchOtherCount
            frequencies[context.getString(R.string.factor_taste_strong)] = strongTasteCount
            frequencies[context.getString(R.string.factor_taste_bad)] = badTasteCount
            frequencies[context.getString(R.string.factor_taste_other)] = otherTasteCount

            return frequencies
        }

        /**
         * Calculates the percentage of times each non-tag factor has appeared, and returns a Map of this
         * @param context Context
         * @param logs The logs to be evaluated
         * @return Map of factor titles to their frequency
         */
        fun calculateFactorPercentages(context: Context, logs: List<LogData>): Map<String, Int> {
            val frequencies = mutableMapOf<String, Int>()
            if (logs.isEmpty()) return frequencies

            val factorMap = calculateFactorFrequency(context, logs)

            //calculate percentages
            val total = logs.size
            val brightLightPercentage = (factorMap.getValue(context.getString(R.string.factor_brightness)).toDouble() / total * 100).toInt()
            val strobeLightPercentage = (factorMap.getValue(context.getString(R.string.factor_strobing)).toDouble() / total * 100).toInt()
            val lightOtherPercentage = (factorMap.getValue(context.getString(R.string.factor_light_manual)).toDouble() / total * 100).toInt()
            val loudSoundPercentage = (factorMap.getValue(context.getString(R.string.factor_loud)).toDouble() / total * 100).toInt()
            val otherSoundPercentage = (factorMap.getValue(context.getString(R.string.factor_noise_manual)).toDouble() / total * 100).toInt()
            val smellStrongPercentage = (factorMap.getValue(context.getString(R.string.factor_smell_strong)).toDouble() / total * 100).toInt()
            val smellOtherPercentage = (factorMap.getValue(context.getString(R.string.factor_smell_other)).toDouble() / total * 100).toInt()
            val textureTouchPercentage = (factorMap.getValue(context.getString(R.string.factor_touch_texture)).toDouble() / total * 100).toInt()
            val personalSpacePercentage = (factorMap.getValue(context.getString(R.string.factor_personal_space)).toDouble() / total * 100).toInt()
            val touchOtherPercentage = (factorMap.getValue(context.getString(R.string.factor_touch_other)).toDouble() / total * 100).toInt()
            val strongTastePercentage = (factorMap.getValue(context.getString(R.string.factor_taste_strong)).toDouble() / total * 100).toInt()
            val badTastePercentage = (factorMap.getValue(context.getString(R.string.factor_taste_bad)).toDouble() / total * 100).toInt()
            val otherTastePercentage = (factorMap.getValue(context.getString(R.string.factor_taste_other)).toDouble() / total * 100).toInt()

            frequencies[context.getString(R.string.factor_brightness)] = brightLightPercentage
            frequencies[context.getString(R.string.factor_strobing)] = strobeLightPercentage
            frequencies[context.getString(R.string.factor_light_manual)] = lightOtherPercentage
            frequencies[context.getString(R.string.factor_loud)] = loudSoundPercentage
            frequencies[context.getString(R.string.factor_noise_manual)] = otherSoundPercentage
            frequencies[context.getString(R.string.factor_smell_strong)] = smellStrongPercentage
            frequencies[context.getString(R.string.factor_smell_other)] = smellOtherPercentage
            frequencies[context.getString(R.string.factor_touch_texture)] = textureTouchPercentage
            frequencies[context.getString(R.string.factor_personal_space)] = personalSpacePercentage
            frequencies[context.getString(R.string.factor_touch_other)] = touchOtherPercentage
            frequencies[context.getString(R.string.factor_taste_strong)] = strongTastePercentage
            frequencies[context.getString(R.string.factor_taste_bad)] = badTastePercentage
            frequencies[context.getString(R.string.factor_taste_other)] = otherTastePercentage

            return frequencies
        }

        /**
         * Retrieves all of the flagged factors and tags present in a log
         * @param context Context
         * @param log the log to be evaluated
         * @return List of strings of the factor titles and tags
         */
        fun getTrueFactors(context: Context, log: LogData): List<String> {
            //threshold values
            val loudSoundDef = loudSoundDef
            val brightLightDef = brightLightDef
            val strobingLightDef = strobingLightDef

            val trueFactors = arrayListOf<String>()

            if (log.avgLux > brightLightDef) {
                trueFactors.add(context.getString(R.string.factor_brightness))
            }
            if (log.luxStdev > strobingLightDef) {
                trueFactors.add(context.getString(R.string.factor_strobing))
            }
            if (log.lightOther) {
                trueFactors.add(context.getString(R.string.factor_light_manual))
            }
            if (log.avgDecibels > loudSoundDef) {
                trueFactors.add(context.getString(R.string.factor_loud))
            }
            if (log.noiseOther) {
                trueFactors.add(context.getString(R.string.factor_noise_manual))
            }
            if (log.smellStrong) {
                trueFactors.add(context.getString(R.string.factor_smell_strong))
            }
            if (log.smellOther) {
                trueFactors.add(context.getString(R.string.factor_smell_other))
            }
            if (log.tactileBad) {
                trueFactors.add(context.getString(R.string.factor_touch_texture))
            }
            if (log.tactilePersonalContact) {
                trueFactors.add(context.getString(R.string.factor_personal_space))
            }
            if (log.tactileOther) {
                trueFactors.add(context.getString(R.string.factor_touch_other))
            }
            if (log.tasteStrong) {
                trueFactors.add(context.getString(R.string.factor_taste_strong))
            }
            if (log.tasteBad) {
                trueFactors.add(context.getString(R.string.factor_taste_bad))
            }
            if (log.tasteOther) {
                trueFactors.add(context.getString(R.string.factor_taste_other))
            }
            for (tag in log.tags) {
                trueFactors.add(tag)
            }
            return trueFactors
        }
    }
}