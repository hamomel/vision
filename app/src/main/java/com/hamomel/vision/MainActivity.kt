package com.hamomel.vision

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hamomel.vision.camerascreen.presintation.CameraFragment
import com.hamomel.vision.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, CameraFragment.create())
            .commit()
    }
}
