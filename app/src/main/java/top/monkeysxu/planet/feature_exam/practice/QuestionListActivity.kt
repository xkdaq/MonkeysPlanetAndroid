package top.monkeysxu.planet.feature_exam.practice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.databinding.ActivityQuestionListBinding
import top.monkeysxu.planet.feature_exam.api.ExamApiService
import top.monkeysxu.planet.feature_exam.model.Question

class QuestionListActivity : BaseActivity<ActivityQuestionListBinding>() {

    companion object {
        private const val EXTRA_MODE = "mode"  // "wrong" or "favorite"
        const val MODE_WRONG = "wrong"
        const val MODE_FAVORITE = "favorite"

        fun start(context: Context, mode: String) {
            context.startActivity(Intent(context, QuestionListActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode)
            })
        }
    }

    private val tokenManager by lazy { TokenManager(this) }
    private val apiService by lazy {
        RetrofitClient.createMpRetrofit(tokenManager)
            .create(ExamApiService::class.java)
    }

    private var mode = MODE_WRONG
    private var questions = listOf<Question>()
    private val adapter = QuestionPreviewAdapter()

    override fun inflateBinding(): ActivityQuestionListBinding {
        return ActivityQuestionListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_WRONG

        setupUI()
        loadQuestions()
    }

    override fun onResume() {
        super.onResume()
        // 返回时刷新列表(可能练习后错题/收藏有变化)
        loadQuestions()
    }

    private fun setupUI() {
        val title = if (mode == MODE_WRONG) "错题练习" else "我的收藏"
        binding.toolbar.tvTitle.text = title
        binding.toolbar.ivBack.setOnClickListener { finish() }

        binding.rvQuestions.layoutManager = LinearLayoutManager(this)
        binding.rvQuestions.adapter = adapter

        adapter.onItemClick = { position ->
            if (questions.isNotEmpty()) {
                PracticeActivity.startWithMode(this, mode, position)
            }
        }

        binding.btnStartPractice.setOnClickListener {
            if (questions.isEmpty()) {
                Toast.makeText(this, "暂无题目", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            PracticeActivity.startWithMode(this, mode, 0)
        }
    }

    private fun loadQuestions() {
        lifecycleScope.launch {
            try {
                val response = if (mode == MODE_WRONG) {
                    apiService.getWrongs()
                } else {
                    apiService.getFavorites()
                }

                if (response.isSuccess && response.data != null) {
                    questions = response.data
                    // 存储到静态holder供PracticeActivity使用
                    PracticeActivity.pendingQuestions = questions

                    if (questions.isEmpty()) {
                        showEmpty()
                    } else {
                        showList()
                    }
                } else {
                    showEmpty()
                }
            } catch (e: Exception) {
                showEmpty()
                Toast.makeText(this@QuestionListActivity, "加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showList() {
        binding.rvQuestions.visibility = View.VISIBLE
        binding.emptyLayout.visibility = View.GONE
        binding.bottomBar.visibility = View.VISIBLE

        val countText = if (mode == MODE_WRONG) {
            "共 ${questions.size} 道错题"
        } else {
            "共 ${questions.size} 道收藏"
        }
        binding.tvCount.text = countText
        adapter.setData(questions, mode)
    }

    private fun showEmpty() {
        binding.rvQuestions.visibility = View.GONE
        binding.emptyLayout.visibility = View.VISIBLE
        binding.bottomBar.visibility = View.GONE

        if (mode == MODE_WRONG) {
            binding.tvEmptyTitle.text = "暂无错题记录"
            binding.tvEmptySubtitle.text = "答错的题目会出现在这里"
        } else {
            binding.tvEmptyTitle.text = "暂无收藏题目"
            binding.tvEmptySubtitle.text = "点击题目收藏按钮添加收藏"
        }
        binding.tvCount.text = ""
    }

    // ============ Adapter ============

    class QuestionPreviewAdapter : RecyclerView.Adapter<QuestionPreviewAdapter.VH>() {

        private var items = listOf<Question>()
        private var mode = MODE_WRONG
        var onItemClick: ((Int) -> Unit)? = null

        fun setData(data: List<Question>, mode: String) {
            this.items = data
            this.mode = mode
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_question_preview, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val question = items[position]
            holder.bind(question, mode)
            holder.itemView.setOnClickListener { onItemClick?.invoke(position) }
        }

        override fun getItemCount() = items.size

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val tvContent: TextView = view.findViewById(R.id.tvQuestionContent)
            private val tvTypeTag: TextView = view.findViewById(R.id.tvTypeTag)
            private val tvExtraInfo: TextView = view.findViewById(R.id.tvExtraInfo)

            fun bind(question: Question, mode: String) {
                // 去除HTML标签显示纯文本预览
                val plainText = Html.fromHtml(question.content, Html.FROM_HTML_MODE_COMPACT).toString().trim()
                tvContent.text = plainText

                tvTypeTag.text = when (question.type) {
                    1 -> "单选题"
                    2 -> "多选题"
                    3 -> "判断题"
                    4 -> "填空题"
                    5 -> "问答题"
                    else -> "题目"
                }

                tvExtraInfo.text = if (mode == MODE_WRONG) {
                    "正确答案：${question.answer ?: ""}"
                } else {
                    "答案：${question.answer ?: ""}"
                }
            }
        }
    }
}
