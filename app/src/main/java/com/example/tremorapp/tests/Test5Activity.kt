package com.example.tremorapp.tests

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tremorapp.databinding.ActivityTest5Binding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.tremorapp.R

class Test5Activity : AppCompatActivity() {

    private lateinit var binding: ActivityTest5Binding
    private var testStarted = false
    private var startTime: Long = 0
    private var lastTextChangeTime: Long = 0
    private var lastText = ""
    private var soundTimer: CountDownTimer? = null
    private var testTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var expectedPressTimes = mutableListOf<Long>()
    private var actualPressTimes = mutableListOf<Long>()

    private val INITIAL_DELAY_MS = 500L
    private val TOTAL_DURATION_MS = 15000L
    private val BEEP_INTERVAL_MS = 1000L


    // Modelo para guardar datos
    data class KeyPressData(
        val timestamp: Long,      // Momento del evento (ms desde inicio)
        val action: String,       // "INSERT" o "DELETE"
        val char: Char?,          // Carácter afectado
        val currentText: String,   // Texto completo en ese momento
        val correct: Boolean,      // Si el carácter es correcto
        val deviation: Long,      // Diferencia con el sonido esperado (ms)
        val expectedTime: Long    // Momento teórico en que debió pulsarse
    )

    private val keyEvents = mutableListOf<KeyPressData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTest5Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTest()
        setupMediaPlayer()
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound).apply {
            setVolume(0.5f, 0.5f)
        }
    }

    override fun onBackPressed() {
        if (testStarted) {
            Toast.makeText(this, "Por favor complete el test primero", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupTest() {
        binding.btnStart.setOnClickListener {
            startCountdown()
        }

        // Configurar TextWatcher
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                lastText = s?.toString() ?: ""
            }


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!testStarted) return

                val currentTime = System.currentTimeMillis() - startTime
                val newText = s?.toString() ?: ""

                // Detener si fue inserción o borrado
                if (newText.length > lastText.length) {
                    // Carácter insertado
                    val newChar = newText.last()
                    val isExpectedTime = isCloseToExpectedTime(currentTime)

                    keyEvents.add(
                        KeyPressData(
                            timestamp = currentTime,
                            action = "INSERT",
                            char = newChar,
                            currentText = newText,
                            correct = newChar.lowercaseChar() == 'k' && isExpectedTime,
                            deviation = calculateDeviation(currentTime),
                            expectedTime = getClosestExpectedTime(currentTime)
                        )
                    )

                    if (newChar.lowercaseChar() == 'k') {
                        actualPressTimes.add(currentTime)
                    }

                    Log.d(
                        "KeyPress",
                        "INSERT '$newChar' at $currentTime ms, correct: ${newChar.lowercaseChar() == 'k' && isExpectedTime}"
                    )
                } else if (newText.length < lastText.length) {
                    // Carácter borrado
                    keyEvents.add(
                        KeyPressData(
                            timestamp = currentTime,
                            action = "DELETE",
                            char = null,
                            currentText = newText,
                            correct = false,
                            deviation = 0,
                            expectedTime = 0
                        )
                    )
                    Log.d("KeyPress", "DELETE at $currentTime ms")
                }

                lastTextChangeTime = currentTime
                lastText = newText
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isCloseToExpectedTime(currentTime: Long): Boolean {
        // Consideramos correcto si está dentro de ±300ms del tiempo esperado
        return expectedPressTimes.any {expectedTime ->
            val adjustedExpected = expectedTime + INITIAL_DELAY_MS
            Math.abs(adjustedExpected - currentTime) <= 300
        }
    }

    private fun calculateDeviation(currentTime: Long): Long {
        val closestExpected = expectedPressTimes.minByOrNull { Math.abs(it - currentTime) }
        return closestExpected?.let { Math.abs(it - currentTime) } ?: Long.MAX_VALUE
    }

    private fun getClosestExpectedTime(currentTime: Long): Long {
        return expectedPressTimes.minByOrNull { Math.abs(it - currentTime) } ?: 0
    }

    private fun startCountdown() {
        binding.btnStart.isEnabled = false
        binding.tvCountdown.visibility = View.VISIBLE

        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = "${(millisUntilFinished / 1000) + 1}"
            }

            override fun onFinish() {
                startTest()
            }
        }.start()
    }

    @SuppressLint("ServiceCast")
    private fun startTest() {
        testStarted = true
        keyEvents.clear()
        expectedPressTimes.clear()
        actualPressTimes.clear()
        startTime = System.currentTimeMillis()


        // Configurar crónometro para 15 segundos
        binding.chronometer.visibility = View.VISIBLE
        binding.chronometer.base = SystemClock.elapsedRealtime()
        binding.chronometer.setOnChronometerTickListener { chronometer ->
            val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
            val minutes = (elapsedMillis / 60000).toInt()
            val seconds = (elapsedMillis % 60000 / 1000).toInt()
            chronometer.text = String.format("Tiempo: %02d:%02d", minutes, seconds)
        }
        binding.chronometer.start()

        binding.tvCountdown.visibility = View.GONE
        binding.etInput.visibility = View.VISIBLE

        // Mostrar teclado virtual
        binding.etInput.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etInput, InputMethodManager.SHOW_IMPLICIT)

        // Temporizador de sonidos cada segundo
        soundTimer =
            object : CountDownTimer(TOTAL_DURATION_MS + INITIAL_DELAY_MS, BEEP_INTERVAL_MS) {
                override fun onTick(millisUntilFinished: Long) {
                    val elapsedTime = TOTAL_DURATION_MS + INITIAL_DELAY_MS - millisUntilFinished

                    // Solo activar despues del retardo inicial
                    if (elapsedTime >= INITIAL_DELAY_MS) {
                        val testTime = elapsedTime - INITIAL_DELAY_MS
                        expectedPressTimes.add(testTime)
                        playSound()
                    }
                    binding.tvCountdown.text = "${millisUntilFinished / 1000 + 1}s"
                }

                override fun onFinish() {
                    playSound() //Último sonido al final
                    expectedPressTimes.add(TOTAL_DURATION_MS)
                }
            }.start()

        // Temporizador de 15 segundos para el test completo
        testTimer = object : CountDownTimer(TOTAL_DURATION_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                binding.chronometer.stop()
                Handler(Looper.getMainLooper()).postDelayed({
                    endTest()
                }, 200)
            }
        }.start()
    }

    private fun playSound() {
        try {
            mediaPlayer?.seekTo(0) //Rebobinar al inicio si ya está reproduciendo
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("Test5", "Error al reproducir sonido", e)
        }
    }


    private fun endTest() {
        testStarted = false
        binding.etInput.clearFocus()
        soundTimer?.cancel()
        testTimer?.cancel()
        binding.etInput.clearFocus()

        // Ocultar teclado virtual
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etInput.windowToken, 0)

        saveTestData()
        showResults()
    }

    private fun saveTestData() {
        try {
            val totalTime = TOTAL_DURATION_MS
            val totalPresses = keyEvents.count { it.action == "INSERT" }
            val kPresses =
                keyEvents.count { it.action == "INSERT" && it.char?.lowercaseChar() == 'k' }
            val correctPresses = keyEvents.count { event ->
                event.action == "INSERT" &&
                        event.char?.lowercaseChar() == 'k' &&
                        isCloseToExpectedTime(event.timestamp)
            }
            val otherPresses = totalPresses - kPresses

            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val username = sharedPref.getString("username", "unknown") ?: "unknown"

            val accuracy = if (expectedPressTimes.isNotEmpty()) {
                (correctPresses.toDouble() / expectedPressTimes.size) * 100
            } else 0.0
            val rhythmDeviation = if (actualPressTimes.isNotEmpty() && actualPressTimes.size >= 2) {
                calculateRhythmDeviation()
            } else 0.0
            val speed = if (totalTime > 0) totalPresses / (totalTime / 1000.0) else 0.0

            // Crear JSON con los datos
            val testData = JSONObject().apply {
                put("test_name", "test5")
                put("username", username)
                put(
                    "start_time",
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date(startTime))
                )
                put("total_time_ms", totalTime)
                put("total_presses", totalPresses)
                put("total_k_presses", kPresses)
                put("correct_rhythm_presses", correctPresses)
                put("total_other_presses", otherPresses)
                put("accuracy", accuracy)
                put("average_deviation_ms", rhythmDeviation)
                put("expected_presses_count", expectedPressTimes.size)
                put("speed_keystrokes_per_sec", speed)
                put("final_text", binding.etInput.text.toString())

                // Agregar todos los eventos de teclado con información de ritmo
                val eventsArray = JSONArray()
                keyEvents.forEach { event ->
                    JSONObject().apply {
                        put("char", event.char?.toString() ?: "")
                        put("timestamp", event.timestamp)
                        put("action", event.action)
                        put("is_k", event.char?.lowercaseChar() == 'k')
                        put("correct", event.correct)
                        put("deviation_ms", event.deviation)
                        put("expected_time", event.expectedTime)
                    }.let { eventsArray.put(it) }
                }
                put("key_events", eventsArray)

                // Agregar tiempos esperados
                val expectedArray = JSONArray()
                expectedPressTimes.forEach {
                    expectedArray.put(it)
                    put("expected_times", expectedArray)
                }
            }

            // Guardar archivo
            val fileName = "test5_${getUsername()}_${System.currentTimeMillis()}.json"
            File(filesDir, fileName).writeText(testData.toString())

            sharedPref.edit().putBoolean("test5_completed", true).apply()
            Log.d("Test5", "Datos guardados correctamente")
        } catch (e: Exception) {
            Log.e("Test5", "Error al guardar datos", e)
        }

        //Marcar test como completado para este usuario
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("${getUsername()}_test5_completed", true)
            apply()
        }

        setResult(RESULT_OK)
    }

    private fun calculateRhythmDeviation(): Double {
        if (actualPressTimes.size < 2) return 0.0

        // Calcular la desviación estandar de los intervalos entre pulsaciones
        val intervals = mutableListOf<Long>()
        for (i in 1 until actualPressTimes.size) {
            intervals.add(actualPressTimes[i] - actualPressTimes[i - 1])
        }
        val mean = intervals.average()
        val variance = intervals.map { (it - mean) * (it - mean) }.average()
        return Math.sqrt(variance)
    }

    private fun getUsername(): String {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("username", "") ?: ""

    }

    private fun showResults() {
        AlertDialog.Builder(this)
            .setTitle("Test completado")
            .setMessage("¿Qué deseas hacer?")
            .setPositiveButton("Guardar datos") { _, _ ->
                saveTestData()
                finish()
            }
            .setNegativeButton("Repetir test") { _, _ -> resetTest() }
            .setCancelable(false)
            .show()
    }

    private fun resetTest() {
        // Limpiar el campo de texto y habilitarlo
        binding.etInput.text.clear()
        binding.etInput.isEnabled = true
        // Restablecer la visibilidad y el estado de los elementos
        binding.etInput.visibility = View.GONE
        // Detener y ocultar cronómetro
        binding.chronometer.visibility = View.GONE
        binding.chronometer.stop()
        //Reactivar el botón de inicio
        binding.btnStart.isEnabled = true
        //Restablecer el contador
        binding.tvCountdown.text = ""
        // Reiniciar estado del test
        testStarted = false
        keyEvents.clear()
        expectedPressTimes.clear()
        actualPressTimes.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        soundTimer?.cancel()
        testTimer?.cancel()
    }
}
