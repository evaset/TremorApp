package com.example.tremorapp.tests

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


import com.example.tremorapp.databinding.ActivityTest7Binding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Test7Activity : AppCompatActivity() {
    private lateinit var binding: ActivityTest7Binding
    private var testStarted = false
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var lastText: String = ""
    private var currentBlockIndex = 0
    private var testTimer: CountDownTimer? = null

    // Bloques de texto para el test
    private val textBlocks = listOf(
        "El sol brilla sobre el",
        "lago. Los niños juegan",
        "en la orilla. Un perro",
        "ladra desde la barca.",
        "Todos ríen bajo el cielo."
    )

    // Modelo para guardar datos
    data class KeyPressData(
        val timestamp: Long,      // Momento del evento (ms desde inicio)
        val action: String,       // "INSERT" o "DELETE"
        val char: Char?,          // Carácter afectado
        val currentText: String,  // Texto completo en ese momento
        val correct: Boolean,     // Si el carácter es correcto
        val blockIndex: Int       // Índice del bloque actual
    )

    private val keyEvents = mutableListOf<KeyPressData>()
    private val allUserTexts = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTest7Binding.inflate(layoutInflater)
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
        // Mostrar el primer bloque de texto
        showCurrentBlock()

        binding.btnStart.setOnClickListener {
            startCountdown()
        }

        binding.btnNextBlock.setOnClickListener {
            nextBlock()
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
                val currentBlock = textBlocks[currentBlockIndex]

                // Determinar si fue inserción o borrado
                if (newText.length > lastText.length) {
                    // Carácter insertado - verificar si es correcto
                    val newChar = newText.last()
                    val expectedChar = currentBlock.getOrNull(newText.length - 1)
                    val isCorrect = newChar == expectedChar

                    keyEvents.add(
                        KeyPressData(
                            timestamp = currentTime,
                            action = "INSERT",
                            char = newChar,
                            currentText = newText,
                            correct = isCorrect,
                            blockIndex = currentBlockIndex
                        )
                    )
                    Log.d("KeyPress", "INSERT '$newChar' at $currentTime ms (Block $currentBlockIndex)")
                } else if (newText.length < lastText.length) {
                    // Carácter borrado
                    keyEvents.add(
                        KeyPressData(
                            timestamp = currentTime,
                            action = "DELETE",
                            char = null,
                            currentText = newText,
                            correct = false,
                            blockIndex = currentBlockIndex
                        )
                    )
                    Log.d("KeyPress", "DELETE at $currentTime ms (Block $currentBlockIndex)")
                }
                lastText = newText
                // Verificar si completó el bloque actual
                if (newText.equals(currentBlock, ignoreCase = true)) {
                    binding.btnNextBlock.isEnabled = true
                    binding.btnNextBlock.visibility = View.VISIBLE
                } else {
                    binding.btnNextBlock.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showCurrentBlock() {
        if (currentBlockIndex < textBlocks.size) {
            binding.tvSentence.text = textBlocks[currentBlockIndex]
            binding.etInput.setText("")
            binding.etInput.isEnabled = true
            binding.btnNextBlock.isEnabled = false

            // Enfocar el EditText y mostrar teclado
            binding.etInput.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun nextBlock() {
        // Guardar el texto actual del bloque antes de avanzar
        val currentText = binding.etInput.text.toString()

        // Solo guardar si hay texto (evitar bloques vacíos)
        if (currentText.isNotBlank()){
            allUserTexts.add(currentText)
        }

        if (currentBlockIndex < textBlocks.size - 1) {
            currentBlockIndex++
            showCurrentBlock()
        } else {
            // Último bloque completado
            endTest()
        }
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
        currentBlockIndex = 0
        startTime = System.currentTimeMillis()

        Log.d("Test7", "Iniciando ttest - mostrando bloque $currentBlockIndex")

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
        binding.btnNextBlock.visibility = View.VISIBLE
        binding.tvSentence.visibility = View.VISIBLE
        binding.btnStart.visibility = View.GONE

        // Mostrar teclado virtual
        binding.etInput.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etInput, InputMethodManager.SHOW_IMPLICIT)

        // Mostrar el primer bloque
        showCurrentBlock()

        // Temporizador de 5 minutos para el test completo
        testTimer = object : CountDownTimer(300000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = "Tiempo restante: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                endTest()
            }
        }.start()
    }

    private fun endTest() {
        testStarted = false
        endTime = System.currentTimeMillis()
        testTimer?.cancel()
        binding.chronometer.stop()
        binding.etInput.isEnabled = false
        binding.btnNextBlock.isEnabled = false

        //Guardar el texto del bloque actual solo si no se había guardado
        val currentText = binding.etInput.text.toString()
        if (currentText.isNotBlank() && (allUserTexts.size <= currentBlockIndex || allUserTexts.lastOrNull() != currentText)){
            allUserTexts.add(currentText)
        }

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
            val correctPresses = keyEvents.count { it.action == "INSERT" && it.correct }
            val incorrectPresses = totalPresses - correctPresses
            val accuracy = if (totalPresses > 0) (correctPresses.toDouble() / totalPresses * 100) else 0.0
            val speed = if (totalTime > 0) totalPresses / (totalTime / 1000.0) else 0.0
            val completedBlocks = (currentBlockIndex + 1).coerceAtMost(textBlocks.size)
            val allBlocksCompleted = currentBlockIndex == textBlocks.size - 1 &&
                    binding.etInput.text.toString().equals(textBlocks.last(), ignoreCase = true)

            val fullUserText = allUserTexts.joinToString("")
            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val username = sharedPref.getString("username", "unknown") ?: "unknown"

            // Crear JSON con los datos
            val testData = JSONObject().apply {
                put("test_name", "test7")
                put("username", username)
                put("start_time", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(startTime)))
                put("total_time_ms", totalTime)
                put("target_text", textBlocks.joinToString(" "))
                put("completed_blocks", completedBlocks)
                put("total_blocks", textBlocks.size)
                put("all_blocks_completed", allBlocksCompleted)
                put("final_text", fullUserText)
                put("total_presses", totalPresses)
                put("correct_presses", correctPresses)
                put("incorrect_presses", incorrectPresses)
                put("accuracy", accuracy)
                put("speed_keystrokes_per_sec", speed)

                // Agregar todos los bloques de texto
                val blocksArray = JSONArray()
                textBlocks.forEach { block ->
                    blocksArray.put(block)
                }
                put("text_blocks", blocksArray)

                // Agregar todos los eventos
                val eventsArray = JSONArray()
                keyEvents.forEach { event ->
                    JSONObject().apply {
                        put("char", event.char?.toString() ?: "")
                        put("timestamp", event.timestamp)
                        put("action", event.action)
                        put("current_text", event.currentText)
                        put("correct", event.correct)
                        put("block_index", event.blockIndex)
                    }.let { eventsArray.put(it) }
                }
                put("key_events", eventsArray)
            }

            // Guardar archivo
            val fileName = "test7_${getUsername()}_${System.currentTimeMillis()}.json"
            File(filesDir, fileName).writeText(testData.toString())

            sharedPref.edit().putBoolean("test7_completed", true).apply()
            Log.d("Test7", "Datos guardados correctamente")
        } catch (e: Exception) {
            Log.e("Test7", "Error al guardar datos", e)
        }

        // Marcar test como completado para este usuario
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("${getUsername()}_test7_completed", true)
            apply()
        }
        setResult(RESULT_OK)
    }

    private fun getUsername(): String {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("username", "") ?: ""
    }

    private fun showResults() {
        val completedBlocks = (currentBlockIndex + 1).coerceAtMost(textBlocks.size)
        val message = if (completedBlocks == textBlocks.size) {
            "¡Felicidades! Completaste todos los bloques."
        } else {
            "Completaste $completedBlocks de ${textBlocks.size} bloques."
        }

        AlertDialog.Builder(this)
            .setTitle("Test completado")
            .setMessage("$message\n\n¿Qué deseas hacer?")
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
        binding.btnNextBlock.visibility = View.GONE
        binding.tvSentence.visibility = View.GONE
        // Detener y ocultar cronómetro
        binding.chronometer.visibility = View.GONE
        binding.chronometer.stop()
        // Reactivar el botón de inicio
        binding.btnStart.visibility = View.VISIBLE
        binding.btnStart.isEnabled = true
        // Restablecer el contador
        binding.tvCountdown.text = ""
        binding.tvCountdown.visibility = View.GONE
        // Reiniciar estado del test
        testStarted = false
        currentBlockIndex = 0
        keyEvents.clear()
        allUserTexts.clear()
        // Restablecer el texto del primer bloque
        binding.tvSentence.text = textBlocks.firstOrNull()?:""
    }
    override fun onDestroy() {
        super.onDestroy()
        testTimer?.cancel()
    }


    }