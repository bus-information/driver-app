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
import com.google.firebase.firestore.GeoPoint
import java.security.MessageDigest
import com.google.firebase.firestore.SetOptions
import java.time.LocalDateTime


@SuppressLint("StaticFieldLeak")
val db = Firebase.firestore
class MainActivity : AppCompatActivity(),OnMapReadyCallback  {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var callback: LocationCallback
    var a=0
    var busId = ""
    //private val handler = Handler()
    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        var x = 0
        val getButton: ToggleButton = findViewById(R.id.toggleButton)
        var timerCallback1: TimerTask.() -> Unit = {

            var str = Integer.toString(x)
            //mHandler.post{messageView.text = str}
            fetchLatestLocation()
            System.out.println(str)
            x++
        }

        getButton.setOnCheckedChangeListener  { _, isChecked ->
            if (isChecked) {
                val radioGroup = findViewById<RadioGroup>(R.id.RadioGroup)
                val id = radioGroup.checkedRadioButtonId
                val checkedRadioButton = findViewById<RadioButton>(id)
                a=1
                busId = MessageDigest.getInstance("SHA-256")
                    .digest((checkedRadioButton.text.toString() + (1..1000).random().toString()).toByteArray())
                    .joinToString(separator = "") {
                        "%02x".format(it)
                    }

                val bus = hashMapOf(
                    "stationsId" to checkedRadioButton.text,
                    "arrivalTimeId" to MessageDigest.getInstance("SHA-256")
                        .digest((checkedRadioButton.text.toString() + (1..1000).random().toString()).toByteArray())
                        .joinToString(separator = "") {
                            "%02x".format(it)
                        }
                )
                db.collection("bus")
                    .document(busId).set(bus)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                Timer().schedule(0, 1000, timerCallback1)
            }
            else if(!isChecked){
                a=0
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now_pojit,14f))
        }
        val locationRequest = createLocationRequest()?:return
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            callback,
            null
        )
    }


    private fun fetchLatestLocation() {
            val latestLocation = fusedLocationProviderClient.lastLocation

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    101
                )
            }
            else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    101
                )
            }
            else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    101
                )
            }
            else {
                val nowTime = LocalDateTime.now()
                latestLocation.addOnSuccessListener() {
                    if (a==1) {
                        if (it != null) {
                            Toast.makeText(this, "${it.latitude} \n ${it.longitude}", Toast.LENGTH_SHORT).show()
                            val radioGroup = findViewById<RadioGroup>(R.id.RadioGroup)
                            val id = radioGroup.checkedRadioButtonId
                            val checkedRadioButton = findViewById<RadioButton>(id)
                            val bus = hashMapOf(
                                "locate" to GeoPoint(it.latitude, it.longitude),
                                "timeStamp" to (nowTime.hour * 3600 + nowTime.minute * 60 + nowTime.second)
                            )
                            println(checkedRadioButton.text)
                            // Add a new document with a generated ID
                            db.collection("bus")
                                .document(busId).set(bus, SetOptions.merge())
                        }
                    }
                    else if(a==0){
                        println("fuck")
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

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}

