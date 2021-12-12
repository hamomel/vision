package com.hamomel.vision

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hamomel.vision.databinding.ActivityMainBinding
import com.hamomel.vision.searchresults.presentation.VisualSearchResultsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bitmap = resources.openRawResource(R.raw.images).use { input ->
            BitmapFactory.decodeStream(input)
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, VisualSearchResultsFragment.create(bitmap))
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        supportFragmentManager.popBackStack()
//        super.onBackPressed()
    }
}
