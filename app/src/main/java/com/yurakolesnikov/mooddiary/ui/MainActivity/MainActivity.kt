package com.yurakolesnikov.mooddiary.ui.MainActivity

import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.databinding.ActivityMainBinding
import com.yurakolesnikov.mooddiary.sharedViewModels.SharedViewModel
import com.yurakolesnikov.mooddiary.ui.AddNoteFragment
import com.yurakolesnikov.mooddiary.utils.hideSystemUI

// Нужно, чтобы навигейшн бар не показывался при нажатии на нижнюю часть экрана.
// Нужно сделать цвет текста статус бара чёрным. Пока что он белый.
class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private val sharedVM: SharedViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityMainBinding.inflate(layoutInflater, null, false)

        setContentView(binding.root)
        hideSystemUI()
        supportActionBar?.hide()

        sharedVM.onAddPressed.observe(this, Observer {
            AddNoteFragment().show(supportFragmentManager, "123")
        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

    }

    fun hideUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(
                WindowInsets.Type.navigationBars()
            )
        }
    }
}


