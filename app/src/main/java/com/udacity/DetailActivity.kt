package com.udacity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        if (intent?.extras != null) {
            val status = intent.getStringExtra("status")
            val fileName = intent.getStringExtra("fileName")

            binding.contentDetail.fileNameText.text = fileName
            binding.contentDetail.statusText.text = status

            when (status) {
                "Fail" -> binding.contentDetail.statusText.setTextColor(Color.RED)
                "Success" -> binding.contentDetail.statusText.setTextColor(Color.GREEN)
                else -> binding.contentDetail.statusText.setTextColor(Color.BLACK)
            }
        }
    }
}
