package top.monkeysxu.planet.feature_exam.practice

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.databinding.ActivityPracticeBinding
import top.monkeysxu.planet.feature_exam.ExamRepository
import top.monkeysxu.planet.feature_exam.api.ExamApiService
import top.monkeysxu.planet.feature_exam.api.RecordRequest
import top.monkeysxu.planet.feature_exam.model.Question

class PracticeActivity : BaseActivity<ActivityPracticeBinding>() {

    companion object {
        private const val TAG = "PracticeActivity"
        private const val EXTRA_BANK_ID = "bank_id"
        private const val EXTRA_CATEGORY_ID = "category_id"
        private const val EXTRA_TYPE = "type"
        private const val EXTRA_MODE = "mode"  // "wrong" or "favorite"
        private const val EXTRA_START_INDEX = "start_index"

        // 静态题目持有器，用于错题/收藏模式传递题目列表
        var pendingQuestions: List<Question>? = null

        fun start(context: Context, bankId: Int, categoryId: Int?, type: Int) {
            context.startActivity(Intent(context, PracticeActivity::class.java).apply {
                putExtra(EXTRA_BANK_ID, bankId)
                putExtra(EXTRA_CATEGORY_ID, categoryId ?: 0)
                putExtra(EXTRA_TYPE, type)
            })
        }

        fun startWithMode(context: Context, mode: String, startIndex: Int = 0) {
            context.startActivity(Intent(context, PracticeActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode)
                putExtra(EXTRA_START_INDEX, startIndex)
            })
        }
    }

    private val tokenManager by lazy { TokenManager(this) }
    private val apiService by lazy {
        RetrofitClient.createMpRetrofit(tokenManager)
            .create(ExamApiService::class.java)
    }
    private val repository by lazy { ExamRepository(apiService) }

    // 题目数据
    private var questions = listOf<Question>()
    private var currentIndex = 0

    // 答题状态
    private var userAnswer = ""        // 当前用户选择的答案
    private var isAnswered = false     // 当前题是否已提交
    private var isCorrect = false      // 当前题是否正确

    // 模式: "answer" = 答题, "study" = 背题
    private var practiceMode = "answer"

    // 答题记录
    data class AnswerRecord(val questionId: Long, val userAnswer: String, val isCorrect: Boolean)
    private val answerRecords = mutableListOf<AnswerRecord>()
    private var correctCount = 0
    private var wrongCount = 0

    // 收藏状态
    private var isFavorite = false

    // 计时
    private var startTime = System.currentTimeMillis()
    private var bankId = 0
    private var categoryId: Int? = null

    override fun inflateBinding(): ActivityPracticeBinding {
        return ActivityPracticeBinding.inflate(layoutInflater)
    }

    // 模式: null=普通, "wrong"=错题, "favorite"=收藏
    private var mode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bankId = intent.getIntExtra(EXTRA_BANK_ID, 0)
        categoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, 0).takeIf { it > 0 }
        val type = intent.getIntExtra(EXTRA_TYPE, 1)
        mode = intent.getStringExtra(EXTRA_MODE)
        startTime = System.currentTimeMillis()

        setupListeners()

        if (mode != null) {
            // 错题/收藏模式：从静态holder获取题目
            loadQuestionsFromHolder()
        } else {
            loadQuestions(bankId, categoryId, type)
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }
        binding.btnPrev.setOnClickListener { onPrevTap() }
        binding.btnNext.setOnClickListener { onNextTap() }
        binding.btnSubmitAnswer.setOnClickListener { onSubmitMultiAnswer() }
        binding.btnFavorite.setOnClickListener { onFavoriteTap() }
        binding.btnCard.setOnClickListener { onQuestionNav() }

        // 模式切换
        binding.btnModeAnswer.setOnClickListener { onModeChange("answer") }
        binding.btnModeStudy.setOnClickListener { onModeChange("study") }
    }

    // ===================== 数据加载 =====================

    private fun showLoading() {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.loadingLayout.visibility = View.GONE
        binding.contentLayout.visibility = View.VISIBLE
    }

    private fun loadQuestionsFromHolder() {
        val pending = pendingQuestions
        if (pending.isNullOrEmpty()) {
            showLoading()
            loadQuestionsFromApi()
            return
        }
        questions = pending
        pendingQuestions = null
        Log.d(TAG, "从holder加载题目: ${questions.size} 题 (mode=$mode)")
        val startIndex = intent.getIntExtra(EXTRA_START_INDEX, 0).coerceIn(0, questions.size - 1)
        hideLoading()
        loadQuestion(startIndex)
    }

    private fun loadQuestionsFromApi() {
        lifecycleScope.launch {
            try {
                val response = if (mode == "wrong") {
                    apiService.getWrongs()
                } else {
                    apiService.getFavorites()
                }
                if (response.isSuccess && !response.data.isNullOrEmpty()) {
                    questions = response.data
                    val startIndex = intent.getIntExtra(EXTRA_START_INDEX, 0).coerceIn(0, questions.size - 1)
                    hideLoading()
                    loadQuestion(startIndex)
                } else {
                    Toast.makeText(this@PracticeActivity, "暂无题目", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PracticeActivity, "加载失败", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadQuestions(bankId: Int, categoryId: Int?, type: Int) {
        showLoading()
        lifecycleScope.launch {
            val result = repository.startPractice(bankId, categoryId, type)
            when (result) {
                is Resource.Success -> {
                    questions = result.data
                    Log.d(TAG, "加载题目成功: ${questions.size} 题")
                    if (questions.isNotEmpty()) {
                        hideLoading()
                        loadQuestion(0)
                    } else {
                        Toast.makeText(this@PracticeActivity, "暂无题目", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "加载题目失败: ${result.message}")
                    Toast.makeText(this@PracticeActivity, result.message, Toast.LENGTH_SHORT).show()
                    finish()
                }
                else -> {}
            }
        }
    }

    // ===================== 题目展示 =====================

    private fun loadQuestion(index: Int) {
        if (index < 0 || index >= questions.size) return
        currentIndex = index
        val question = questions[index]

        // 检查是否已有答题记录
        val existingRecord = answerRecords.find { it.questionId == question.id }
        isAnswered = existingRecord != null
        isCorrect = existingRecord?.isCorrect ?: false
        userAnswer = existingRecord?.userAnswer ?: ""

        // 背题模式下始终显示为已答
        val showAnalysis = practiceMode == "study" || isAnswered

        // 更新进度
        updateProgress(index)

        // 题目类型标签
        binding.tvQuestionType.text = when (question.type) {
            1 -> "单选题"
            2 -> "多选题"
            3 -> "判断题"
            4 -> "填空题"
            5 -> "问答题"
            else -> "题目"
        }

        // 渲染题目内容(WebView)
        val html = """
            <html><head><meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>body{font-size:15px;line-height:1.8;color:#333;margin:0;padding:0;word-wrap:break-word;} img{max-width:100%;height:auto;}</style>
            </head><body>${question.content}</body></html>
        """.trimIndent()
        binding.webViewQuestion.settings.javaScriptEnabled = false
        binding.webViewQuestion.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)

        // 渲染选项
        renderOptions(question, showAnalysis)

        // 多选题提交按钮
        if (question.type == 2 && !isAnswered && practiceMode != "study") {
            binding.btnSubmitAnswer.visibility = View.VISIBLE
        } else {
            binding.btnSubmitAnswer.visibility = View.GONE
        }

        // 解析区域
        if (showAnalysis) {
            showAnalysisSection(question)
        } else {
            binding.analysisLayout.visibility = View.GONE
        }

        // 底部按钮状态
        binding.btnPrev.alpha = if (currentIndex > 0) 1f else 0.4f
        binding.btnNext.text = if (currentIndex >= questions.size - 1) "完成" else "下一题"

        // 收藏状态
        checkFavorite(question.id)
    }

    private fun updateProgress(index: Int) {
        binding.tvCurrentIndex.text = "${index + 1}"
        binding.tvTotalCount.text = "/${questions.size}"

        // 更新进度条
        val progress = ((index + 1).toFloat() / questions.size.toFloat())
        binding.progressBarFill.post {
            val totalWidth = binding.progressBarBg.width
            val params = binding.progressBarFill.layoutParams
            params.width = (totalWidth * progress).toInt()
            binding.progressBarFill.layoutParams = params
        }
    }

    // ===================== 选项渲染 =====================

    private fun renderOptions(question: Question, showResult: Boolean) {
        binding.optionsContainer.removeAllViews()

        val opts = getOptions(question)
        if (opts.isEmpty() && question.type == 3) {
            // 判断题默认选项
            renderOptionItem("A", "正确", question, showResult)
            renderOptionItem("B", "错误", question, showResult)
        } else {
            opts.forEach { (label, content) ->
                renderOptionItem(label, content, question, showResult)
            }
        }
    }

    private fun renderOptionItem(label: String, content: String, question: Question, showResult: Boolean) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_option, binding.optionsContainer, false)
        val root = itemView.findViewById<LinearLayout>(R.id.optionRoot)
        val tvLabel = itemView.findViewById<TextView>(R.id.tvOptionLabel)
        val tvContent = itemView.findViewById<TextView>(R.id.tvOptionContent)
        val tvStatus = itemView.findViewById<TextView>(R.id.tvOptionStatus)

        tvLabel.text = label
        tvContent.text = content

        if (showResult) {
            // 显示结果状态
            applyOptionResultStyle(root, tvLabel, tvStatus, label, question)
        } else {
            // 未提交状态 - 检查是否选中
            if (userAnswer.contains(label)) {
                root.setBackgroundResource(R.drawable.bg_practice_option_selected)
            }
        }

        // 点击事件
        root.setOnClickListener {
            if (!isAnswered && practiceMode != "study") {
                onOptionSelect(label, question)
            }
        }

        binding.optionsContainer.addView(itemView)
    }

    private fun applyOptionResultStyle(
        root: LinearLayout, tvLabel: TextView, tvStatus: TextView,
        label: String, question: Question
    ) {
        val correctAnswer = question.answer?.trim()?.uppercase() ?: ""
        val isCorrectOption = correctAnswer.contains(label)
        val isUserSelected = userAnswer.contains(label)

        when {
            // 用户选了但不是正确答案 - 红色(错误)
            isUserSelected && !isCorrectOption -> {
                root.setBackgroundResource(R.drawable.bg_practice_option_wrong)
                tvLabel.setBackgroundResource(R.drawable.bg_practice_option_label_wrong)
                tvLabel.setTextColor(Color.WHITE)
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = "✗"
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.wrong_red))
            }
            // 多选题中漏选(正确答案有但用户没选) - 黄色(漏选)
            question.type == 2 && !isCorrect && isCorrectOption && !isUserSelected -> {
                root.setBackgroundResource(R.drawable.bg_practice_option_missed)
                tvLabel.setBackgroundResource(R.drawable.bg_practice_option_label_missed)
                tvLabel.setTextColor(Color.WHITE)
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = "!"
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.warning_orange))
            }
            // 正确答案 - 绿色
            isCorrectOption -> {
                root.setBackgroundResource(R.drawable.bg_practice_option_correct)
                tvLabel.setBackgroundResource(R.drawable.bg_practice_option_label_correct)
                tvLabel.setTextColor(Color.WHITE)
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = "✓"
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.correct_green))
            }
        }
    }

    // ===================== 选项选择 =====================

    private fun onOptionSelect(label: String, question: Question) {
        if (question.type == 2) {
            // 多选题: 切换选中状态
            val answers = userAnswer.toMutableList()
            if (answers.contains(label[0])) {
                answers.remove(label[0])
            } else {
                answers.add(label[0])
            }
            answers.sort()
            userAnswer = String(answers.toCharArray())

            // 刷新选项样式(选中状态)
            refreshOptionsSelectedState()
        } else {
            // 单选题/判断题: 直接选中并提交
            userAnswer = label
            submitAnswer(question)
        }
    }

    private fun refreshOptionsSelectedState() {
        for (i in 0 until binding.optionsContainer.childCount) {
            val itemView = binding.optionsContainer.getChildAt(i)
            val root = itemView.findViewById<LinearLayout>(R.id.optionRoot)
            val tvLabel = itemView.findViewById<TextView>(R.id.tvOptionLabel)
            val label = tvLabel.text.toString()

            if (userAnswer.contains(label)) {
                root.setBackgroundResource(R.drawable.bg_practice_option_selected)
            } else {
                root.setBackgroundResource(R.drawable.bg_practice_option_default)
            }
        }
    }

    // ===================== 提交答案 =====================

    private fun onSubmitMultiAnswer() {
        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "请选择答案", Toast.LENGTH_SHORT).show()
            return
        }
        val question = questions[currentIndex]
        submitAnswer(question)
    }

    private fun submitAnswer(question: Question) {
        if (isAnswered) return

        val correctAnswer = question.answer?.trim()?.uppercase() ?: ""
        val answer = userAnswer.trim().uppercase()

        // 判断正确性
        isCorrect = if (question.type == 2) {
            // 多选题排序后比较
            val userChars = answer.toList().sorted()
            val correctChars = correctAnswer.toList().sorted()
            userChars == correctChars
        } else {
            answer == correctAnswer
        }

        isAnswered = true

        // 记录
        answerRecords.add(AnswerRecord(question.id, userAnswer, isCorrect))
        if (isCorrect) correctCount++ else wrongCount++

        // 隐藏多选提交按钮
        binding.btnSubmitAnswer.visibility = View.GONE

        // 刷新选项为结果状态
        renderOptions(question, true)

        // 显示解析
        showAnalysisSection(question)
    }

    // ===================== 解析展示 =====================

    private fun showAnalysisSection(question: Question) {
        binding.analysisLayout.visibility = View.VISIBLE

        val correctAnswer = question.answer?.trim()?.uppercase() ?: "—"
        val displayUserAnswer = if (userAnswer.isNotEmpty()) userAnswer.uppercase() else "—"

        binding.tvCorrectAnswer.text = correctAnswer
        binding.tvCorrectAnswer.setTextColor(ContextCompat.getColor(this, R.color.correct_green))

        binding.tvUserAnswer.text = displayUserAnswer
        val userAnswerColor = if (isCorrect) R.color.correct_green else R.color.wrong_red
        binding.tvUserAnswer.setTextColor(ContextCompat.getColor(this, userAnswerColor))

        // 解析内容
        val analysis = question.analysis
        if (!analysis.isNullOrBlank()) {
            binding.analysisContentLayout.visibility = View.VISIBLE
            val analysisHtml = """
                <html><head><meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>body{font-size:14px;line-height:1.7;color:#666;margin:0;padding:0;word-wrap:break-word;} img{max-width:100%;height:auto;}</style>
                </head><body>$analysis</body></html>
            """.trimIndent()
            binding.webViewAnalysis.settings.javaScriptEnabled = false
            binding.webViewAnalysis.loadDataWithBaseURL(null, analysisHtml, "text/html", "utf-8", null)
        } else {
            binding.analysisContentLayout.visibility = View.GONE
        }
    }

    // ===================== 模式切换 =====================

    private fun onModeChange(mode: String) {
        if (practiceMode == mode) return
        practiceMode = mode

        if (mode == "answer") {
            binding.btnModeAnswer.setBackgroundResource(R.drawable.bg_mode_active)
            binding.btnModeAnswer.setTextColor(ContextCompat.getColor(this, R.color.primary))
            binding.btnModeStudy.background = null
            binding.btnModeStudy.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
        } else {
            binding.btnModeStudy.setBackgroundResource(R.drawable.bg_mode_active)
            binding.btnModeStudy.setTextColor(ContextCompat.getColor(this, R.color.primary))
            binding.btnModeAnswer.background = null
            binding.btnModeAnswer.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
        }

        // 背题模式直接显示答案
        if (mode == "study") {
            isAnswered = true
            val question = questions[currentIndex]
            renderOptions(question, true)
            showAnalysisSection(question)
            binding.btnSubmitAnswer.visibility = View.GONE
        } else {
            // 答题模式重新加载当前题
            loadQuestion(currentIndex)
        }
    }

    // ===================== 导航 =====================

    private fun onPrevTap() {
        if (currentIndex > 0) {
            loadQuestion(currentIndex - 1)
        }
    }

    private fun onNextTap() {
        if (currentIndex < questions.size - 1) {
            loadQuestion(currentIndex + 1)
        } else {
            showCompleteDialog()
        }
    }

    // ===================== 答题卡 =====================

    private fun onQuestionNav() {
        val answered = answerRecords.size
        val msg = "已答 $answered / ${questions.size} 题\n正确 $correctCount 题，错误 $wrongCount 题"
        AlertDialog.Builder(this)
            .setTitle("答题卡")
            .setMessage(msg)
            .setPositiveButton("确定", null)
            .show()
    }

    // ===================== 收藏 =====================

    private fun onFavoriteTap() {
        val question = questions.getOrNull(currentIndex) ?: return
        lifecycleScope.launch {
            try {
                val response = apiService.toggleFavorite(mapOf("questionId" to question.id))
                if (response.isSuccess && response.data != null) {
                    isFavorite = response.data
                    updateFavoriteUI()
                    val tip = if (isFavorite) "收藏成功" else "取消收藏"
                    Toast.makeText(this@PracticeActivity, tip, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "切换收藏失败", e)
            }
        }
    }

    private fun checkFavorite(questionId: Long) {
        lifecycleScope.launch {
            try {
                val response = apiService.checkFavorite(questionId)
                if (response.isSuccess && response.data != null) {
                    isFavorite = response.data
                    updateFavoriteUI()
                }
            } catch (e: Exception) {
                Log.e(TAG, "检查收藏失败", e)
            }
        }
    }

    private fun updateFavoriteUI() {
        binding.tvFavoriteIcon.text = if (isFavorite) "★" else "☆"
        binding.tvFavoriteText.text = if (isFavorite) "已收藏" else "收藏"
        val color = if (isFavorite) R.color.warning_orange else R.color.text_hint
        binding.tvFavoriteIcon.setTextColor(ContextCompat.getColor(this, color))
    }

    // ===================== 完成 =====================

    private fun showCompleteDialog() {
        val accuracy = if (questions.isNotEmpty()) (correctCount * 100 / questions.size) else 0
        val msg = "共${questions.size}题，答对${correctCount}题，正确率${accuracy}%"
        AlertDialog.Builder(this)
            .setTitle("练习完成")
            .setMessage(msg)
            .setPositiveButton("确定") { _, _ ->
                // 错题/收藏模式不保存记录
                if (mode == null) savePracticeRecord()
                finish()
            }
            .setNegativeButton("继续查看", null)
            .show()
    }

    private fun savePracticeRecord() {
        val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        val request = RecordRequest(
            bankId = bankId,
            categoryId = categoryId,
            correctCount = correctCount,
            wrongCount = wrongCount,
            totalCount = questions.size,
            duration = duration
        )
        lifecycleScope.launch {
            try { repository.saveRecord(request) } catch (_: Exception) {}
        }
    }

    // ===================== 工具方法 =====================

    /** 解析选项：优先用 optionsMap，否则解析 options JSON */
    private fun getOptions(question: Question): List<Pair<String, String>> {
        question.optionsMap?.let { map ->
            if (map.isNotEmpty()) return map.entries.sortedBy { it.key }.map { it.key to it.value }
        }
        return try {
            val raw = question.options ?: return emptyList()
            @Suppress("UNCHECKED_CAST")
            val map = com.google.gson.Gson().fromJson(raw, Map::class.java) as? Map<String, String>
            map?.entries?.sortedBy { it.key }?.map { it.key to it.value } ?: emptyList()
        } catch (e: Exception) {
            // 尝试解析 "A.xxx|B.xxx" 格式
            try {
                val raw = question.options ?: return emptyList()
                raw.split("|").mapNotNull { item ->
                    val match = Regex("^([A-E])\\.\\s*(.+)$").find(item.trim())
                    match?.let { it.groupValues[1] to it.groupValues[2] }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
