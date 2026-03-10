package com.example.soverloadtracker.presentation

import android.util.Log
import kotlin.math.sqrt

class SensorDataComputer {
    var bpms : ArrayList<Float> = ArrayList()
    var lightReads: ArrayList<Float> = ArrayList()
    var lightAccuracyLow = false
    var soundReadings : ArrayList<Double> = ArrayList()
    var lightStdev: Float? = null

    //toggles
    val highLightLevel = 150f
    val lightSmpleSizeMin = 4
    val strobingSTDevThresh = 40
    val dbThreshold = 80

    /**
     * Guess whether the user in a strobing light environment based on the
     * standard deviation of the light readings data set.
     * Defaults to false for small sample sizes due to error margin
     * @return If lights likely to be strobing
     */
    fun isLightStrobing(): Boolean {
        //assume false for small sample sizes
        if (lightReads.size < lightSmpleSizeMin) {
            Log.d("LOGPROCESS", "Strobing too low sample size.")
            return false
        }
        else if (lightAccuracyLow) {
            Log.d("LOGPROCESS", "Strobing too low light accuracy.")
            return false
        }

        val mean = lightReads.average()
        val sumOfSquaredDifferences = lightReads.sumOf { (it.toDouble() - mean) * (it - mean) }
        val variance = sumOfSquaredDifferences / (lightReads.size - 1)
        val stdev = sqrt(variance)
        lightStdev = stdev.toFloat()

        if (stdev >= strobingSTDevThresh) {
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
        if (lightReads.average() > highLightLevel) {
            Log.d("LOGPROCESS", "High light.")
            return true
        }
        else if(isLightStrobing()) {
            lightReads.removeIf { it < lightReads.average() }
            if (lightReads.average() > highLightLevel) {
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
        if (mean > dbThreshold) {
            Log.d("LOGPROCESS", "Loud sound.")
            return true
        }
        Log.d("LOGPROCESS", "Quiet sound.")
        return false
    }

}