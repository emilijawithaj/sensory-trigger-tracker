package com.example.soverloadtracker.presentation.dataStorage

import java.time.Instant

class LogData (val dateTime: Instant, var avgLux: Float, var wasBright: Boolean, var luxStdev: Float, var lightOther: Boolean,
               var avgDecibels: Float, var wasLoud: Boolean, var noiseOther: Boolean, var smellStrong: Boolean,
               var smellOther: Boolean, var tactileBad: Boolean, var tactilePersonalContact: Boolean, var tactileOther: Boolean,
               var tasteStrong: Boolean, var tasteBad: Boolean, var tasteOther: Boolean, val tags: ArrayList<String>) {

    /**
     * Constructor for LogData that accepts and handles nulls
     */
    constructor(dateTime: Instant, avgLux: Float?, wasBright: Boolean, luxStdev: Float?, lightOther: Boolean,
    avgDecibels: Float?, wasLoud: Boolean, noiseOther: Boolean, smellStrong: Boolean,
    smellOther: Boolean, tactileBad: Boolean, tactilePersonalContact: Boolean, tactileOther: Boolean,
    tasteStrong: Boolean, tasteBad: Boolean, tasteOther: Boolean, tags: ArrayList<String>)
            : this (dateTime, avgLux ?: -1f, wasBright,luxStdev ?: -1f, lightOther,
        avgDecibels ?: -1f, wasLoud, noiseOther, smellStrong, smellOther, tactileBad, tactilePersonalContact, tactileOther, tasteStrong, tasteBad, tasteOther, tags)
}