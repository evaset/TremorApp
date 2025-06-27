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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestsMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        initUi()
    }

    private fun initUi() {
        setupTestButtons()
        setupViewDataButton()
        updateCheckboxStates()
    }

    private fun setupTestButtons() {
        binding.btnTest1.setOnClickListener {
            launchTestActivity(
                Test1Activity::class.java,
                "test1_completed",
                binding.cbTest1
            )
        }
        binding.btnTest2.setOnClickListener {
            launchTestActivity(
                Test2Activity::class.java,
                "test2_completed",
                binding.cbTest2
            )
        }
        binding.btnTest3.setOnClickListener {
            launchTestActivity(
                Test3Activity::class.java,
                "test3_completed",
                binding.cbTest3
            )
        }
        binding.btnTest4.setOnClickListener {
            launchTestActivity(
                Test4Activity::class.java,
                "test4_completed",
                binding.cbTest4
            )
        }
        binding.btnTest5.setOnClickListener {
            launchTestActivity(
                Test5Activity::class.java,
                "test5_completed",
                binding.cbTest5
            )
        }
        binding.btnTest6.setOnClickListener {
            launchTestActivity(
                Test6Activity::class.java,
                "test6_completed",
                binding.cbTest6
            )
        }
        binding.btnTest7.setOnClickListener {
            launchTestActivity(
                Test7Activity::class.java,
                "test7_completed",
                binding.cbTest7
            )
        }
    }

    private fun launchTestActivity(activityClass: Class<*>, prefKey: String, checkbox: CheckBox) {
        val intent = Intent(this, activityClass)
        startActivityForResult(intent, checkbox.id) //ID del checkbox como requestCode
    }

    private fun setupViewDataButton() {
        binding.btnViewData.setOnClickListener {
            val intent = Intent(this, ViewDataActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateCheckboxStates() {
        updateCheckboxState("test1_completed", binding.cbTest1)
        updateCheckboxState("test2_completed", binding.cbTest2)
        updateCheckboxState("test3_completed", binding.cbTest3)
        updateCheckboxState("test4_completed", binding.cbTest4)
        updateCheckboxState("test5_completed", binding.cbTest5)
        updateCheckboxState("test6_completed", binding.cbTest6)
        updateCheckboxState("test7_completed", binding.cbTest7)
    }

    private fun updateCheckboxState(prefKey: String, checkbox: CheckBox) {
        checkbox.isChecked = sharedPref.getBoolean(prefKey, false)
        checkbox.isEnabled = false //Deshabilitar la interacción manual
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                binding.cbTest1.id -> markTestAsCompleted("test1_completed", binding.cbTest1)
                binding.cbTest2.id -> markTestAsCompleted("test2_completed", binding.cbTest2)
                binding.cbTest3.id -> markTestAsCompleted("test3_completed", binding.cbTest3)
                binding.cbTest4.id -> markTestAsCompleted("test4_completed", binding.cbTest4)
                binding.cbTest5.id -> markTestAsCompleted("test5_completed", binding.cbTest5)
                binding.cbTest6.id -> markTestAsCompleted("test6_completed", binding.cbTest6)
                binding.cbTest7.id -> markTestAsCompleted("test7_completed", binding.cbTest7)
            }
        }
    }

    private fun markTestAsCompleted(prefKey: String, checkbox: CheckBox) {
        with(sharedPref.edit()) {
            putBoolean(prefKey, true)
            apply()
        }
        checkbox.isChecked = true
    }

    override fun onResume() {
        super.onResume()
        updateCheckboxStates() //Actualizar estados al volver a Tests Menu
    }
}

