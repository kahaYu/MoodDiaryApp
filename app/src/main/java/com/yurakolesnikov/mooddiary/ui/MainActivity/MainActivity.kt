package com.yurakolesnikov.mooddiary.ui.MainActivity

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        removeBars() // Remove status and action bars.


    }

    fun removeBars () {
        supportActionBar?.hide()

        // Makes background of StatusBar transparent.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w: Window = window
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }
}