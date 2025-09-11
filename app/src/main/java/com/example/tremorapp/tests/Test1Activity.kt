package com.example.tremorapp.tests

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tremorapp.databinding.ActivityTest1Binding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Test1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityTest1Binding
    private var testStarted = false
    private var startTime: Long = 0
    private var lastTextChangeTime: Long = 0
    private var lastText: String = ""

    // Modelo para guardar datos
    data class KeyPressData(
        val timestamp: Long,      // Momento del evento (ms desde inicio)
        val action: String,       // "INSERT" o "DELETE"
        val char: Char?,          // Carácter afectado
        val currentText: String   // Texto completo en ese momento
    )

    private val keyEvents = mutableListOf<KeyPressData>()
    private lateinit var testTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTest1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el listener del botón de retroceso
        binding.btnBackMenu.setOnClickListener {
            finish() // Cierra esta activity y vuelve a la anterior
        }
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
                    // Carácter insertado
                    val newChar = newText.last()
                    keyEvents.add(KeyPressData(
                        timestamp = currentTime,
                        action = "INSERT",
                        char = newChar,
                        currentText = newText
                    ))
                    Log.d("KeyPress", "INSERT '$newChar' at $currentTime ms")
                } else if (newText.length < lastText.length) {
                    // Carácter borrado
                    keyEvents.add(KeyPressData(
                        timestamp = currentTime,
                        action = "DELETE",
                        char = null,
                        currentText = newText
                    ))
                    Log.d("KeyPress", "DELETE at $currentTime ms")
                }

                lastTextChangeTime = currentTime
                lastText = newText
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun startCountdown() {
        binding.btnStart.isEnabled = false
        binding.btnStart.visibility = View.GONE
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

        // Temporizador de 15 segundos
        object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = "${millisUntilFinished/1000+1}s"
            }
            override fun onFinish() {
                binding.chronometer.stop()
                endTest() }
        }.start()
    }

    private fun endTest() {
        testStarted = false
        binding.etInput.clearFocus()

        // Ocultar teclado virtual
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etInput.windowToken, 0)

        saveTestData()
        showResults()
    }

    private fun saveTestData() {
        try {
            val totalTime = 15000L
            val totalPresses = keyEvents.count {it.action == "INSERT" }
            val kPresses = keyEvents.count {it.action == "INSERT" && it.char?.lowercaseChar() == 'k' }
            val otherPresses = totalPresses - kPresses
            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val username = sharedPref.getString("username", "unknown") ?: "unknown"
            val accuracy = if (totalPresses > 0) (kPresses.toDouble()/totalPresses * 100) else 0.0
            val speed = if (totalTime > 0 ) totalPresses / (totalTime/1000.0) else 0.0

            // Crear JSON con los datos
            val testData = JSONObject().apply {
                put("test_name", "test1")
                put("username", username)
                put("start_time", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(startTime)))
                put("total_time_ms", totalTime)
                put("total_presses", totalPresses)
                put("total_k_presses", kPresses)
                put("total_other_presses", otherPresses)
                put("accuracy", accuracy)
                put("speed_keystrokes_per_sec", speed)
                put("final_text", binding.etInput.text.toString())

                // Agregar todos los eventos de 'k'
                val eventsArray = JSONArray()
                keyEvents.filter { it.action == "INSERT" }.forEach { event ->
                    JSONObject().apply {
                        put("char", event.char?.toString() ?: "")
                        put("timestamp", event.timestamp)
                        put("is_k", event.char?.lowercaseChar() == 'k')
                    }.let { eventsArray.put(it) }
                }
                put("key_events", eventsArray)
            }

            // Guardar archivo
            val fileName = "test1_${getUsername()}_${System.currentTimeMillis()}.json"
            File(filesDir, fileName).writeText(testData.toString())

            sharedPref.edit().putBoolean("test1_completed", true).apply()
            Log.d("Test1", "Datos guardados correctamente")
        } catch (e: Exception) {
            Log.e("Test1", "Error al guardar datos", e)
        }

        //Marcar test como completado para este usuario
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPref.edit()){
            putBoolean("${getUsername()}_test1_completed", true)
            apply()
        }

        setResult(RESULT_OK)}

    private fun getUsername(): String{
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("username", "")?:""

    }


    private fun showResults() {
        AlertDialog.Builder(this)
            .setTitle("Test completado")
            .setMessage("¿Qué deseas hacer?")
            .setPositiveButton("Guardar datos") { _, _ ->
                saveTestData()
                finish() }
            .setNegativeButton("Repetir test") { _, _ -> resetTest() }
            .setCancelable(false)
            .show()
    }

    private fun resetTest() {
        // Limpiar el campo de texto y habilitarlo
        binding.etInput.text.clear()
        binding.etInput.isEnabled = true
        binding.etInput.visibility = View.GONE
        // Detener y ocultar cronómetro
        binding.chronometer.visibility = View.GONE
        binding.chronometer.stop()
        //Reactivar el botón de inicio y hacerlo visible
        binding.btnStart.isEnabled = true
        binding.btnStart.visibility = View.VISIBLE
        //Restablecer el contador
        binding.tvCountdown.text = ""
        // Reiniciar estado del test
        testStarted = false
        keyEvents.clear()
    }
}