package top.monkeysxu.planet.feature_home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.youth.banner.indicator.CircleIndicator
import kotlinx.coroutines.launch
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseFragment
import top.monkeysxu.planet.core.base.Refreshable
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.ViewLimitStore
import top.monkeysxu.planet.databinding.FragmentHomeBinding
import top.monkeysxu.planet.feature_common.article.ArticleDetailActivity
import top.monkeysxu.planet.feature_common.article.ArticleListActivity
import top.monkeysxu.planet.feature_common.material.MaterialListActivity
import top.monkeysxu.planet.feature_common.search.SearchActivity
import top.monkeysxu.planet.feature_common.webview.WebViewActivity
import top.monkeysxu.planet.feature_home.adapter.ArticleAdapter
import top.monkeysxu.planet.feature_home.adapter.HomeBannerAdapter
import top.monkeysxu.planet.feature_home.adapter.PanArticleAdapter
import top.monkeysxu.planet.feature_home.api.HomeApiService

class HomeFragment : BaseFragment<FragmentHomeBinding>(), Refreshable {

    private val apiService by lazy {
        RetrofitClient.createArticleRetrofit()
            .create(HomeApiService::class.java)
    }
    private val repository by lazy { HomeRepository(apiService) }
    private val viewModel: HomeViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(repository) as T
            }
        }
    }

    private val articleAdapter by lazy { ArticleAdapter() }
    private val panAdapter by lazy { PanArticleAdapter() }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<android.widget.TextView>(R.id.tvTitle).text = "首页"
        initView()
        observeViewModel()
        viewModel.loadHomeData()
    }

    override fun onTabSelected() {
        viewModel.loadHomeData()
    }

    private fun initView() {
        // 下拉刷新
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        // 搜索框
        binding.searchBox.setOnClickListener {
            SearchActivity.start(requireContext(), SearchActivity.FROM_ARTICLE)
        }

        val viewLimitStore = ViewLimitStore(requireContext())

        // 文章列表
        binding.rvArticles.layoutManager = LinearLayoutManager(context)
        binding.rvArticles.adapter = articleAdapter
        articleAdapter.maxItems = 10  // 首页最多显示10条
        articleAdapter.onItemClick = { article ->
            onArticleClick(article.id)
        }

        // 网盘列表
        binding.rvPanArticles.layoutManager = LinearLayoutManager(context)
        binding.rvPanArticles.adapter = panAdapter
        panAdapter.onItemClick = { pan ->
            onArticleClick(pan.id)
        }

        // 查看更多
        binding.tvMoreArticles.setOnClickListener {
            ArticleListActivity.start(requireContext())
        }
        binding.tvMorePans.setOnClickListener {
            MaterialListActivity.start(requireContext())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.homeData.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Resource.Loading -> {
                            if (!binding.swipeRefresh.isRefreshing) {
                                binding.swipeRefresh.isRefreshing = true
                            }
                        }

                        is Resource.Success -> {
                            binding.swipeRefresh.isRefreshing = false
                            result.data?.let { bindData(it) }
                        }

                        is Resource.Error -> {
                            binding.swipeRefresh.isRefreshing = false
                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun bindData(data: top.monkeysxu.planet.feature_home.model.HomeData) {
        // Banner
        data.bannerList?.let { banners ->
            if (banners.isNotEmpty()) {
                val bannerAdapter = HomeBannerAdapter(banners)
                bannerAdapter.onBannerClick = { banner ->
                    if (!banner.linkUrl.isNullOrEmpty()) {
                        if (banner.linkUrl.startsWith("http")) {
                            WebViewActivity.start(
                                requireContext(),
                                getString(R.string.app_name),
                                banner.linkUrl
                            )
                        }
                    }
                }
                binding.banner.setAdapter(bannerAdapter)
                    .addBannerLifecycleObserver(viewLifecycleOwner)
                    .indicator = CircleIndicator(context)
            }
        }

        // 公告
        data.noticeList?.let { notices ->
            if (notices.isNotEmpty()) {
                binding.noticeFlipper.removeAllViews()
                notices.forEach { notice ->
                    val textView = TextView(context).apply {
                        text = notice.title
                        textSize = 14f
                        setTextColor(resources.getColor(R.color.text_primary, null))
                        maxLines = 1
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        setOnClickListener {
                            showNoticeDialog(notice.title, notice.content)
                        }
                    }
                    binding.noticeFlipper.addView(textView)
                }
                binding.noticeLayout.visibility = View.VISIBLE
            } else {
                binding.noticeLayout.visibility = View.GONE
            }
        }

        // 文章列表
        data.articleList?.let { articles ->
            if (articles.isNotEmpty()) {
                articleAdapter.setData(articles)
            }
        }

        // 网盘资源
        data.panArticleList?.let { pans ->
            if (pans.isNotEmpty()) {
                binding.panSection.visibility = View.VISIBLE
                panAdapter.setData(pans)
            } else {
                binding.panSection.visibility = View.GONE
            }
        }
    }

    /** 通过 ID 跳转到文章详情页 */
    private fun onArticleClick(articleId: Int) {
        ArticleDetailActivity.start(requireContext(), articleId)
    }

    /** 公告弹窗（自定义圆角卡片风格） */
    private fun showNoticeDialog(title: String, content: String?) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_notice)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.82).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = title
        dialog.findViewById<TextView>(R.id.tvDialogContent).text = content ?: "暂无内容"
        dialog.findViewById<View>(R.id.btnDialogConfirm).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}
