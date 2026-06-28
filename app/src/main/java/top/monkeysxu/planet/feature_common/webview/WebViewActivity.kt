package top.monkeysxu.planet.feature_common.webview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.databinding.ActivityWebviewBinding

class WebViewActivity : BaseActivity<ActivityWebviewBinding>() {

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_URL = "url"
        private const val EXTRA_HTML = "html"

        fun start(context: Context, title: String, url: String) {
            context.startActivity(Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_URL, url)
            })
        }

        fun startWithHtml(context: Context, title: String, html: String) {
            context.startActivity(Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_HTML, html)
            })
        }
    }

    override fun inflateBinding(): ActivityWebviewBinding {
        return ActivityWebviewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)
        val url = intent.getStringExtra(EXTRA_URL)
        val html = intent.getStringExtra(EXTRA_HTML)

        binding.toolbar.tvTitle.text = title
        binding.toolbar.ivBack.setOnClickListener { finish() }

        setupWebView()

        if (!html.isNullOrEmpty()) {
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
        } else if (!url.isNullOrEmpty()) {
            binding.webView.loadUrl(url)
        }
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

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
