package com.example.tremorapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.tremorapp.databinding.ActivityViewDataBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.io.File
import java.io.FileOutputStream

class ViewDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewDataBinding
    private lateinit var sharedPref: SharedPreferences
    private val currentUser: String by lazy {
        sharedPref.getString("username", "") ?: ""
    }

    // Metodo que se ejecuta cuando la actividad es creada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        loadUserData()

        binding.btnExportData.setOnClickListener { exportAllData() }    // exportar datos al hacer clic

        // Configurar el listener del botón de retroceso
        binding.btnBackMenu.setOnClickListener { finish() }
        }

    // Cargar información del usuario actual
    private fun loadUserData() {
        if (currentUser.isEmpty()) {
            binding.tvUserInfo.text = "No hay usuario identificado"
            return
        }

        binding.tvUserInfo.text = "Usuario: $currentUser"
        loadTestData()
    }

    // Cargar y mostrar datos de todos los tests
    private fun loadTestData() {
        val stringBuilder = StringBuilder().apply {
            append("\n=== Resultado de Tests ===")
            append("\n\n")
            append(loadTest1Data())

            append("\n\n")
            append(loadTest2Data())

            append("\n\n")
            append(loadTest3Data())

            append("\n\n")
            append(loadTest4Data())

            append("\n\n")
            append(loadTest5Data())

            append("\n\n")
            append(loadTest6Data())

            append("\n\n")
            append(loadTest7Data())
        }
        binding.tvTestData.text = stringBuilder.toString()
    }

    // Cargar datos específicos de los tests:
    // - Buscar archivos del test específico
    // - Leer el más reciente
    // - Extraer y formatear las métricas relevantes
    // - Devuelven string con los resultados

    private fun loadTest1Data(): String {
        val test1Files = filesDir.listFiles { file ->
            file.name.startsWith("test1_") && file.name.contains("_${currentUser}_")
        } ?: return "Test 1: No hay datos"

        if (test1Files.isEmpty()) return "Test 1: No completado"

        return try {
            val latestFile = test1Files.maxByOrNull { it.lastModified() }!!
            val json = JSONObject(latestFile.readText())

            val totalPresses = json.getJSONArray("key_events").length()
            val kPresses = json.getInt("total_k_presses")
            val otherPresses = totalPresses - kPresses
            val totalTimeSec = json.getLong("total_time_ms") / 1000.0
            val speedAll = json.getDouble("speed_keystrokes_per_sec")
            val accuracy = json.getDouble("accuracy")

            """        
                === Test 1 ===
                Usuario: ${json.getString("username")}
                Fecha: ${json.getString("start_time")}
                              
                Métricas:
                - Tiempo total: ${totalTimeSec}ms
                - Pulsaciones totales: $totalPresses
                - Pulsaciones 'k': $kPresses
                - Otras teclas: $otherPresses
                - Velocidad total: ${"%.2f".format(speedAll)} teclas/seg            
                - Precisión: ${"%.2f".format(accuracy)}%
                
                Texto final: ${json.optString("final_text", "N/A")}
            """.trimIndent()

        } catch (e: Exception) {
            "Error leyendo datos: ${e.message}"
        }
    }

    private fun loadTest2Data(): String {
        val test2Files = filesDir.listFiles { file ->
            file.name.startsWith("test2_") && file.name.contains("_${currentUser}_")
        } ?: return "Test 2: No hay datos"

        if (test2Files.isEmpty()) return "Test 2: No completado"

        return try {
            val latestFile = test2Files.maxByOrNull { it.lastModified() }!!
            val json = JSONObject(latestFile.readText())

            val totalTimeSec = json.getLong("total_time_ms") / 1000.0
            val totalPresses = json.getInt("total_presses")
            val correctPresses = json.getInt("correct_presses")
            val incorrectPresses = json.getInt("incorrect_presses")
            val accuracy = json.getDouble("accuracy")
            val targetText = json.getString("target_text")
            val speedAll = json.getDouble("speed_keystrokes_per_sec")

            """        
                === Test 2 ===
                Usuario: ${json.getString("username")}
                Fecha: ${json.getString("start_time")}
                
                Frase objetivo: 
                "$targetText"
                              
                Métricas:
                - Tiempo total: ${totalTimeSec}ms
                - Pulsaciones totales: $totalPresses
                - Pulsaciones correctas: $correctPresses
                - Errores: $incorrectPresses
                - Velocidad total: ${"%.2f".format(speedAll)} teclas/seg            
                - Precisión: ${"%.2f".format(accuracy)}%
                               
                Texto final: ${json.optString("final_text", "N/A")}
            """.trimIndent()

        } catch (e: Exception) {
            "Error leyendo datos: ${e.message}"
        }
    }

    private fun loadTest3Data(): String {
        val test3Files = filesDir.listFiles { file ->
            file.name.startsWith("test3_") && file.name.contains("_${currentUser}_")
        } ?: return "Test 3: No hay datos"

        if (test3Files.isEmpty()) return "Test 3: No completado"

        return try {
            val latestFile = test3Files.maxByOrNull { it.lastModified() }!!
            val json = JSONObject(latestFile.readText())

            val totalTimeSec = json.getLong("total_time_ms") / 1000.0
            val totalPresses = json.getInt("total_presses")
            val correctPresses = json.getInt("correct_presses")
            val incorrectPresses = json.getInt("incorrect_presses")
            val accuracy = json.getDouble("accuracy")
            val speedAll = json.getDouble("speed_keystrokes_per_sec")
            val sequences = json.getString("sequence")

                """        
                === Test 3 ===
                Usuario: ${json.getString("username")}
                Fecha: ${json.getString("start_time")}
                
                Secuencias objetivo: 
                "$sequences"
                              
                Métricas:
                - Tiempo total: ${totalTimeSec}ms
                - Pulsaciones totales: $totalPresses
                - Pulsaciones correctas: $correctPresses
                - Errores: $incorrectPresses
                - Velocidad total: ${"%.2f".format(speedAll)} teclas/seg            
                - Precisión: ${"%.2f".format(accuracy)}%
            """.trimIndent()

        } catch (e: Exception) {
            "Error leyendo datos: ${e.message}"
        }
    }

    private fun loadTest4Data(): String {
        val test4Files = filesDir.listFiles { file ->
            file.name.startsWith("test4_") && file.name.contains("_${currentUser}_")
        } ?: return "Test 4: No hay datos"

        if (test4Files.isEmpty()) return "Test 4: No completado"

        return try {
            val latestFile = test4Files.maxByOrNull { it.lastModified() }!!
            val json = JSONObject(latestFile.readText())

            val totalTimeSec = json.getLong("total_time_ms") / 1000.0
            val totalPresses = json.getInt("total_presses")
            val correctPresses = json.getInt("correct_presses")
            val incorrectPresses = json.getInt("incorrect_presses")
            val accuracy = json.getDouble("accuracy")
            val targetText = json.getString("target_text")
            val speedAll = json.getDouble("speed_keystrokes_per_sec")

            val actualChanges = json.optInt("actual_changes", -1)
            val userReported = json.optInt("user_reported", -1)
            val difference = json.optInt("difference", -1)

            """        
                === Test 4 ===
                Usuario: ${json.getString("username")}
                Fecha: ${json.getString("start_time")}
                
                Frase objetivo: 
                "$targetText"
                              
                Métricas de escritura:
                - Tiempo total: ${totalTimeSec}ms
                - Pulsaciones totales: $totalPresses
                - Pulsaciones correctas: $correctPresses
                - Errores: $incorrectPresses
                - Velocidad total: ${"%.2f".format(speedAll)} teclas/seg            
                - Precisión: ${"%.2f".format(accuracy)}%
                
                Métricas de percepción: 
                - Cambios reales de color: $actualChanges
                - Conteo del usuario: $userReported
                - Diferencias: $difference
                                             
                Texto final: ${json.optString("final_text", "N/A")}
            """.trimIndent()

        } catch (e: Exception) {
            "Error leyendo datos: ${e.message}"
        }
    }

    private fun loadTest5Data(): String {
        val test5Files = filesDir.listFiles { file ->
            file.name.startsWith("test5_") && file.name.contains("_${currentUser}_")
        } ?: return "Test 5: No hay datos"

        if (test5Files.isEmpty()) return "Test 5: No completado"

        return try {
            val latestFile = test5Files.maxByOrNull { it.lastModified() }!!
            val json = JSONObject(latestFile.readText())

            val totalTimeSec = json.getLong("total_time_ms") / 1000.0
            val totalPresses = json.getInt("total_presses")
            val correctPresses = json.getInt("correct_rhythm_presses")
            val incorrectPresses = json.getInt("total_other_presses")
            val accuracy = json.getDouble("accuracy")
            val speedAll = json.getDouble("speed_keystrokes_per_sec")

            val totalSounds = json.getInt("expected_presses_count")
            val avgDeviation = json.getDouble("average_deviation_ms")

            """        
                === Test 5 ===
                Usuario: ${json.getString("username")}
                Fecha: ${json.getString("start_time")}
                                                           
                Métricas de escritura:
                - Tiempo total: ${totalTimeSec}ms
                - Pulsaciones totales: $totalPresses
                - Pulsaciones correctas: $correctPresses
                - Errores: $incorrectPresses
                - Velocidad total: ${"%.2f".format(speedAll)} teclas/seg            
                - Precisión: ${"%.2f".format(accuracy)}%
                
                Métricas de ritmo: 
                - Sonidos emitidos: $totalSounds
                - Desviación promedio:  ${"%.1f".format(avgDeviation)} ms
                                                           
                Texto final: ${json.optString("final_text", "N/A")}
            """.trimIndent()

        } catch (e: Exception) {
            "Error leyendo datos: ${e.message}"
        }
    }

    private fun loadTest6Data(): String {
        val test6Files = filesDir.listFiles { file ->
            file.name.startsWith("test6_") && file.name.contains("_${currentUser}_")
        } ?: return "Test 6: No hay datos"

        if (test6Files.isEmpty()) return "Test 6: No completado"

        return try {
            val latestFile = test6Files.maxByOrNull { it.lastModified() }!!
            val json = JSONObject(latestFile.readText())

            val totalTimeSec = json.getLong("total_time_ms") / 1000.0
            val totalPresses = json.getInt("total_presses")
            val correctPresses = json.getInt("correct_presses")
            val incorrectPresses = json.getInt("incorrect_presses")
            val accuracy = json.getDouble("accuracy")
            val targetText = json.getString("target_text")
            val speedAll = json.getDouble("speed_keystrokes_per_sec")

            val completedPhrase  = json.optBoolean("completed_phrase", false)
            val completedInTime = json.optBoolean("completed_in_time", false)

            """        
                === Test 6 ===
                Usuario: ${json.getString("username")}
                Fecha: ${json.getString("start_time")}
                
                Frase objetivo: 
                "$targetText"
                
                Estado: ${if (completedInTime) "COMPLETADA EN TIEMPO" else "TIEMPO AGOTADO"}
                
                Frase completada: ${if (completedPhrase) "Si" else "NO"}
                              
                Métricas:
                - Tiempo total: ${totalTimeSec}ms
                - Pulsaciones totales: $totalPresses
                - Pulsaciones correctas: $correctPresses
                - Errores: $incorrectPresses
                - Velocidad total: ${"%.2f".format(speedAll)} teclas/seg            
                - Precisión: ${"%.2f".format(accuracy)}%
                               
                Texto final: ${json.optString("final_text", "N/A")}
            """.trimIndent()

        } catch (e: Exception) {
            "Error leyendo datos: ${e.message}"
        }
    }

    private fun loadTest7Data(): String {
        val test7Files = filesDir.listFiles { file ->
            file.name.startsWith("test7_") && file.name.contains("_${currentUser}_")
        } ?: return "Test 7: No hay datos"

        if (test7Files.isEmpty()) return "Test 7: No completado"

        return try {
            val latestFile = test7Files.maxByOrNull { it.lastModified() }!!
            val json = JSONObject(latestFile.readText())

            val totalTimeSec = json.getLong("total_time_ms") / 1000.0
            val totalPresses = json.getInt("total_presses")
            val correctPresses = json.getInt("correct_presses")
            val incorrectPresses = json.getInt("incorrect_presses")
            val accuracy = json.getDouble("accuracy")
            val targetText = json.getString("target_text")
            val speedAll = json.getDouble("speed_keystrokes_per_sec")

            """        
                === Test 7 ===
                Usuario: ${json.getString("username")}
                Fecha: ${json.getString("start_time")}
                
                Frase objetivo: 
                "$targetText"
                       
                Métricas:
                - Tiempo total: ${totalTimeSec}ms
                - Pulsaciones totales: $totalPresses
                - Pulsaciones correctas: $correctPresses
                - Errores: $incorrectPresses
                - Velocidad total: ${"%.2f".format(speedAll)} teclas/seg            
                - Precisión: ${"%.2f".format(accuracy)}%
                               
                Texto final: ${json.optString("final_text", "N/A")}
            """.trimIndent()

        } catch (e: Exception) {
            "Error leyendo datos: ${e.message}"
        }
    }

    // Exportar todos los datos a un archivo JSON y compartirlo
    private fun exportAllData() {
        try {
            val allData = JSONObject().apply {
                put("username", currentUser)
                put("export_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

                val testsArray = JSONArray()

                // Agregar Test 1
                getTestFilesForUser(1)?.let { files ->
                    files.maxByOrNull { it.lastModified() }?.let { file ->
                        testsArray.put(JSONObject(file.readText()))
                    }
                }
                put("tests", testsArray)

                // Agregar Test 2
                getTestFilesForUser(2)?.let { files ->
                    files.maxByOrNull { it.lastModified() }?.let { file ->
                        testsArray.put(JSONObject(file.readText()))
                    }
                }
                put("tests", testsArray)

                // Agregar Test 3
                getTestFilesForUser(3)?.let { files ->
                    files.maxByOrNull { it.lastModified() }?.let { file ->
                        testsArray.put(JSONObject(file.readText()))
                    }
                }
                put("tests", testsArray)

                // Agregar Test 4
                getTestFilesForUser(4)?.let { files ->
                    files.maxByOrNull { it.lastModified() }?.let { file ->
                        testsArray.put(JSONObject(file.readText()))
                    }
                }
                put("tests", testsArray)

                // Agregar Test 5
                getTestFilesForUser(5)?.let { files ->
                    files.maxByOrNull { it.lastModified() }?.let { file ->
                        testsArray.put(JSONObject(file.readText()))
                    }
                }
                put("tests", testsArray)

                // Agregar Test 6
                getTestFilesForUser(6)?.let { files ->
                    files.maxByOrNull { it.lastModified() }?.let { file ->
                        testsArray.put(JSONObject(file.readText()))
                    }
                }
                put("tests", testsArray)

                // Agregar Test 7
                getTestFilesForUser(7)?.let { files ->
                    files.maxByOrNull { it.lastModified() }?.let { file ->
                        testsArray.put(JSONObject(file.readText()))
                    }
                }
                put("tests", testsArray)
            }

            val exportDir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports").apply {
                if (!exists()) mkdirs()
            }

            File(exportDir, "test_data_${currentUser}_${System.currentTimeMillis()}.json").apply {
                FileOutputStream(this).use {
                    it.write(allData.toString().toByteArray())
                }

                // Compartir el archivo usando Intent
                FileProvider.getUriForFile(
                    this@ViewDataActivity,
                    "${packageName}.fileprovider",
                    this
                ).let { uri ->
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }.let { intent ->
                        startActivity(Intent.createChooser(intent, "Exportar datos a..."))
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al exportar datos: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Obtener archivos de test específicos para el usuario actual
    private fun getTestFilesForUser(testNumber: Int): Array<File>? {
        return filesDir.listFiles { file ->
            file.name.startsWith("test${testNumber}_") && file.name.contains("_${currentUser}_")
        }
    }
}
