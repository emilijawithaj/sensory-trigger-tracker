package com.example.soverloadtracker.detailsViews

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * adapter for handling tabs
 */
class TabAdapter (activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun createFragment(index: Int): Fragment {
        when (index) {
            0 -> return FactorsFragment()
            1 -> return LogsHistoryFragment()
        }
        return LogsHistoryFragment()
    }

    override fun getItemCount(): Int
    {
        return 2
    }
}