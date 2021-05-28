package com.example.soundboardapp

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var soundPool: SoundPool

    private var kobe: Int = 0
    private var soljacic: Int = 0
    private var zuckerberg: Int = 0
    private var hawking: Int = 0
    private var rimac: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initSound()
        loadSounds()
        picClick()

    }

    private fun picClick() {
        kobe_pic.setOnClickListener {
            playSound(kobe)
            Toast.makeText(applicationContext, "Kobe Bryant", Toast.LENGTH_SHORT).show()
        }

        soljacic_pic.setOnClickListener {
            playSound(soljacic)
            Toast.makeText(applicationContext, "Marin Soljačić", Toast.LENGTH_SHORT).show()
        }

        mark_pic.setOnClickListener {
            playSound(zuckerberg)
            Toast.makeText(applicationContext, "Mark Zuckerberg", Toast.LENGTH_SHORT).show()
        }

        hawk_pic.setOnClickListener {
            playSound(hawking)
            Toast.makeText(applicationContext, "Stephen Hawking", Toast.LENGTH_SHORT).show()
        }

        rimac_pic.setOnClickListener {
            playSound(rimac)
            Toast.makeText(applicationContext, "Mate Rimac", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    private fun initSound() {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()

            SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        }
    }

    private fun loadSounds() {
        kobe = soundPool.load(this, R.raw.kobe_sound, 1)
        soljacic = soundPool.load(this, R.raw.soljacic_sound, 1)
        zuckerberg = soundPool.load(this, R.raw.mark_sound, 1)
        hawking = soundPool.load(this, R.raw.hawking_sound, 1)
        rimac = soundPool.load(this, R.raw.rimac_sound, 1)
    }


    private fun playSound(sound: Int) {
        soundPool.play(sound, 1.0f, 1.0f, 0, 0, 1f)
    }
}