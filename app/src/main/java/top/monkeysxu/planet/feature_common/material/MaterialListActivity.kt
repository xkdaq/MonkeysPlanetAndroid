package top.monkeysxu.planet.feature_common.material

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.ViewLimitStore
import top.monkeysxu.planet.databinding.ActivityMaterialListBinding
import top.monkeysxu.planet.feature_home.adapter.PanArticleAdapter
import top.monkeysxu.planet.feature_home.api.HomeApiService

class MaterialListActivity : BaseActivity<ActivityMaterialListBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MaterialListActivity::class.java))
        }
    }

    private val apiService by lazy {
        RetrofitClient.createArticleRetrofit()
            .create(HomeApiService::class.java)
    }
    private val panAdapter by lazy { PanArticleAdapter() }

    override fun inflateBinding(): ActivityMaterialListBinding {
        return ActivityMaterialListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.toolbar.ivBack.setOnClickListener { finish() }
        binding.toolbar.tvTitle.text = "网盘资源"

        val viewLimitStore = ViewLimitStore(this)
        binding.rvPanArticles.layoutManager = LinearLayoutManager(this)
        binding.rvPanArticles.adapter = panAdapter
        panAdapter.onItemClick = { pan ->
            MaterialDetailActivity.start(this@MaterialListActivity, pan.id)
        }

        loadPans()
    }

    private fun loadPans() {
        lifecycleScope.launch {
            try {
                val response = apiService.getHomeData()
                if (response.isSuccess) {
                    response.data?.panArticleList?.let {
                        panAdapter.setData(it)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MaterialListActivity, "加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}