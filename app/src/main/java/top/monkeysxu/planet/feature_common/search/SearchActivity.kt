package top.monkeysxu.planet.feature_common.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.databinding.ActivitySearchBinding
import top.monkeysxu.planet.feature_common.article.ArticleDetailActivity
import top.monkeysxu.planet.feature_common.material.MaterialDetailActivity
import top.monkeysxu.planet.feature_home.adapter.ArticleAdapter
import top.monkeysxu.planet.feature_home.api.HomeApiService
import top.monkeysxu.planet.feature_material.MaterialRepository
import top.monkeysxu.planet.feature_material.adapter.MaterialAdapter
import top.monkeysxu.planet.feature_material.api.MaterialApiService

class SearchActivity : BaseActivity<ActivitySearchBinding>() {

    companion object {
        private const val EXTRA_FROM = "search_from"
        const val FROM_ARTICLE = "article"
        const val FROM_MATERIAL = "material"

        fun start(context: Context, from: String = FROM_ARTICLE) {
            context.startActivity(Intent(context, SearchActivity::class.java).apply {
                putExtra(EXTRA_FROM, from)
            })
        }
    }

    private lateinit var from: String

    // 资料搜索
    private val materialApiService by lazy {
        RetrofitClient.createMaterialRetrofit().create(MaterialApiService::class.java)
    }
    private val materialRepository by lazy { MaterialRepository(materialApiService) }
    private val materialAdapter by lazy { MaterialAdapter() }

    // 文章搜索
    private val articleApiService by lazy {
        RetrofitClient.createArticleRetrofit().create(HomeApiService::class.java)
    }
    private val articleAdapter by lazy { ArticleAdapter() }

    private var pageNum = 1
    private val pageSize = 12
    private var hasMore = true
    private var isLoading = false

    override fun inflateBinding(): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        from = intent.getStringExtra(EXTRA_FROM) ?: FROM_ARTICLE

        binding.ivBack.setOnClickListener { finish() }
        binding.ivClear.setOnClickListener {
            binding.etKeyword.text?.clear()
        }
        binding.etKeyword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        // 根据来源设置不同的 Adapter 和提示文字
        binding.rvResults.layoutManager = LinearLayoutManager(this)
        if (from == FROM_MATERIAL) {
            binding.etKeyword.hint = "搜索资料"
            binding.rvResults.adapter = materialAdapter
            materialAdapter.onItemClick = { material ->
                MaterialDetailActivity.start(this, material.id)
            }
        } else {
            binding.etKeyword.hint = "搜索公告文章"
            binding.rvResults.adapter = articleAdapter
            articleAdapter.onItemClick = { article ->
                ArticleDetailActivity.start(this, article.id)
            }
        }

        // 滚动加载更多
        binding.rvResults.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                if (!rv.canScrollVertically(1) && hasMore && !isLoading) {
                    loadMore()
                }
            }
        })
    }

    private fun performSearch() {
        val keyword = binding.etKeyword.text.toString().trim()
        if (keyword.isEmpty()) {
            Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show()
            return
        }
        pageNum = 1
        hasMore = true
        loadData(keyword, refresh = true)
    }

    private fun loadMore() {
        val keyword = binding.etKeyword.text.toString().trim()
        if (keyword.isEmpty()) return
        pageNum++
        loadData(keyword, refresh = false)
    }

    private fun loadData(keyword: String, refresh: Boolean) {
        isLoading = true
        binding.progressBar.visibility = if (refresh) View.VISIBLE else View.GONE
        binding.emptyLayout.visibility = View.GONE

        lifecycleScope.launch {
            try {
                if (from == FROM_MATERIAL) {
                    loadMaterials(keyword, refresh)
                } else {
                    loadArticles(keyword, refresh)
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "搜索失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                isLoading = false
            }
        }
    }

    private suspend fun loadMaterials(keyword: String, refresh: Boolean) {
        val result = materialRepository.getMaterialList(keyword = keyword, pageNum = pageNum)
        when (result) {
            is Resource.Success -> {
                if (refresh) {
                    materialAdapter.setData(result.data)
                } else {
                    materialAdapter.addData(result.data)
                }
                hasMore = result.data.size >= pageSize
                if (refresh && result.data.isEmpty()) {
                    binding.tvEmpty.text = "暂无搜索结果"
                    binding.emptyLayout.visibility = View.VISIBLE
                }
            }
            is Resource.Error -> {
                Toast.makeText(this@SearchActivity, result.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    private suspend fun loadArticles(keyword: String, refresh: Boolean) {
        val response = articleApiService.getArticleList(
            pageNum = pageNum,
            pageSize = pageSize,
            keywords = keyword
        )
        if (response.isSuccess && response.listData != null) {
            val articles = response.listData!!
            if (refresh) {
                articleAdapter.setData(articles)
            } else {
                articleAdapter.appendData(articles)
            }
            hasMore = articles.size >= pageSize
            if (refresh && articles.isEmpty()) {
                binding.tvEmpty.text = "暂无搜索结果"
                binding.emptyLayout.visibility = View.VISIBLE
            }
        } else {
            Toast.makeText(this@SearchActivity, "搜索失败", Toast.LENGTH_SHORT).show()
        }
    }
}
