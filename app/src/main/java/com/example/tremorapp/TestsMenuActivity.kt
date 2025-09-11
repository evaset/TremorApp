package com.example.tremorapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tremorapp.databinding.ActivityTestsMenuBinding
import android.content.Intent
import android.content.SharedPreferences
import android.widget.CheckBox
import com.example.tremorapp.tests.Test1Activity
import com.example.tremorapp.tests.Test2Activity
import com.example.tremorapp.tests.Test3Activity
import com.example.tremorapp.tests.Test4Activity
import com.example.tremorapp.tests.Test5Activity
import com.example.tremorapp.tests.Test6Activity
import com.example.tremorapp.tests.Test7Activity

class TestsMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestsMenuBinding
    private lateinit var sharedPref: SharedPreferences

    // Propiedad lazy para obtener el nombre del usuario actual
    private val currentUser: String by lazy {
        sharedPref.getString("username", "") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestsMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar el SharedPreferences
        sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Configurar el listener del botón de retroceso
        binding.btnBack.setOnClickListener {
            finish() // Cierra esta activity y vuelve a la anterior
        }

        initUi()
    }

    // Función para inicializar la iterfaz del usuario
    private fun initUi() {
        setupTestButtons()
        setupViewDataButton()
        updateCheckboxStates()
    }

    private fun getTestCompletionKey(testNumber: Int): String {
        return "${currentUser}_test${testNumber}_completed"
    }

    // Función para configurar los listeners de los botones de tests
    private fun setupTestButtons() {
        binding.btnTest1.setOnClickListener {
            launchTestActivity(
                Test1Activity::class.java,
                1,
                binding.cbTest1
            )
        }
        binding.btnTest2.setOnClickListener {
            launchTestActivity(
                Test2Activity::class.java,
                2,
                binding.cbTest2
            )
        }
        binding.btnTest3.setOnClickListener {
            launchTestActivity(
                Test3Activity::class.java,
                3,
                binding.cbTest3
            )
        }
        binding.btnTest4.setOnClickListener {
            launchTestActivity(
                Test4Activity::class.java,
                4,
                binding.cbTest4
            )
        }
        binding.btnTest5.setOnClickListener {
            launchTestActivity(
                Test5Activity::class.java,
                5,
                binding.cbTest5
            )
        }
        binding.btnTest6.setOnClickListener {
            launchTestActivity(
                Test6Activity::class.java,
                6,
                binding.cbTest6
            )
        }
        binding.btnTest7.setOnClickListener {
            launchTestActivity(
                Test7Activity::class.java,
                7,
                binding.cbTest7
            )
        }
    }

    // Función para lanzar una actividad de test
    private fun launchTestActivity(activityClass: Class<*>, testNumber: Int, checkbox: CheckBox) {
        val intent = Intent(this, activityClass)
        startActivityForResult(intent, checkbox.id) //ID del checkbox como requestCode
    }

    // Función para configurar el botón de ver datos
    private fun setupViewDataButton() {
        binding.btnViewData.setOnClickListener {
            val intent = Intent(this, ViewDataActivity::class.java)
            startActivity(intent)
        }
    }

    // Función para actualizar el estado de todos los checkboxes
    private fun updateCheckboxStates() {
        updateCheckboxState(1, binding.cbTest1)
        updateCheckboxState(2, binding.cbTest2)
        updateCheckboxState(3, binding.cbTest3)
        updateCheckboxState(4, binding.cbTest4)
        updateCheckboxState(5, binding.cbTest5)
        updateCheckboxState(6, binding.cbTest6)
        updateCheckboxState(7, binding.cbTest7)
    }

    // Función para actualizar el estado de un checkbox individual
    private fun updateCheckboxState(testNumber: Int, checkbox: CheckBox) {
        // Verificar tanto SharedPreferences como archivo de datos
        val prefKey = "${currentUser}_test${testNumber}_completed"
        val hasData = filesDir.listFiles { file ->
            file.name.startsWith("test${testNumber}_") && file.name.contains("_${currentUser}_")
        }?.isNotEmpty() ?: false

        // Marcar como complero solo si existe en ambos lugares
        checkbox.isChecked = sharedPref.getBoolean(prefKey, false) && hasData
        checkbox.isEnabled = false // Deshabilitar la interacción manual
    }

    /* Método que maneja los resultados de actividades lanzadas con startActivityForResult */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si el resultado es exitoso, marcar el test como oompletado
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                binding.cbTest1.id -> markTestAsCompleted(1, binding.cbTest1)
                binding.cbTest2.id -> markTestAsCompleted(2, binding.cbTest2)
                binding.cbTest3.id -> markTestAsCompleted(3, binding.cbTest3)
                binding.cbTest4.id -> markTestAsCompleted(4, binding.cbTest4)
                binding.cbTest5.id -> markTestAsCompleted(5, binding.cbTest5)
                binding.cbTest6.id -> markTestAsCompleted(6, binding.cbTest6)
                binding.cbTest7.id -> markTestAsCompleted(7, binding.cbTest7)
            }
        }
    }

    // Función para marcar un test como completado en SharedPreferences
    private fun markTestAsCompleted(testNumber: Int, checkbox: CheckBox) {
        with(sharedPref.edit()) {
            putBoolean(getTestCompletionKey(testNumber), true)
            apply()
        }
        checkbox.isChecked = true
    }

    // Metodo que se ejecuta cuando la actividad se reanuda
    override fun onResume() {
        super.onResume()
        updateCheckboxStates() // Actualizar estados al volver a Tests Menu
    }
}

