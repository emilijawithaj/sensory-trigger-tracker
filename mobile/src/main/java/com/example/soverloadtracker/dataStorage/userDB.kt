package com.example.soverloadtracker.dataStorage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.Instant

class SqLiteDatabase(context: Context) : SQLiteOpenHelper(
    context,

    DATABASE_NAME,
    null, DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        //build tables
        val CREATE_LOG_TABLE =
            "CREATE TABLE $TABLE_LOG ($COLUMN_DATETIME TEXT PRIMARY KEY, $COLUMN_AVG_LUX REAL, $COLUMN_LUX_STDEV REAL, $COLUMN_LIGHT_OTHER INTEGER, " +
                    " $COLUMN_AVG_DECIBELS REAL, $COLUMN_NOISE_OTHER INTEGER, " +
                    " $COLUMN_SMELL_STRONG INTEGER, $COLUMN_SMELL_OTHER INTEGER, $COLUMN_TACTILE_BAD INTEGER, $COLUMN_TACTILE_PERSONALCONTACT INTEGER, " +
                    " $COLUMN_TACTILE_OTHER INTEGER, $COLUMN_TASTE_STRONG INTEGER, $COLUMN_TASTE_BAD INTEGER, $COLUMN_TASTE_OTHER INTEGER)"
        val CREATE_TAG_TABLE =
            "CREATE TABLE $TABLE_TAGS ($COLUMN_TAG_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TAG TEXT, $COLUMN_TAG_DATETIME TEXT)"

        val CREATE_TRIGGER_TABLE =
            "CREATE TABLE $TABLE_TRIGGERS ($COLUMN_TRIGGER_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TRIGGER TEXT)"

        db.execSQL(CREATE_TRIGGER_TABLE)
        db.execSQL(CREATE_LOG_TABLE)
        db.execSQL(CREATE_TAG_TABLE)
    }

    /**
     * Drops and rebuilds all tables.
     * @param db SQLiteDatabase object
     * @param oldVer previous database version
     * @param newVer current database version
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVer: Int, newVer: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TAGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIGGERS")

        onCreate(db)
    }

    /**
     * Clears all log records from the log database
     */
    fun clearLogs() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_LOG")
        db.execSQL("DELETE FROM $TABLE_TAGS")
    }

    /**
     * Generate and return a list of all log records, including tags
     * @return List of log records in format LogData
     */
    fun listLogRecords(): MutableList<LogData> {
        //set up
        val sql = "SELECT * FROM $TABLE_LOG"
        val db = this.readableDatabase
        val logsList = arrayListOf<LogData>()
        val cursor = db.rawQuery(sql, null)

        //get data from log table
        if (cursor.moveToFirst()) {
            do {
                val dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATETIME))
                val avgLux = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AVG_LUX))
                val luxStdev = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_LUX_STDEV))
                val lightOther =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIGHT_OTHER)) == 1
                val avgDecibels = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AVG_DECIBELS))
                val noiseOther =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOISE_OTHER)) == 1
                val smellStrong =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SMELL_STRONG)) == 1
                val smellOther =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SMELL_OTHER)) == 1
                val tactileBad =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TACTILE_BAD)) == 1
                val tactilePersonalContact =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TACTILE_PERSONALCONTACT)) == 1
                val tactileOther =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TACTILE_OTHER)) == 1
                val tasteStrong =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASTE_STRONG)) == 1
                val tasteBad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASTE_BAD)) == 1
                val tasteOther =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASTE_OTHER)) == 1

                //format data correctly for object and add to list
                val dateTimeO = Instant.parse(dateTime)

                //find tags for this log
                val tagSql = "SELECT * FROM $TABLE_TAGS WHERE $COLUMN_TAG_DATETIME = ?"
                val tagCursor = db.rawQuery(tagSql, arrayOf(dateTime))
                val tags = arrayListOf<String>()
                if (tagCursor.moveToFirst()) {
                    do {
                        val tag = tagCursor.getString(tagCursor.getColumnIndexOrThrow(COLUMN_TAG))
                        tags.add(tag)
                    } while (tagCursor.moveToNext())
                }
                tagCursor.close()

                //generate object and add to list
                logsList.add(
                    LogData(
                        dateTimeO,
                        avgLux,
                        luxStdev,
                        lightOther,
                        avgDecibels,
                        noiseOther,
                        smellStrong,
                        smellOther,
                        tactileBad,
                        tactilePersonalContact,
                        tactileOther,
                        tasteStrong,
                        tasteBad,
                        tasteOther,
                        tags
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return logsList
    }


    /**
     * Create a new log entry from a logData object
     * @param logData LogData object of record to be saved
     */
    fun addLogRecord(logData: LogData) {
        val logValues = ContentValues()
        val tagValues = ContentValues()

        val db = this.writableDatabase
        db.beginTransaction()
        try {

            //handle log values
            logValues.put(COLUMN_DATETIME, logData.dateTime.toString())
            logValues.put(COLUMN_AVG_LUX, logData.avgLux)
            logValues.put(COLUMN_LUX_STDEV, logData.luxStdev)
            logValues.put(COLUMN_AVG_DECIBELS, logData.avgDecibels)
            logValues.put(COLUMN_LIGHT_OTHER, logData.lightOther)
            logValues.put(COLUMN_NOISE_OTHER, logData.noiseOther)
            logValues.put(COLUMN_SMELL_STRONG, logData.smellStrong)
            logValues.put(COLUMN_SMELL_OTHER, logData.smellOther)
            logValues.put(COLUMN_TACTILE_BAD, logData.tactileBad)
            logValues.put(COLUMN_TACTILE_PERSONALCONTACT, logData.tactilePersonalContact)
            logValues.put(COLUMN_TACTILE_OTHER, logData.tactileOther)
            logValues.put(COLUMN_TASTE_STRONG, logData.tasteStrong)
            logValues.put(COLUMN_TASTE_BAD, logData.tasteBad)
            logValues.put(COLUMN_TASTE_OTHER, logData.tasteOther)

            //handle tag values
            for (tag in logData.tags) {
                tagValues.put(COLUMN_TAG, tag)
                tagValues.put(COLUMN_TAG_DATETIME, logData.dateTime.toString())
                db.insert(TABLE_TAGS, null, tagValues)
            }

            //push to db
            db.insertWithOnConflict(TABLE_LOG, null, logValues, SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    /**
     * Updates only the manually logged aspects, maintaining previous sensor reading values.
     * @param dateTime dateTime ID of log to replace, as Instant
     * @param logData LogData object, sensor related and dateTime values of which will be disregarded
     * @throws NoSuchElementException if log not found
     */
    @Throws(NoSuchElementException::class)
    fun updateLogRecordBools(dateTime: Instant, logData: LogData) {
        updateLogRecordBools(dateTime.toString(), logData)
    }

    /**

     * Updates only the manually logged aspects, maintaining previous sensor reading values.
     * @param dateTime dateTime ID of log to replace, as String
     * @param logData LogData object, sensor related and dateTime values of which will be disregarded
     * @throws NoSuchElementException if log not found
     */
    @Throws(NoSuchElementException::class)
    fun updateLogRecordBools(dateTime: String, logData: LogData) {
        val logValues = ContentValues()
        val tagValues = ContentValues()
        val logCurrent = retrieveLog(dateTime)

        val db = this.writableDatabase
        db.beginTransaction()
        try {

            if (logCurrent == null) {
                throw NoSuchElementException("Update log fail; log not found")
            }

            //assemble log values
            logValues.put(COLUMN_DATETIME, dateTime)
            logValues.put(COLUMN_AVG_LUX, logCurrent.avgLux)
            logValues.put(COLUMN_LUX_STDEV, logCurrent.luxStdev)
            logValues.put(COLUMN_AVG_DECIBELS, logCurrent.avgDecibels)

            logValues.put(COLUMN_LIGHT_OTHER, logData.lightOther)
            logValues.put(COLUMN_NOISE_OTHER, logData.noiseOther)
            logValues.put(COLUMN_SMELL_STRONG, logData.smellStrong)
            logValues.put(COLUMN_SMELL_OTHER, logData.smellOther)
            logValues.put(COLUMN_TACTILE_BAD, logData.tactileBad)
            logValues.put(COLUMN_TACTILE_PERSONALCONTACT, logData.tactilePersonalContact)
            logValues.put(COLUMN_TACTILE_OTHER, logData.tactileOther)
            logValues.put(COLUMN_TASTE_STRONG, logData.tasteStrong)
            logValues.put(COLUMN_TASTE_BAD, logData.tasteBad)
            logValues.put(COLUMN_TASTE_OTHER, logData.tasteOther)

            //push
            db.update(TABLE_LOG, logValues, "$COLUMN_DATETIME = ?", arrayOf(dateTime))

            //add and push tag values
            for (tag in logData.tags) {
                if (!logCurrent.tags.contains(tag)) {
                    tagValues.put(COLUMN_TAG, tag)
                    tagValues.put(COLUMN_TAG_DATETIME, dateTime)
                    db.insert(TABLE_TAGS, null, tagValues)
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    /**
     * Updates a record
     * @param dateTime dateTime ID of log to replace, as Instant
     * @param logData LogData object, sensor related and dateTime values of which will be disregarded
     * @throws NoSuchElementException if log not found
     */
    @Throws(NoSuchElementException::class)
    fun updateLogRecord(dateTime: Instant, logData: LogData) {
        val logValues = ContentValues()
        val tagValues = ContentValues()
        val logCurrent = retrieveLog(dateTime)

        val db = this.writableDatabase
        db.beginTransaction()
        try {

            if (logCurrent == null) {
                throw NoSuchElementException("Update log fail; log not found")
            }

            //assemble log values
            logValues.put(COLUMN_DATETIME, dateTime.toString())
            logValues.put(COLUMN_AVG_LUX, logData.avgLux)
            logValues.put(COLUMN_LUX_STDEV, logData.luxStdev)
            logValues.put(COLUMN_AVG_DECIBELS, logData.avgDecibels)

            logValues.put(COLUMN_LIGHT_OTHER, logData.lightOther)
            logValues.put(COLUMN_NOISE_OTHER, logData.noiseOther)
            logValues.put(COLUMN_SMELL_STRONG, logData.smellStrong)
            logValues.put(COLUMN_SMELL_OTHER, logData.smellOther)
            logValues.put(COLUMN_TACTILE_BAD, logData.tactileBad)
            logValues.put(COLUMN_TACTILE_PERSONALCONTACT, logData.tactilePersonalContact)
            logValues.put(COLUMN_TACTILE_OTHER, logData.tactileOther)
            logValues.put(COLUMN_TASTE_STRONG, logData.tasteStrong)
            logValues.put(COLUMN_TASTE_BAD, logData.tasteBad)
            logValues.put(COLUMN_TASTE_OTHER, logData.tasteOther)

            //push
            db.update(TABLE_LOG, logValues, "$COLUMN_DATETIME = ?", arrayOf(dateTime.toString()))

            //clear existing tags
            db.delete(
                TABLE_TAGS,
                "$COLUMN_TAG_DATETIME = ?",
                arrayOf(logData.dateTime.toString())
            )

            //add and push tag values
            for (tag in logData.tags) {
                tagValues.put(COLUMN_TAG, tag)
                tagValues.put(COLUMN_TAG_DATETIME, dateTime.toString())
                db.insert(TABLE_TAGS, null, tagValues)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }


    /**
     * Attempts to retrieve a specific log
     * @param dateTime dateTime ID of the log, as a String
     */
    fun retrieveLog(dateTime: String): LogData? {
        var logData: LogData? = null
        val query = "SELECT * FROM $TABLE_LOG WHERE $COLUMN_DATETIME = ?"
        val db = this.readableDatabase

        //get values
        val cursor = db.rawQuery(query, arrayOf(dateTime))
        if (cursor.moveToFirst()) {
            val avgLux = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AVG_LUX))
            val luxStdev = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_LUX_STDEV))
            val lightOther = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIGHT_OTHER)) == 1
            val avgDecibels = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AVG_DECIBELS))
            val noiseOther = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOISE_OTHER)) == 1
            val smellStrong = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SMELL_STRONG)) == 1
            val smellOther = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SMELL_OTHER)) == 1
            val tactileBad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TACTILE_BAD)) == 1
            val tactilePersonalContact =
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TACTILE_PERSONALCONTACT)) == 1
            val tactileOther =
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TACTILE_OTHER)) == 1
            val tasteStrong = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASTE_STRONG)) == 1
            val tasteBad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASTE_BAD)) == 1
            val tasteOther = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASTE_OTHER)) == 1

            val tagSql = "SELECT * FROM $TABLE_TAGS WHERE $COLUMN_TAG_DATETIME = ?"
            val tagCursor = db.rawQuery(tagSql, arrayOf(dateTime))
            val tags = arrayListOf<String>()
            if (tagCursor.moveToFirst()) {
                do {
                    val tag = tagCursor.getString(tagCursor.getColumnIndexOrThrow(COLUMN_TAG))
                    tags.add(tag)
                } while (tagCursor.moveToNext())
            }
            tagCursor.close()

            //make object
            logData = LogData(
                Instant.parse(dateTime),
                avgLux,
                luxStdev,
                lightOther,
                avgDecibels,
                noiseOther,
                smellStrong,
                smellOther,
                tactileBad,
                tactilePersonalContact,
                tactileOther,
                tasteStrong,
                tasteBad,
                tasteOther,
                tags
            )
        }
        cursor.close()
        db.close()
        return logData
    }


    /**
     * Attempts to retrieve a specific log
     * @param dateTime dateTime ID of the log, as an Instant
     */
    fun retrieveLog(dateTime: Instant): LogData? {
        return retrieveLog(dateTime.toString())
    }


    /**
     * Retrieves all tag records independent of logs
     * @return List of tags
     */
    fun listTagRecords(): MutableList<Tag> {
        val tags = arrayListOf<Tag>()
        val query = "SELECT * FROM $TABLE_TAGS"
        val db = this.readableDatabase

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val tag = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG))
                val dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG_DATETIME))
                tags.add(Tag(tag, Instant.parse(dateTime)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tags
    }


    /**
     * Adds a trigger to the known triggers table
     * @param trigger Trigger to be added as a String
     */
    fun addTrigger(trigger: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TRIGGER, trigger)
        db.insert(TABLE_TRIGGERS, null, values)
        db.close()
    }

    /**
     * Deletes a trigger from the known triggers table
     * @param trigger Trigger to be deleted as a String
     */
    fun deleteTrigger(trigger: String) {
        val db = this.writableDatabase
        db.delete(TABLE_TRIGGERS, "$COLUMN_TRIGGER = ?", arrayOf(trigger))
        db.close()
    }

    /**
     * Deletes a tag from the tags table
     * @param tag Tag to be deleted
     */
    fun deleteTag(tag: Tag) {
        val db = this.writableDatabase
        db.delete(
            TABLE_TAGS,
            "$COLUMN_TAG = ? AND $COLUMN_TAG_DATETIME = ?",
            arrayOf(tag.name, tag.dateTime.toString())
        )
        db.close()
    }

    /**
     * Deletes a log record from the table
     * @param dateTime dateTime ID of the log, as a String
     */
    fun deleteLog(dateTime: String) {
        val db = this.writableDatabase
        db.delete(TABLE_LOG, "$COLUMN_DATETIME = ?", arrayOf(dateTime))
        db.close()
    }

    /**
     * Deletes a log record from the table
     * @param log LogData object to be removed
     */
    fun deleteLog(log: LogData) {
        deleteLog(log.dateTime.toString())
    }

    /**
     * Gets all tags for a specific dateTime (i.e. log)
     * @param dateTime dateTime ID of the log, as an Instant
     * @return List of tags as Strings
     */
    fun getTags(dateTime: Instant): List<String> {
        val tags = arrayListOf<String>()
        val query = "SELECT * FROM $TABLE_TAGS WHERE $COLUMN_TAG_DATETIME = ?"
        val db = this.readableDatabase

        val cursor = db.rawQuery(query, arrayOf(dateTime.toString()))
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG))
                tags.add(name)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tags
    }

    /**
     * Retrieves all known triggers
     * @return List of triggers
     */
    fun getTriggers(): List<String> {
        val sql = "SELECT * FROM $TABLE_TRIGGERS"
        val triggers = mutableListOf<String>()
        val db = this.readableDatabase

        val cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                val trigger = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIGGER))
                triggers.add(trigger)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return triggers
    }


    //COMPANION OBJ
    companion object {
        private var instance: SqLiteDatabase? = null

        // This is the only way other classes should get the database
        @Synchronized
        fun getInstance(context: Context): SqLiteDatabase {
            if (instance == null) {
                // Use applicationContext to prevent memory leaks
                instance = SqLiteDatabase(context.applicationContext)
            }
            return instance!!
        }

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "soverloadtracker.db"
        private const val TABLE_LOG = "logs"
        private const val TABLE_TAGS = "tags"
        private const val COLUMN_DATETIME = "datetime"
        private const val COLUMN_AVG_LUX = "avg_lux"
        private const val COLUMN_LUX_STDEV = "light_stdev"
        private const val COLUMN_LIGHT_OTHER = "light_other"
        private const val COLUMN_AVG_DECIBELS = "avg_decibels"
        private const val COLUMN_NOISE_OTHER = "noise_other"
        private const val COLUMN_SMELL_STRONG = "smell_strong"
        private const val COLUMN_SMELL_OTHER = "smell_other"
        private const val COLUMN_TACTILE_BAD = "tactile_bad"
        private const val COLUMN_TACTILE_PERSONALCONTACT = "tactile_pcontact"
        private const val COLUMN_TACTILE_OTHER = "tactile_other"
        private const val COLUMN_TASTE_STRONG = "taste_strong"
        private const val COLUMN_TASTE_BAD = "taste_bad"
        private const val COLUMN_TASTE_OTHER = "taste_other"

        private const val COLUMN_TAG_ID = "tag_id"
        private const val COLUMN_TAG = "tag"
        private const val COLUMN_TAG_DATETIME = "tag_datetime"

        private const val TABLE_TRIGGERS = "triggers"
        private const val COLUMN_TRIGGER_ID = "trigger_id"
        private const val COLUMN_TRIGGER = "trigger_name"

    }
}