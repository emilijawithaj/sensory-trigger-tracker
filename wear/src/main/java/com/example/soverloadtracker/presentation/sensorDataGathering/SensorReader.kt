package com.example.soverloadtracker.presentation.sensorDataGathering

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.util.Log
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.endExercise
import androidx.health.services.client.startExercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import kotlin.math.log10

class SensorReader(private val context: Context,
                   private val coroutineScope: CoroutineScope) {
    val dataProcessor = SensorDataComputer()
    private val delayTime = 3000L
    // HR reading
    private val healthServicesClient by lazy { HealthServices.getClient(context) }
    private val exerciseClient by lazy { healthServicesClient.exerciseClient }
    var bpms: ArrayList<Float> = ArrayList(); private set
    // light reading
    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val lightSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }
    var lightReads: ArrayList<Float> = ArrayList(); private set
    //sound
    private var recorder: MediaRecorder? = null
    private var measurementJob: Job? = null
    val soundReadings: ArrayList<Double> = ArrayList()


    /**
     * Callback for HR streaming. Is set up as a generic workout for data access purposes
     */
    private val hrUpdateCallback = object : ExerciseUpdateCallback {
        override fun onAvailabilityChanged(
            dataType: DataType<*, *>,
            availability: Availability
        ) {}

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val state = update.exerciseStateInfo.state
            if (state.isEnded) {
                //Measurement ended
                Log.d("SOBOOT", "Exercise has ended.")
                return
            }

            // Get latest heart rate data point
            val latestMetrics = update.latestMetrics
            val heartRateDataPoints = latestMetrics.getData(DataType.HEART_RATE_BPM)

            if (heartRateDataPoints.isNotEmpty()) {
                val bpm = heartRateDataPoints.last().value
                Log.d("OUTPUTPRESENT", "Current BPM: $bpm")
                bpms.add(bpm.toFloat()) // Add BPM to list
            }
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {}
        override fun onRegistered() {}
        override fun onRegistrationFailed(throwable: Throwable) {}
    }

    /**
     * start BPM measuring stream
     */
    fun startHeartRateStreaming() {
        coroutineScope.launch {
            //set up callback
            exerciseClient.setUpdateCallback(hrUpdateCallback)
            bpms.clear()

            val dataTypes = setOf(DataType.HEART_RATE_BPM)
            //config generic workout
            val config = ExerciseConfig.builder(ExerciseType.WORKOUT)
                .setDataTypes(dataTypes)
                .build()

            try {
                //start the exercise. This will trigger the onExerciseUpdateReceived callback.
                exerciseClient.startExercise(config)
                Log.d("LOGREAD", "Exercise started successfully.")
                delay(delayTime) // Wait
                Log.d("OUTPUTPRESENT", "5 second timer finished. Stopping heart rate streaming.")
                stopHeartRateStreaming()
            } catch (e: Exception) {
                Log.e("LOGREAD", "Failed to start exercise", e)
            }
        }
    }

    /**
     * kill BPM measuring stream
     */
    fun stopHeartRateStreaming() {
        coroutineScope.launch {
            try {
                exerciseClient.endExercise()
                dataProcessor.bpms = bpms
                Log.d("LOGREAD", "Exercise ended successfully.")
            } catch (e: Exception) {
                Log.e("LOGREAD", "Failed to end exercise", e)
            }
        }
    }


    /**
     * Light sensor listener - receive and  handle sensor data
     */
    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val luxValue = event.values[0]
                lightReads.add(luxValue)
                Log.d("OUTPUTPRESENT", "Light sensor reading: $luxValue lux")

            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            if (accuracy <3) {
                dataProcessor.lightAccuracyLow = true
            }
        }
    }

    /**
     * Get a single light sensor reading.
     */
    fun takeLightReading() {
        if (lightSensor == null) {
            Log.e("LOGREAD", "Device has no light sensor!")
            return
        }

        coroutineScope.launch {
            Log.d("LOGREAD", "Registering listener for a single light reading.")
            lightReads.clear()

            // Register the listener
            sensorManager.registerListener(
                lightSensorListener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )

            //wait and unregister
            delay(delayTime)
            Log.d("LOGREAD", "5 second timer finished. Unregistering listener.")
            sensorManager.unregisterListener(lightSensorListener)
            dataProcessor.lightReads = lightReads
            //TODO DEBUG
            /*
            dataProcessor.isLightStrobing()
            dataProcessor.isLightBright()
             */
        }
    }

    /**
     * Setup and start audio recorder.
     */
    private fun startRecorder() {
        // Use cache to temporarily hold audio data while processing
        val outputFile = File(context.cacheDir, "mic_temp.3gp")

        // Create a new MediaRecorder instance
        recorder = MediaRecorder(context)
        try {
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
                Log.d("SoundRecorder", "MediaRecorder started successfully.")
            }
        } catch (e: IOException) {
            Log.e("SoundRecorder", "MediaRecorder prepare() failed", e)
            recorder = null // Ensure recorder is null if start fails
        }
    }

    /**
     * Record and collect amplitude readings in decibels.
     */
    fun takeSoundReading() {
        //ensure safe to proceed and set up
        // If a measurement is already running, cancel it before starting a new one.
        if (measurementJob?.isActive == true) {
            measurementJob?.cancel()
        }

        measurementJob = coroutineScope.launch {
            startRecorder()
            // If the recorder failed to start, don't
    if (recorder == null) {
        Log.e("SoundRecorder", "Cannot take reading, recorder is not initialized.")
        //TODO THROW HERE
        return@launch
    }

    Log.d("SoundRecorder", "Starting 5-second sound measurement.")
    soundReadings.clear()

    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < delayTime) {
        // Get amplitude and compute decibels
        val amplitude = recorder?.maxAmplitude ?: 0
        val db = 20 * log10(amplitude.toDouble())

        if (db.isFinite()) { // Avoid adding -Infinity if amplitude is 0
            soundReadings.add(db)
        }

        Log.v("OUTPUTPRESENT", "Current vol in Decibels: $db")
        delay(250) // Read the amplitude 4 times per second
    }

    stopRecorder()
    Log.d("SoundRecorder", "Finished 5-second sound measurement. Readings: ${soundReadings.size}")
}
}

    /**
     * Stops the audio recorder and remove temp audio file.
     */
    private fun stopRecorder() {
        dataProcessor.soundReadings = soundReadings
        recorder?.apply {
            try {
                stop()
                release()
                Log.d("SoundRecorder", "MediaRecorder stopped and released.")
            } catch (e: Exception) {
                Log.w("SoundRecorder", "Failed to stop MediaRecorder cleanly", e)
            }
        }
        recorder = null
        //remove temp file
        File(context.cacheDir, "mic_temp.3gp").delete()
    }
}