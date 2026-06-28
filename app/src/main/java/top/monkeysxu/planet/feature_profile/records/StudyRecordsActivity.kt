package top.monkeysxu.planet.feature_profile.records

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.databinding.ActivityStudyRecordsBinding
import top.monkeysxu.planet.databinding.ItemPracticeRecordBinding
import top.monkeysxu.planet.feature_exam.api.ExamApiService
import top.monkeysxu.planet.feature_exam.model.PracticeRecord
import top.monkeysxu.planet.feature_exam.model.PracticeRecordPage
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class StudyRecordsActivity : BaseActivity<ActivityStudyRecordsBinding>() {

    private val tokenManager by lazy { TokenManager(this) }
    private val apiService by lazy {
        RetrofitClient.createMpRetrofit(tokenManager)
            .create(ExamApiService::class.java)
    }
    private val adapter by lazy { RecordAdapter() }
    private var pageNum = 1
    private val pageSize = 10
    private var isLoading = false
    private var hasMore = true
    private val records = mutableListOf<PracticeRecord>()

    override fun inflateBinding() = ActivityStudyRecordsBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        loadRecords()
    }

    private fun initView() {
        binding.toolbar.ivBack.setOnClickListener { finish() }
        binding.toolbar.tvTitle.text = "学习记录"
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            pageNum = 1
            hasMore = true
            records.clear()
            loadRecords()
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val layoutManager = rv.layoutManager as LinearLayoutManager
                if (!isLoading && hasMore && layoutManager.findLastVisibleItemPosition() >= adapter.itemCount - 3) {
                    loadRecords()
                }
            }
        })
    }

    private fun loadRecords() {
        if (isLoading) return
        isLoading = true
        lifecycleScope.launch {
            try {
                val response = apiService.getRecords(pageNum, pageSize)
                if (response.isSuccess && response.data != null) {
                    updateUI(response.data)
                } else {
                    Toast.makeText(this@StudyRecordsActivity, response.msg ?: "加载失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudyRecordsActivity, e.message ?: "网络异常", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateUI(data: PracticeRecordPage) {
        records.addAll(data.list)
        adapter.submitList(records.toList())

        binding.tvPracticeCount.text = data.total.toString()
        binding.tvTotalQuestions.text = data.totalQuestions.toString()
        binding.tvCorrectCount.text = data.totalCorrect.toString()
        binding.tvDuration.text = data.totalDurationText

        hasMore = records.size < data.total
        pageNum++

        binding.emptyView.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (records.isEmpty()) View.GONE else View.VISIBLE
    }

    private inner class RecordAdapter : RecyclerView.Adapter<RecordAdapter.VH>() {
        private var list = listOf<PracticeRecord>()

        fun submitList(newList: List<PracticeRecord>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemPracticeRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size

        inner class VH(private val binding: ItemPracticeRecordBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: PracticeRecord) {
                binding.tvType.text = item.practiceTypeName
                binding.tvTime.text = item.createTime
                binding.tvBank.text = item.bankName
                binding.tvCount.text = "${item.correctCount}/${item.totalCount}题"
                binding.tvAccuracy.text = "${item.accuracy}%"
                val colorRes = when {
                    item.accuracy >= 80 -> android.R.color.holo_green_dark
                    item.accuracy >= 60 -> android.R.color.holo_orange_dark
                    else -> android.R.color.holo_red_dark
                }
                binding.tvAccuracy.setTextColor(binding.root.context.getColor(colorRes))
                binding.tvDuration.text = "用时：${item.durationText}"
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, StudyRecordsActivity::class.java))
        }
    }
}
