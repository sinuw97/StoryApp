package com.example.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.storyapp.RetrofitClient
import com.example.storyapp.databinding.ActivityMapsBinding
import com.example.storyapp.models.Story
import com.example.storyapp.models.StoryResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        fetchStoriesWithLocation()
    }

    private fun fetchStoriesWithLocation() {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)


        if (token.isNullOrEmpty()) {

            Toast.makeText(this, "Token tidak valid, silakan login ulang", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }

        RetrofitClient.instance.getAllStories("Bearer $token", location = 1)
            .enqueue(object : Callback<StoryResponse> {
                override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                    if (response.isSuccessful) {
                        val stories = response.body()?.listStory
                        if (!stories.isNullOrEmpty()) {
                            stories.forEach { story ->
                                if (story.lat != null && story.lon != null) {
                                    addMarker(story)
                                }
                            }
                        } else {
                            Toast.makeText(this@MapsActivity, "Tidak ada cerita dengan lokasi", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Menangani kesalahan jika gagal memuat cerita
                        Toast.makeText(this@MapsActivity, "Gagal memuat cerita", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@MapsActivity, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addMarker(story: Story) {
        val position = LatLng(story.lat!!, story.lon!!)
        val markerOptions = MarkerOptions()
            .position(position)
            .title(story.name)
            .snippet(story.description)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

        googleMap.addMarker(markerOptions)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 5f))
    }

    private fun redirectToLogin() {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
