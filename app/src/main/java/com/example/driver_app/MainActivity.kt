package com.example.driver_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

val db = Firebase.firestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}