package com.example.tremorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.tremorapp.databinding.ActivityMainBinding



class MainActivity : AppCompactActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
    }

    private fun initUi(){
        initListener()
    }

    private fun initListener(){
        binding.btnContinue.setOnClickListener {
            val username = binding.etUserName.text.toString().trim()
            if (username.isEmpty()){
                Toast.makeText(this, "Por favor ingresa un nombre de usuario", Toast.LENGTH_SHORT).show()
            }
            else{
                //Save user name
                val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                with(sharedPref.edit()){
                    putString("username", username)
                    apply()
                }
                //Navigate to next window
                val intent = Intent(this, TestsMenuActivity::class.java)
                startActivity(intent)
            }
        }
    }
}







