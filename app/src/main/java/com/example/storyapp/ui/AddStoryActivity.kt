package com.example.storyapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.storyapp.R
import com.example.storyapp.RetrofitClient
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.google.firebase.appdistribution.gradle.models.UploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var currentPhotoPath: String? = null
    private var getFile: File? = null
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        token = preferences.getString("token", "") ?: ""

        if (token.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Add enter animation for views
        binding.previewImage.alpha = 0f
        binding.buttonContainer.alpha = 0f
        binding.descriptionLayout.alpha = 0f
        binding.uploadButton.alpha = 0f

        binding.previewImage.animate()
            .alpha(1f)
            .setDuration(300)
            .withEndAction {
                binding.buttonContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .withEndAction {
                        binding.descriptionLayout.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .withEndAction {
                                binding.uploadButton.animate()
                                    .alpha(1f)
                                    .setDuration(300)
                            }
                    }
            }

        binding.cameraButton.setOnClickListener { startCamera() }
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.uploadButton.setOnClickListener { uploadImage() }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val file = File(currentPhotoPath!!)
            getFile = file
            binding.previewImage.setImageURI(Uri.fromFile(file))
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val file = uriToFile(uri, this)
                getFile = file
                binding.previewImage.setImageURI(uri)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.CAMERA] ?: false -> startCamera()
                permissions[if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }] ?: false -> startGallery()
                else -> {
                    Toast.makeText(this, "Izin ditolak", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        if (!checkPermission(Manifest.permission.CAMERA)) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "${applicationContext.packageName}.fileprovider",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            try {
                launcherIntentCamera.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkPermission(Manifest.permission.READ_MEDIA_IMAGES)) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                return
            }
        } else {
            if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                return
            }
        }

        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        try {
            val chooser = Intent.createChooser(intent, "Pilih gambar")
            launcherIntentGallery.launch(chooser)
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuka galeri", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage() {
        if (getFile == null) {
            Toast.makeText(this, "Silakan pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val description = binding.descriptionEditText.text.toString()
        if (description.isEmpty()) {
            Toast.makeText(this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val file = getFile as File
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )
        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        val bearerToken = "Bearer $token"

        // Tampilkan loading bar
        binding.loadingProgressBar.visibility = View.VISIBLE

        RetrofitClient.instance.uploadImage(bearerToken, imageMultipart, descriptionRequestBody)
            .enqueue(object : Callback<UploadResponse> {
                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    // Mensimulasikan delay sebelum loading selesai
                    Handler(mainLooper).postDelayed({
                        binding.loadingProgressBar.visibility = View.GONE

                        if (response.isSuccessful) {
                            Toast.makeText(this@AddStoryActivity, "Upload berhasil", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            if (response.code() == 401) {
                                getSharedPreferences("user_session", MODE_PRIVATE)
                                    .edit()
                                    .clear()
                                    .apply()
                                startActivity(Intent(this@AddStoryActivity, LoginActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(
                                    this@AddStoryActivity,
                                    "Upload gagal",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }, 2000) // Delay 2 detik
                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    // Mensimulasikan delay sebelum loading selesai
                    Handler(mainLooper).postDelayed({
                        binding.loadingProgressBar.visibility = View.GONE
                        Toast.makeText(this@AddStoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }, 2000) // Delay 2 detik
                }
            })
    }

    companion object {
        private fun createCustomTempFile(context: android.content.Context): File {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir: File? = context.getExternalFilesDir(null)
            return File.createTempFile(timeStamp, ".jpg", storageDir)
        }

        private fun uriToFile(uri: Uri, context: android.content.Context): File {
            val myFile = createCustomTempFile(context)
            val inputStream = context.contentResolver.openInputStream(uri) as? java.io.InputStream
            val outputStream = FileOutputStream(myFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream?.read(buffer).also { length = it ?: -1 } != -1) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.close()
            inputStream?.close()
            return myFile
        }
    }

    override fun finish() {
        super.finish()
        // Custom exit animation
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down)
    }
}
