package com.example.medicalsum

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.medicalsum.data.AppDatabase
import com.example.medicalsum.data.SummaryEntity
import com.example.medicalsum.repository.SummaryRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest

class MainActivity : ComponentActivity() {
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }
    private lateinit var repository: SummaryRepository
    private lateinit var btnApply: MaterialButton
    private lateinit var progressApply: ProgressBar
    private lateinit var inputText: EditText
    private var isLoading = false
    private lateinit var photoUri: Uri
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                recognizeTextFromImage(photoUri)
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { recognizeTextFromImage(it) }
        }

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { readTextFromFile(it) }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openCamera() else Toast.makeText(
                this,
                "Camera permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnApply = findViewById(R.id.btn_apply)
        progressApply = findViewById(R.id.progress_apply)
        inputText = findViewById(R.id.input_text)
        val db = AppDatabase.getDatabase(this)
        repository = SummaryRepository(db.summaryDao())

        findViewById<ImageView>(R.id.icon_camera).setOnClickListener { openCameraWithPermission() }
        findViewById<ImageView>(R.id.icon_gallery).setOnClickListener { pickImageLauncher.launch("image/*") }
        findViewById<ImageView>(R.id.icon_file).setOnClickListener { pickFileLauncher.launch("text/plain") }
        btnApply.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter some text!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isLoading) return@setOnClickListener

            startLoading()
            summarizeText(text)
        }

        setupBottomNavigation()
    }

    private fun openCameraWithPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> openCamera()

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val photoFile = File.createTempFile(
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}",
            ".jpg",
            externalCacheDir
        )
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        takePhotoLauncher.launch(photoUri)
    }

    private fun recognizeTextFromImage(uri: Uri) {
        val image = InputImage.fromFilePath(this, uri)
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                if (extractedText.isNotEmpty()) {
                    inputText.setText(extractedText)
                    inputText.setSelection(extractedText.length)
                } else {
                    Toast.makeText(this, "No text found in image", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun readTextFromFile(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val text = inputStream.bufferedReader().use { it.readText() }
                inputText.setText(text)
                inputText.setSelection(text.length)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLoading() {
        isLoading = true
        btnApply.text = ""
        progressApply.visibility = View.VISIBLE
        btnApply.isEnabled = false
    }

    private fun stopLoading() {
        isLoading = false
        btnApply.text = "Apply"
        progressApply.visibility = View.GONE
        btnApply.isEnabled = true
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_library -> {
                    startActivity(Intent(this, SummaryActivity::class.java))
                    true
                }

                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    private fun summarizeText(text: String) {
        val url = "http://10.0.2.2:8000/summarize"
        val json = JSONObject()
        json.put("text", text)
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    stopLoading()
                    Toast.makeText(this@MainActivity, "Request failed!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                runOnUiThread { stopLoading() }

                if (!response.isSuccessful || body == null) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Server error", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val jsonObj = JSONObject(body)
                val summary = jsonObj.getString("summary")
                val defaultTitle = "Title"

                lifecycleScope.launch {
                    repository.insert(
                        SummaryEntity(
                            title = defaultTitle,
                            originalText = text,
                            summaryText = summary
                        )
                    )
                    val intent = Intent(this@MainActivity, SummaryDetailsActivity::class.java)
                    intent.putExtra("summary_text", summary)
                    intent.putExtra("summary_title", defaultTitle)
                    startActivity(intent)
                }
            }
        })
    }
}