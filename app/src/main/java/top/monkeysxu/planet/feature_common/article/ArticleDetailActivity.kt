package top.monkeysxu.planet.feature_common.article

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.databinding.ActivityArticleDetailBinding
import top.monkeysxu.planet.feature_home.api.HomeApiService

/**
 * 文章详情页 — 先跳转再加载（参考小程序 detail 页面）
 */
class ArticleDetailActivity : BaseActivity<ActivityArticleDetailBinding>() {

    companion object {
        private const val EXTRA_ID = "article_id"

        fun start(context: Context, articleId: Int) {
            context.startActivity(Intent(context, ArticleDetailActivity::class.java).apply {
                putExtra(EXTRA_ID, articleId)
            })
        }
    }

    private val apiService by lazy {
        RetrofitClient.createArticleRetrofit()
            .create(HomeApiService::class.java)
    }

    override fun inflateBinding(): ActivityArticleDetailBinding {
        return ActivityArticleDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.toolbar.ivBack.setOnClickListener { finish() }
        setupWebView()

        val articleId = intent.getIntExtra(EXTRA_ID, -1)
        if (articleId == -1) {
            showError("参数错误")
            return
        }

        loadDetail(articleId)
    }

    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                defaultTextEncodingName = "utf-8"
            }
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
        }
    }

    private fun loadDetail(id: Int) {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = apiService.getArticleDetail(id)
                if (response.isSuccess && response.data != null) {
                    val article = response.data
                    binding.toolbar.tvTitle.text = article.title
                    if (!article.content.isNullOrEmpty()) {
                        showContent(article.content)
                    } else {
                        showError("该文章暂无内容")
                    }
                } else {
                    showError("获取详情失败")
                }
            } catch (e: Exception) {
                showError("网络异常: ${e.message}")
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE
        binding.webView.visibility = View.GONE
    }

    private fun showError(msg: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = msg
        binding.webView.visibility = View.GONE
    }

    private fun showContent(html: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE

        val styledHtml = """
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { padding: 16px; font-size: 16px; line-height: 1.6; color: #333; }
                    img { max-width: 100%; height: auto; }
                    pre { background: #f5f5f5; padding: 12px; overflow-x: auto; }
                </style>
            </head>
            <body>$html</body>
            </html>
        """.trimIndent()
        binding.webView.loadDataWithBaseURL(null, styledHtml, "text/html", "utf-8", null)
    }
}
