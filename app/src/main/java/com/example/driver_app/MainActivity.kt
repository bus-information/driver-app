package com.example.driver_app

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

val db = Firebase.firestore

// Create a new user with a first and last name
val user = hashMapOf(
    "first" to "Ada",
    "last" to "Lovelace",
    "born" to 1815
)

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var callback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val getButton:Button = findViewById(R.id.get_button)
        getButton.setOnClickListener {
            fetchLatestLocation()
        }

        callback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
            }
        }
    }

    private fun fetchLatestLocation() {
        var latestLocation = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                101
            )
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                101
            )
        } else {
            latestLocation.addOnSuccessListener {
                if (it != null) {
                    Toast.makeText(this, "${it.latitude} \n ${it.longitude}", Toast.LENGTH_SHORT).show()
                }
                val locationRequest = createLocationRequest()?:return@addOnSuccessListener
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    callback,
                    null
                )
            }
        }
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

}

