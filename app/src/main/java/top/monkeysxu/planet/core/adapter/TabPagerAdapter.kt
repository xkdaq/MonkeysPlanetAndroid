package top.monkeysxu.planet.core.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import top.monkeysxu.planet.feature_exam.ExamFragment
import top.monkeysxu.planet.feature_home.HomeFragment
import top.monkeysxu.planet.feature_material.MaterialFragment
import top.monkeysxu.planet.feature_profile.ProfileFragment

class TabPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf<Fragment>(
        HomeFragment(),
        MaterialFragment(),
        ExamFragment(),
        ProfileFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getFragmentAt(position: Int): Fragment = fragments[position]
}
