package com.example.tremorapp.tests

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.example.tremorapp.databinding.ActivityTest2Binding
import android.os.SystemClock
import android.text.Editable
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import org.json.JSONArray
import java.io.File
import android.util.Log
import android.view.View
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager

class Test2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityTest2Binding
    private var testStarted = false
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var lastText: String = ""
    private val targetText = "Lupra zenok tir"

    // Modelo para guardar datos
    data class KeyPressData(
        val timestamp: Long,      // Momento del evento (ms desde inicio)
        val action: String,       // "INSERT" o "DELETE"
        val char: Char?,          // Carácter afectado
        val currentText: String,   // Texto completo en ese momento
        val correct: Boolean      // Si el carácter es correcto
    )

    private val keyEvents = mutableListOf<KeyPressData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTest2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTest()
    }

    override fun onBackPressed() {
        if (testStarted) {
            Toast.makeText(this, "Por favor complete el test primero", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupTest() {
        // Mostrar la frase a copiar
        binding.tvSentence.text = targetText

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

                // Determinar si fue inserción o borrado
                if (newText.length > lastText.length) {
                    // Carácter insertado - verificar si es correcto
                    val newChar = newText.last()
                    val expectedChar = targetText.getOrNull(newText.length - 1)
                    val isCorrect = newChar == expectedChar

                    keyEvents.add(
                        KeyPressData(
                            timestamp = currentTime,
                            action = "INSERT",
                            char = newChar,
                            currentText = newText,
                            correct = isCorrect
                        )
                    )
                    Log.d("KeyPress", "INSERT '$newChar' at $currentTime ms")
                } else if (newText.length < lastText.length) {
                    // Carácter borrado
                    keyEvents.add(
                        KeyPressData(
                            timestamp = currentTime,
                            action = "DELETE",
                            char = null,
                            currentText = newText,
                            correct = false
                        )
                    )
                    Log.d("KeyPress", "DELETE at $currentTime ms")
                }
                lastText = newText

                // Verificar si completó la frase
                if (newText.equals(targetText, ignoreCase = true)){
                    endTest()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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
        startTime = System.currentTimeMillis()

        // Configurar crónometro
        binding.chronometer.visibility = View.VISIBLE
        binding.chronometer.base = SystemClock.elapsedRealtime()
        binding.chronometer.setOnChronometerTickListener { chronometer ->
            val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
            chronometer.text = "Tiempo:${elapsedMillis / 1000}s"
        }
        binding.chronometer.start()

        binding.tvCountdown.visibility = View.GONE
        binding.etInput.visibility = View.VISIBLE

        // Mostrar teclado virtual
        binding.etInput.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun endTest() {
        testStarted = false
        endTime = System.currentTimeMillis()
        binding.chronometer.stop()
        binding.etInput.isEnabled = false

        // Ocultar teclado virtual
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etInput.windowToken, 0)

        saveTestData()
        showResults()
    }

    private fun saveTestData() {
        try {
            val totalTime = endTime - startTime
            val totalPresses = keyEvents.count { it.action == "INSERT" }
            val correctPresses = keyEvents.count {it.action == "INSERT" && it.correct}
            val incorrectPresses = totalPresses - correctPresses
            val accuracy = if (totalPresses > 0) (correctPresses.toDouble()/totalPresses * 100) else 0.0
            val speed = if (totalTime > 0 ) totalPresses / (totalTime/1000.0) else 0.0

            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val username = sharedPref.getString("username", "unknown") ?: "unknown"

            // Crear JSON con los datos
            val testData = JSONObject().apply {
                put("test_name", "test2")
                put("username", username)
                put(
                    "start_time",
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date(startTime)
                    )
                )
                put("total_time_ms", totalTime)
                put("target_text", targetText)
                put("final_text", binding.etInput.text.toString())
                put("total_presses", totalPresses)
                put("correct_presses", correctPresses)
                put("incorrect_presses", incorrectPresses)
                put("accuracy", accuracy)
                put("speed_keystrokes_per_sec", speed)

                // Agregar todos los eventos
                val eventsArray = JSONArray()
                keyEvents.forEach { event ->
                    JSONObject().apply {
                        put("char", event.char?.toString() ?: "")
                        put("timestamp", event.timestamp)
                        put("action", event.action)
                        put("current_text", event.currentText)
                        put("correct", event.correct)
                    }.let { eventsArray.put(it) }
                }
                put("key_events", eventsArray)
            }

            // Guardar archivo
            val fileName = "test2_${getUsername()}_${System.currentTimeMillis()}.json"
            File(filesDir, fileName).writeText(testData.toString())

            sharedPref.edit().putBoolean("test2_completed", true).apply()
            Log.d("Test2", "Datos guardados correctamente")
        } catch (e: Exception) {
            Log.e("Test2", "Error al guardar datos", e)
        }

        //Marcar test como completado para este usuario
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("${getUsername()}_test2_completed", true)
            apply()
        }

        setResult(RESULT_OK)
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
    }
}