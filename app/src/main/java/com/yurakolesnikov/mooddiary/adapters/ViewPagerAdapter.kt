package com.yurakolesnikov.mooddiary.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivity
import com.yurakolesnikov.mooddiary.ui.PageFragment

class ViewPagerAdapter(
    private val owner: MainActivity,
    private var pages: MutableList<PageFragment>
) : FragmentStateAdapter(owner) {

    var pageIds = pages.map { it.hashCode().toLong() } // Create array of unique ids for check in containsItem

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }

    // Next 2 fun are for right fragments displaying
    // Without them adapter doesn't clear deleted fragments and try to reuse them
    override fun getItemId(position: Int): Long {
        return pages[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return pageIds.contains(itemId)
    }
}