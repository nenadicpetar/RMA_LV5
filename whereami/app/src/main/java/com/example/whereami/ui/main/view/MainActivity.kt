package com.example.whereami.ui.main.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.whereami.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.dataContainer, MainFragment.newInstance())
                .commitNow()

            supportFragmentManager.beginTransaction()
                .replace(R.id.mapContainer, MapFragment.newInstance())
                .commitNow()
        }
    }
}