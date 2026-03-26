package com.example.soverloadtracker.presentation.sensorDataGathering

import android.util.Log
import kotlin.math.sqrt

class SensorDataComputer {
    var bpms : ArrayList<Float> = ArrayList()
    var lightReads: ArrayList<Float> = ArrayList()
    var lightAccuracyLow = false
    var soundReadings : ArrayList<Double> = ArrayList()
    var lightStdev: Float? = null

    //toggles
    companion object {
        var HIGH_LIGHT_LEVEL = 250f
        const val LIGHT_SAMPLE_MIN = 4
        const val STROBING_STDEV_THRESHOLD = 500
        var DECIBEL_THRESHOLD = 60
        const val HIGH_HR_THRESHOLD = 50

//        /**
//         * Updates the thresholds, to be passed data based on previously logged values in the database
//         * @param light new Light threshold
//         * @param db new Loudness threshold
//         */
//        fun updateThresholds(
//            light: Float,
//            db: Int,
//        ) {
//            HIGH_LIGHT_LEVEL = light
//            DECIBEL_THRESHOLD = db
//            Log.d("SensorDataComputer", "Thresholds updated from database")
//        }
    }
    /**
     * Guess whether the user in a strobing light environment based on the
     * standard deviation of the light readings data set.
     * Defaults to false for small sample sizes due to error margin
     * @return If lights likely to be strobing
     */
    fun isLightStrobing(): Boolean {
        //assume false for small sample sizes
        if (lightReads.size < LIGHT_SAMPLE_MIN) {
            Log.d("LOGPROCESS", "Strobing too low sample size.")
            return false
        }
        else if (lightAccuracyLow) {
            Log.d("LOGPROCESS", "Strobing too low light accuracy.")
            return false
        }

        val mean = lightReads.average()
        val sumOfSquaredDifferences = lightReads.sumOf { (it.toDouble() - mean) * (it.toDouble() - mean) }
        val variance = sumOfSquaredDifferences / (lightReads.size - 1)
        val stdev = sqrt(variance)
        lightStdev = stdev.toFloat()

        if (stdev >= STROBING_STDEV_THRESHOLD) {
            Log.d("LOGPROCESS", "Is strobing. Stdev: $stdev")
            return true
        }
        Log.d("LOGPROCESS", "Is not strobing.")
        return false
    }


    /**
     * Return true if average of light reads is above the threshold
     * For possibly strobing lights, remove below avg values and reevaluate to account
     * for skew when the lights strobe off
     * @return If lights likely to be bright
     */
    fun isLightBright(): Boolean {
        if (lightReads.average() > HIGH_LIGHT_LEVEL) {
            Log.d("LOGPROCESS", "High light.")
            return true
        }
        else if(isLightStrobing()) {
            lightReads.removeIf { it < lightReads.average() }
            if (lightReads.average() > HIGH_LIGHT_LEVEL) {
                Log.d("LOGPROCESS", "High light.")
                return true
            }
        }
        Log.d("LOGPROCESS", "Low light.")
        return false
    }

    fun isLoudSound(): Boolean {
        val mean = soundReadings.average()
        Log.d("LOGPROCESS", "$mean dB.")
        if (mean > DECIBEL_THRESHOLD) {
            Log.d("LOGPROCESS", "Loud sound.")
            return true
        }
        Log.d("LOGPROCESS", "Quiet sound.")
        return false
    }

}