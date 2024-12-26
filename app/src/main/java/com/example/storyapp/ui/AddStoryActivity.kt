package com.example.storyapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.storyapp.RetrofitClient
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        animateViews()

        binding.cameraButton.setOnClickListener { startCamera() }
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.uploadButton.setOnClickListener { uploadImage() }

        // izin lokasi
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            getLastLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
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
                val file = uriToFile(uri)
                getFile = file
                binding.previewImage.setImageURI(uri)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                startCamera()
            } else if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Izin ditolak", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        if (!checkPermission(Manifest.permission.CAMERA)) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        createCustomTempFile().also { file ->
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )
            currentPhotoPath = file.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        launcherIntentGallery.launch(Intent.createChooser(intent, "Pilih gambar"))
    }

    private fun reduceImageSize(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val reducedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true)
        val outputFile = createCustomTempFile()
        val outputStream = FileOutputStream(outputFile)
        reducedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.close()
        return outputFile
    }

    private fun uploadImage() {
        if (getFile == null || binding.descriptionEditText.text!!.isEmpty()) {
            Toast.makeText(this, "Lengkapi data terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val description = binding.descriptionEditText.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val reducedFile = reduceImageSize(getFile!!)
        val requestImageFile = reducedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart = MultipartBody.Part.createFormData("photo", reducedFile.name, requestImageFile)

        binding.loadingProgressBar.visibility = View.VISIBLE

        RetrofitClient.instance.uploadImageWithLocation(
            "Bearer $token", imageMultipart, description, latitude, longitude
        ).enqueue(object : Callback<UploadResponse> {

            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                binding.loadingProgressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(this@AddStoryActivity, "Upload berhasil", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK) // Tambahkan ini
                    finish()
                } else {
                    Toast.makeText(this@AddStoryActivity, "Upload gagal", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                binding.loadingProgressBar.visibility = View.GONE
                Toast.makeText(this@AddStoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //merah dikit ga ngaruh :v
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                latitude = it.latitude
                longitude = it.longitude
            }
        }
    }

    private fun animateViews() {
        binding.previewImage.alpha = 0f
        binding.buttonContainer.alpha = 0f
        binding.descriptionLayout.alpha = 0f
        binding.uploadButton.alpha = 0f

        binding.previewImage.animate().alpha(1f).setDuration(300).withEndAction {
            binding.buttonContainer.animate().alpha(1f).setDuration(300).withEndAction {
                binding.descriptionLayout.animate().alpha(1f).setDuration(300).withEndAction {
                    binding.uploadButton.animate().alpha(1f).setDuration(300)
                }
            }
        }
    }

    private fun createCustomTempFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private fun uriToFile(uri: Uri): File {
        val contentResolver = contentResolver
        val myFile = createCustomTempFile()
        val inputStream = contentResolver.openInputStream(uri)!!
        val outputStream = FileOutputStream(myFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return myFile
    }
}
