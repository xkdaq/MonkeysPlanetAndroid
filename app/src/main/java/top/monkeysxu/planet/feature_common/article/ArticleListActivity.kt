package top.monkeysxu.planet.feature_common.article

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.ViewLimitStore
import top.monkeysxu.planet.databinding.ActivityArticleListBinding
import top.monkeysxu.planet.feature_home.adapter.ArticleAdapter
import top.monkeysxu.planet.feature_home.api.HomeApiService

class ArticleListActivity : BaseActivity<ActivityArticleListBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ArticleListActivity::class.java))
        }
    }

    private val apiService by lazy {
        RetrofitClient.createArticleRetrofit()
            .create(HomeApiService::class.java)
    }
    private val articleAdapter by lazy { ArticleAdapter() }
    private val viewLimitStore by lazy { ViewLimitStore(this) }

    private var pageNum = 1
    private val pageSize = 12
    private var hasMore = true
    private var isLoading = false

    override fun inflateBinding(): ActivityArticleListBinding {
        return ActivityArticleListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.toolbar.ivBack.setOnClickListener { finish() }
        binding.toolbar.tvTitle.text = "最新公告"

        binding.rvArticles.layoutManager = LinearLayoutManager(this)
        binding.rvArticles.adapter = articleAdapter
        articleAdapter.onItemClick = { article ->
            ArticleDetailActivity.start(this, article.id)
        }

        // 滚动到底部加载更多
        binding.rvArticles.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && hasMore && !isLoading) {
                    loadMore()
                }
            }
        })

        loadArticles(refresh = true)
    }

    private fun loadArticles(refresh: Boolean) {
        if (isLoading) return
        isLoading = true

        if (refresh) {
            pageNum = 1
            hasMore = true
        } else {
            pageNum++
        }

        lifecycleScope.launch {
            try {
                val response = apiService.getArticleList(pageNum = pageNum, pageSize = pageSize)
                if (response.isSuccess) {
                    val list = response.listData ?: emptyList()
                    if (refresh) {
                        articleAdapter.setData(list)
                    } else {
                        articleAdapter.appendData(list)
                    }
                    hasMore = list.size >= pageSize
                } else {
                    Toast.makeText(this@ArticleListActivity, "加载失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ArticleListActivity, "网络异常", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadMore() {
        loadArticles(refresh = false)
    }
}
