package com.example.chatappfinal.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatappfinal.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
    }
    private fun requestPermissions() = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.CAMERA
    ).let { requestPermissions(it, 101) }

}