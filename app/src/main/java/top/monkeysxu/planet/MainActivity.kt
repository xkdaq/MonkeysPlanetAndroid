package top.monkeysxu.planet

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import top.monkeysxu.planet.core.adapter.TabPagerAdapter
import top.monkeysxu.planet.core.base.Refreshable
import top.monkeysxu.planet.core.ext.loadSystemBar
import top.monkeysxu.planet.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pagerAdapter: TabPagerAdapter

    /** 每个 Tab 的 [icon, text] 视图对 */
    private val tabViews = mutableListOf<Pair<ImageView, TextView>>()

    /** Tab 图标资源：[normal, selected] */
    private val tabIcons = listOf(
        Pair(R.drawable.ic_nav_home, R.drawable.ic_nav_home_selected),
        Pair(R.drawable.ic_nav_material, R.drawable.ic_nav_material_selected),
        Pair(R.drawable.ic_nav_exam, R.drawable.ic_nav_exam_selected),
        Pair(R.drawable.ic_nav_profile, R.drawable.ic_nav_profile_selected)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.loadSystemBar(tops = 0)
        setupViewPager()
        setupTabs()
    }

    private fun setupViewPager() {
        pagerAdapter = TabPagerAdapter(this)
        binding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false
            offscreenPageLimit = 4 // 预加载所有页面
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabSelection(position)
                // 通知当前 Fragment 刷新
                val fragment = pagerAdapter.getFragmentAt(position)
                (fragment as? Refreshable)?.onTabSelected()
            }
        })
    }

    private fun setupTabs() {
        // 收集 4 个 Tab 的 icon + text
        tabViews.clear()
        tabViews.add(Pair(binding.tabHomeIcon, binding.tabHomeText))
        tabViews.add(Pair(binding.tabMaterialIcon, binding.tabMaterialText))
        tabViews.add(Pair(binding.tabExamIcon, binding.tabExamText))
        tabViews.add(Pair(binding.tabProfileIcon, binding.tabProfileText))

        // 点击切换
        binding.tabHome.setOnClickListener { binding.viewPager.setCurrentItem(0, false) }
        binding.tabMaterial.setOnClickListener { binding.viewPager.setCurrentItem(1, false) }
        binding.tabExam.setOnClickListener { binding.viewPager.setCurrentItem(2, false) }
        binding.tabProfile.setOnClickListener { binding.viewPager.setCurrentItem(3, false) }

        // 默认选中第一个
        updateTabSelection(0)
    }

    private fun updateTabSelection(selectedPosition: Int) {
        val selectedColor = ContextCompat.getColor(this, R.color.primary)
        val normalColor = ContextCompat.getColor(this, R.color.text_hint)

        tabViews.forEachIndexed { index, (icon, text) ->
            val isSelected = index == selectedPosition
            // 切换图标（使用小程序原生 PNG）
            val (normalRes, selectedRes) = tabIcons[index]
            icon.setImageResource(if (isSelected) selectedRes else normalRes)
            icon.clearColorFilter()
            // 切换文字颜色
            text.setTextColor(if (isSelected) selectedColor else normalColor)
        }
    }
}
