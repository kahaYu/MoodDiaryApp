package com.yurakolesnikov.mooddiary.ui.MainActivity

import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.adapters.ViewPagerAdapter
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.ActivityMainBinding
import com.yurakolesnikov.mooddiary.sharedViewModels.SharedViewModel
import com.yurakolesnikov.mooddiary.ui.AddNoteFragment
import com.yurakolesnikov.mooddiary.ui.PageFragment
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import dagger.hilt.android.AndroidEntryPoint

// Всё переделать. Во-первых нужно изменить лайвдату на флоу. Во вторых понять, откуда удобнее
// надувать вьюхи и создавать страницы.

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vm: MainActivityViewModel by viewModels()
    private val sharedVM: SharedViewModel by viewModels()

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        hideSystemUI() // Extension that hides system bars.
        supportActionBar?.hide()

        viewPager = binding.viewPagerContainer
        viewPagerAdapter = ViewPagerAdapter(this,vm.pages)
        viewPager.adapter = viewPagerAdapter

        vm.pages.add(PageFragment(this))

        vm.deleteAllNotesTrigger.observe(this, Observer {
            vm.pages.add(PageFragment(this))
            viewPagerAdapter.pageIds = viewPagerAdapter.pages.map { it.hashCode().toLong() }
            viewPagerAdapter.notifyDataSetChanged()
        })
    }

    public fun createPage() {
        vm.pages.add(PageFragment(this))
        viewPagerAdapter.notifyItemInserted(vm.pages.size - 1)
        viewPager.setCurrentItem(vm.pages.lastIndex, true)
    }

}


