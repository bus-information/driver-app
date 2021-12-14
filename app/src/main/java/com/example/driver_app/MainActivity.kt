package com.example.driver_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalTime
import java.util.*
import kotlin.concurrent.schedule
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions



@SuppressLint("StaticFieldLeak")
val db = Firebase.firestore

var aId = "0"
var bLocate = "0"
var tStamp = "0"
var sId = "0"
val sList = arrayOf("")

class MainActivity : AppCompatActivity(),OnMapReadyCallback {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var callback: LocationCallback
    var a = 0
    private val handler = Handler()
    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //val mHandler = Handler()
        var x = 0
        val getButton: ToggleButton = findViewById(R.id.toggleButton)
        var timerCallback1: TimerTask.() -> Unit = {

            var str = Integer.toString(x)
            //mHandler.post{messageView.text = str}
            fetchLatestLocation()
            System.out.println(str)
            x++
        }

        getButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                a = 1
                Timer().schedule(0, 1000, timerCallback1)
            } else if (!isChecked) {
                a = 0
            }
        }

        callback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val ll = fusedLocationProviderClient.lastLocation
        ll.addOnSuccessListener {
            var now_pojit = LatLng(it.latitude, it.longitude)
            mMap.addMarker(MarkerOptions().position(now_pojit).title("現在地"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now_pojit, 14f))
        }
        val locationRequest = createLocationRequest() ?: return
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            callback,
            null
        )
    }


    private fun fetchLatestLocation() {
        val latestLocation = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                101
            )
        } else if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
        } else if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                101
            )
        } else {
            latestLocation.addOnSuccessListener {

                if (a == 1) {
                    if (it != null) {
                        Toast.makeText(
                            this,
                            "${it.latitude} \n ${it.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()
                        val radioGroup = findViewById<RadioGroup>(R.id.RadioGroup)
                        val id = radioGroup.checkedRadioButtonId
                        val checkedRadioButton = findViewById<RadioButton>(id)
                        val user = hashMapOf(
                            "latitude" to "${it.latitude}",
                            "longitude" to "${it.longitude}",
                            "going" to checkedRadioButton.text
                        )
                        println(checkedRadioButton.text)
                        // Add a new document with a generated ID
                        db.collection("bus")
                            .document("gpucZzxPYDlp5sWXhQOO")
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val document = task.result
                                    if (document != null && document.data != null) {
                                        aId = document.data?.get("arrivalTimeId").toString()
                                        bLocate = document.data?.get("locate").toString()
                                        sId = document.data?.get("stationsId").toString()
                                        tStamp = document.data?.get("timeStamp").toString()
                                    }
                                }

                                db.collection(sId)
                                    .get()
                                    .addOnSuccessListener { result ->
                                        for (document in result) {
                                            Log.d(TAG, "${document.id} => ${document.data}")
                                        }

                                    }
                                    .addOnFailureListener { exception ->
                                        Log.w(TAG, "No of Station...", exception)
                                    }
                            }
                    } else if (a == 0) {
                        println("fuck")
                    }
                    println(aId)
                    println(bLocate)
                    println(sId)
                    println(tStamp)
                    val locationRequest = createLocationRequest() ?: return@addOnSuccessListener
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        callback,
                        null
                    )
                }
            }

        }
    }
    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}