package top.monkeysxu.planet.feature_common.material

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.databinding.ActivityMaterialDetailBinding
import top.monkeysxu.planet.feature_material.api.MaterialApiService

/**
 * 资料详情页 — 通过 ID 加载，支持网盘链接跳转浏览器
 */
class MaterialDetailActivity : BaseActivity<ActivityMaterialDetailBinding>() {

    companion object {
        private const val EXTRA_ID = "material_id"

        fun start(context: Context, materialId: Int) {
            context.startActivity(Intent(context, MaterialDetailActivity::class.java).apply {
                putExtra(EXTRA_ID, materialId)
            })
        }
    }

    private val apiService by lazy {
        RetrofitClient.createMaterialRetrofit().create(MaterialApiService::class.java)
    }

    override fun inflateBinding(): ActivityMaterialDetailBinding {
        return ActivityMaterialDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.toolbar.ivBack.setOnClickListener { finish() }

        val id = intent.getIntExtra(EXTRA_ID, -1)
        if (id == -1) {
            showError("参数错误")
            return
        }
        loadDetail(id)
    }

    private fun loadDetail(id: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE
        binding.tvError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = apiService.getMaterialDetail(id)
                if (response.isSuccess && response.data != null) {
                    val material = response.data
                    bindData(material)
                } else {
                    showError("加载失败")
                }
            } catch (e: Exception) {
                showError("网络异常: ${e.message}")
            }
        }
    }

    private fun bindData(material: top.monkeysxu.planet.feature_material.model.MaterialItem) {
        binding.progressBar.visibility = View.GONE
        binding.contentLayout.visibility = View.VISIBLE

        binding.toolbar.tvTitle.text = material.title

        // 科目 / 分类标签
        val tags = material.categoryList?.takeIf { it.isNotEmpty() }
            ?: listOfNotNull(material.subjectName, material.categoryName).filter { it.isNotBlank() }
        val categories = tags.joinToString(" · ")
        if (categories.isNotEmpty()) {
            binding.tvCategories.text = categories
            binding.tvCategories.visibility = View.VISIBLE
        } else {
            binding.tvCategories.visibility = View.GONE
        }

        // 备注内容
        if (!material.linkRemark.isNullOrEmpty()) {
            binding.tvRemark.text = material.linkRemark
            binding.tvRemark.visibility = View.VISIBLE
        } else {
            binding.tvRemark.visibility = View.GONE
        }

        // 详情内容（HTML）
        if (!material.content.isNullOrEmpty()) {
            binding.webViewContent.visibility = View.VISIBLE
            binding.webViewContent.settings.javaScriptEnabled = true
            binding.webViewContent.settings.domStorageEnabled = true
            val styledHtml = """
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { padding: 0; margin: 0; font-size: 15px; line-height: 1.6; color: #333; }
                        img { max-width: 100%; height: auto; }
                        pre { background: #f5f5f5; padding: 12px; overflow-x: auto; }
                    </style>
                </head>
                <body>${material.content}</body>
                </html>
            """.trimIndent()
            binding.webViewContent.loadDataWithBaseURL(null, styledHtml, "text/html", "utf-8", null)
        } else {
            binding.webViewContent.visibility = View.GONE
        }

        // 百度网盘
        val hasBaidu = !material.baiduUrl.isNullOrEmpty()
        if (hasBaidu) {
            binding.baiduCard.visibility = View.VISIBLE
            binding.tvBaiduCode.text =
                if (!material.baiduCode.isNullOrEmpty()) "提取码: ${material.baiduCode}" else "点击打开链接"
            binding.btnOpenBaidu.setOnClickListener {
                if (!material.baiduCode.isNullOrEmpty()) {
                    copyToClipboard("提取码", material.baiduCode)
                    Toast.makeText(this, "提取码已复制", Toast.LENGTH_SHORT).show()
                }
                openInBrowser(material.baiduUrl!!)
            }
        } else {
            binding.baiduCard.visibility = View.GONE
        }

        // 夸克网盘
        val hasQuark = !material.quarkUrl.isNullOrEmpty()
        if (hasQuark) {
            binding.quarkCard.visibility = View.VISIBLE
            binding.tvQuarkCode.text =
                if (!material.quarkCode.isNullOrEmpty()) "提取码: ${material.quarkCode}" else "点击打开链接"
            binding.btnOpenQuark.setOnClickListener {
                if (!material.quarkCode.isNullOrEmpty()) {
                    copyToClipboard("提取码", material.quarkCode)
                    Toast.makeText(this, "提取码已复制", Toast.LENGTH_SHORT).show()
                }
                openInBrowser(material.quarkUrl!!)
            }
        } else {
            binding.quarkCard.visibility = View.GONE
        }

        // 无网盘链接时提示
        if (!hasBaidu && !hasQuark) {
            binding.tvNoPan.visibility = View.VISIBLE
        } else {
            binding.tvNoPan.visibility = View.GONE
        }
    }

    private fun showError(msg: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = msg
    }

    private fun openInBrowser(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
