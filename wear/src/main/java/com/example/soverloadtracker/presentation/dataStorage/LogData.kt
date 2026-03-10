package com.example.soverloadtracker.presentation

import java.time.Instant

class LogData (val dateTime: Instant, var avgLux: Float, var luxStdev: Float, var lightOther: Boolean,
               var avgDecibels: Float, var noiseOther: Boolean, var smellStrong: Boolean,
               var smellOther: Boolean, var tactileBad: Boolean, var tactilePersonalContact: Boolean, var tactileOther: Boolean,
               var tasteStrong: Boolean, var tasteBad: Boolean, var tasteOther: Boolean, val tags: ArrayList<String>) {

    /**
     * Constructor for LogData that accepts and handles nulls
     */
    constructor(dateTime: Instant, avgLux: Float?, luxStdev: Float?, lightOther: Boolean,
    avgDecibels: Float?, noiseOther: Boolean, smellStrong: Boolean,
    smellOther: Boolean, tactileBad: Boolean, tactilePersonalContact: Boolean, tactileOther: Boolean,
    tasteStrong: Boolean, tasteBad: Boolean, tasteOther: Boolean, tags: ArrayList<String>)
            : this (dateTime, avgLux ?: -1f, luxStdev ?: -1f, lightOther,
        avgDecibels ?: -1f, noiseOther, smellStrong, smellOther, tactileBad, tactilePersonalContact, tactileOther, tasteStrong, tasteBad, tasteOther, tags)
}