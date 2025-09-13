package com.example.tremorapp.tests

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.example.tremorapp.databinding.ActivityTest4Binding
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
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.tremorapp.R
import kotlin.math.abs

class Test4Activity : AppCompatActivity() {

    private lateinit var binding: ActivityTest4Binding

    // Variables para controlar el estado y tiempo del test
    private var testStarted = false
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var lastText: String = ""

    // Tesxto objetivo que el usuario debe copiar
    private val targetText = "Lupra zenok tir"

    // Variables para el circulo que cambia de color
    private val colors = listOf(
        R.color.red,
        R.color.blue,
        R.color.green,
        R.color.yellow,
        R.color.purple
    )
    private var colorChangeCount = 0
    private lateinit var colorChangeTimer: CountDownTimer

    // Modelo  de datos para registrar evetos de teclado
    data class KeyPressData(
        val timestamp: Long,      // Momento del evento (ms desde inicio)
        val action: String,       // "INSERT" o "DELETE"
        val char: Char?,          // Carácter afectado
        val currentText: String,   // Texto completo en ese momento
        val correct: Boolean      // Si el carácter es correcto
    )

    // Lista para almacenar todos los eventos del tecladdo
    private val keyEvents = mutableListOf<KeyPressData>()

    // Metodo que se ejecuta cuando la actividad es creada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTest4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el listener del botón de retroceso
        binding.btnBackMenu.setOnClickListener {
            if (testStarted) {
                Toast.makeText(this, "Por favor complete el test primero", Toast.LENGTH_SHORT)
                    .show()
            } else {
                finish()
            }
        }
        setupTest()
    }

    // Manejar el botón de retroceso físico del dispositivo
    override fun onBackPressed() {
        if (testStarted) {
            Toast.makeText(this, "Por favor complete el test primero", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    // Función para configurar el test
    private fun setupTest() {
        // Mostrar la frase a copiar
        binding.tvInstruction.visibility = View.VISIBLE
        binding.tvSentence.text = targetText    // Mostrar la frase a copiar
        binding.btnStart.setOnClickListener {
            startCountdown()
        }

        // Configurar TextWatcher para detectar cambios en el texto
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                lastText = s?.toString() ?: ""  // Guardar el texto actual antes del cambio
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
                if (newText.equals(targetText, ignoreCase = true)) {
                    endTest()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Función para iniciar la cuenta regresiva de 3 segundos
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

    // Función para iniciar el test
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

        startColorAnimation()
    }

    // Función para inicial la animación del círculo
    private fun startColorAnimation() {
        binding.tvInstruction.visibility = View.GONE
        binding.circleView.visibility = View.VISIBLE
        colorChangeCount = 0

        colorChangeTimer = object : CountDownTimer(30000, 2000) { // 30 segundos de test
            override fun onTick(millisUntilFinished: Long) {
                val randomColor = colors.random()
                binding.circleView.setColorFilter(
                    ContextCompat.getColor(this@Test4Activity, randomColor),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                colorChangeCount++
            }

            override fun onFinish() {
                endTest()
            }
        }.start()
    }

    // Función para finalizar el test
    private fun endTest() {
        testStarted = false
        endTime = System.currentTimeMillis()
        binding.chronometer.stop()
        binding.etInput.isEnabled = false

        colorChangeTimer.cancel()
        showColorCountDialog()

        // Ocultar teclado virtual
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etInput.windowToken, 0)
    }

    // Función para mostrar opciones ddel usuario -> respuesta cambios de color
    private fun showColorCountDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_color_count, null)
        val input = view.findViewById<EditText>(R.id.etColorCount)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Conteo de cambios de color")
            .setMessage("¿Cuántas veces cambió el círculo de color durante el test?")
            .setView(view)
            .setPositiveButton("Continuar", null)
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val userCount = input.text.toString().toIntOrNull()

                if (userCount == null) {
                    Toast.makeText(this, "Ingresa un número valido", Toast.LENGTH_SHORT).show()
                } else {
                    dialog.dismiss()
                    showResults(userCount)
                }
            }

            // Mostrar teclado automáticamente
            input.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }
        dialog.show()
    }

    // Función para mostrar los resultados y opciones del usuario
    private fun showResults(userCount: Int) {
        AlertDialog.Builder(this)
            .setTitle("¿Qué deseas hacer?")
            .setPositiveButton("Guardar") { _, _ ->
                saveTestData(userCount)
                finish()
            }
            .setNegativeButton("Repetir") { _, _ -> resetTest() }
            .setCancelable(false)
            .show()
    }

    // Función para guardar los datos del test
    private fun saveTestData(userCount: Int) {
        try {
            val totalTime = endTime - startTime
            val totalPresses = keyEvents.count { it.action == "INSERT" }
            val incorrectPresses =  keyEvents.count { it.action == "INSERT" && !it.correct }
            val correctPresses =  keyEvents.count { it.action == "INSERT" && it.correct } - incorrectPresses
            val accuracy =
                if (totalPresses > 0) (correctPresses.toDouble() / totalPresses * 100) else 0.0
            val speed = if (totalTime > 0) totalPresses / (totalTime / 1000.0) else 0.0

            val colorChange = abs(colorChangeCount - 1)     // Nº de colores totales - 1 = veces que cambia de color
            val difference = abs(colorChange - userCount)

            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val username = sharedPref.getString("username", "unknown") ?: "unknown"

            // Crear JSON con los datos
            val testData = JSONObject().apply {
                put("test_name", "test4")
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

                put("actual_changes", colorChange)
                put("user_reported", userCount)
                put("difference", difference)

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

            // Guardar archivo JSON
            val fileName = "test4_${getUsername()}_${System.currentTimeMillis()}.json"
            File(filesDir, fileName).writeText(testData.toString())

            //Marcar test como completado para este usuario
            with(sharedPref.edit()) {
                putBoolean("${getUsername()}_test4_completed", true)
                apply()
            }
            setResult(RESULT_OK)
        } catch (e: Exception) {
            Log.e("Test4", "Error al guardar datos", e)
            setResult(RESULT_CANCELED)
        }
    }

    // Función para obtener el nombre de usuario
    private fun getUsername(): String {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("username", "") ?: ""

    }

    // Función para reiniciar el test
    private fun resetTest() {
        // Limpiar el campo de texto y habilitarlo
        binding.etInput.text.clear()
        binding.etInput.isEnabled = true
        binding.etInput.visibility = View.GONE
        binding.etInput.visibility = View.GONE
        binding.circleView.visibility = View.GONE
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
        // Hacer visible las instrucciones
        binding.tvInstruction.visibility = View.VISIBLE
    }
}