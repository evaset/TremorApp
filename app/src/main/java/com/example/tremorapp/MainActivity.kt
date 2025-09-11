package com.example.tremorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tremorapp.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Iniciar la interfaz de usuario
        initUi()
    }

    // Función para iniciar los componentes de la UI
    private fun initUi(){
        initListener()  // Configurar los listeners de los botones y otros controles
    }

    // Función para configurar los listeners
    private fun initListener(){
        binding.btnContinue.setOnClickListener {
            val username = binding.etUserName.text.toString().trim()    // Obtener y limpiar el texto del campo de usuario
            // Validar que el campo no este vacio
            if (username.isEmpty()){
                Toast.makeText(this, "Por favor ingresa un nombre de usuario", Toast.LENGTH_SHORT).show()
            }
            else{
                // Guardar el nombre de usuario en SharedPreferences
                val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                with(sharedPref.edit()){
                    putString("username", username)
                    apply()
                }
                // Navegar a la siguiente actividad (TestMenuActivity)
                val intent = Intent(this, TestsMenuActivity::class.java)
                startActivity(intent)
            }
        }
    }
}







