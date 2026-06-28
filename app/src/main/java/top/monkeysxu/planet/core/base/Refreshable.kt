package top.monkeysxu.planet.core.base

/**
 * Tab 页面切换刷新接口
 * 当 ViewPager2 切换到某个 Tab 时，对应 Fragment 实现此接口来刷新数据
 */
interface Refreshable {
    fun onTabSelected()
}
