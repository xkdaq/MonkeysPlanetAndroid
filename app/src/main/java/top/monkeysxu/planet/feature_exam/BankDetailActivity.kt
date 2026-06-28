package top.monkeysxu.planet.feature_exam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.databinding.ActivityBankDetailBinding
import top.monkeysxu.planet.feature_exam.adapter.CategoryAdapter
import top.monkeysxu.planet.feature_exam.api.ExamApiService
import top.monkeysxu.planet.feature_exam.model.Category
import top.monkeysxu.planet.feature_exam.practice.PracticeActivity
import top.monkeysxu.planet.feature_exam.practice.QuestionListActivity

class BankDetailActivity : BaseActivity<ActivityBankDetailBinding>() {

    companion object {
        private const val EXTRA_BANK_ID = "bank_id"
        private const val EXTRA_BANK_NAME = "bank_name"
        private const val EXTRA_BANK_DESC = "bank_desc"

        fun start(context: Context, bankId: Int, bankName: String, bankDesc: String?) {
            context.startActivity(Intent(context, BankDetailActivity::class.java).apply {
                putExtra(EXTRA_BANK_ID, bankId)
                putExtra(EXTRA_BANK_NAME, bankName)
                putExtra(EXTRA_BANK_DESC, bankDesc)
            })
        }
    }

    private val tokenManager by lazy { TokenManager(this) }
    private val apiService by lazy {
        RetrofitClient.createMpRetrofit(tokenManager)
            .create(ExamApiService::class.java)
    }

    private val categoryAdapter by lazy { CategoryAdapter() }
    private var bankId: Int = 0
    private var allCategoryTree: List<Category> = emptyList()
    private var selectedCategoryId: Int? = null

    override fun inflateBinding(): ActivityBankDetailBinding {
        return ActivityBankDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bankId = intent.getIntExtra(EXTRA_BANK_ID, 0)
        val bankName = intent.getStringExtra(EXTRA_BANK_NAME) ?: "题库详情"
        val bankDesc = intent.getStringExtra(EXTRA_BANK_DESC)

        binding.toolbar.ivBack.setOnClickListener { finish() }
        binding.toolbar.tvTitle.text = bankName

        if (!bankDesc.isNullOrEmpty()) {
            binding.tvBankDesc.text = bankDesc
            binding.tvBankDesc.visibility = View.VISIBLE
        }

        initView()
        loadCategories()
    }

    private fun initView() {
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = categoryAdapter
        categoryAdapter.onItemClick = { category ->
            selectedCategoryId = category.id
            startPractice(3) // 专项练习，加载该分类全部题目
        }

        binding.btnOrderPractice.setOnClickListener { startPractice(1) }
        binding.btnRandomPractice.setOnClickListener { startPractice(2) }
        binding.btnWrongPractice.setOnClickListener {
            QuestionListActivity.start(this, QuestionListActivity.MODE_WRONG)
        }
        binding.btnFavoritePractice.setOnClickListener {
            QuestionListActivity.start(this, QuestionListActivity.MODE_FAVORITE)
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val response = apiService.getCategoryTree(bankId)
                if (response.isSuccess && response.data != null) {
                    val tree = buildCategoryTree(response.data)
                    allCategoryTree = tree
                    processAndDisplay(tree)
                } else {
                    showEmptyCategories()
                }
            } catch (e: Exception) {
                showEmptyCategories()
            }
        }
    }

    private fun processAndDisplay(tree: List<Category>) {
        // 判断是否有三级结构（科目-章-节）
        val hasSubjectLevel = tree.any { cat ->
            cat.children?.any { child -> !child.children.isNullOrEmpty() } == true
        }

        if (hasSubjectLevel) {
            // 有科目层，显示科目Tab
            setupSubjectTabs(tree)
            val firstSubject = tree.firstOrNull()
            categoryAdapter.setData(firstSubject?.children ?: emptyList())
        } else {
            // 无科目层，直接显示分类
            binding.subjectTabScroll.visibility = View.GONE
            categoryAdapter.setData(tree)
        }

        if (categoryAdapter.itemCount == 0) {
            showEmptyCategories()
        } else {
            binding.emptyCategoryLayout.visibility = View.GONE
            binding.rvCategories.visibility = View.VISIBLE
        }
    }

    private fun setupSubjectTabs(subjects: List<Category>) {
        binding.subjectTabScroll.visibility = View.VISIBLE
        binding.subjectChipGroup.removeAllViews()

        subjects.forEachIndexed { index, subject ->
            val chip = Chip(this).apply {
                text = subject.name
                isCheckable = true
                isChecked = (index == 0)
                setChipBackgroundColorResource(R.color.bg_gray)
                setTextColor(resources.getColor(R.color.text_secondary, null))
                chipStrokeWidth = 0f
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        setChipBackgroundColorResource(R.color.primary_light)
                        setTextColor(resources.getColor(R.color.primary, null))
                        categoryAdapter.setData(subject.children ?: emptyList())
                    } else {
                        setChipBackgroundColorResource(R.color.bg_gray)
                        setTextColor(resources.getColor(R.color.text_secondary, null))
                    }
                }
            }
            if (index == 0) {
                chip.setChipBackgroundColorResource(R.color.primary_light)
                chip.setTextColor(resources.getColor(R.color.primary, null))
            }
            binding.subjectChipGroup.addView(chip)
        }
    }

    private fun showEmptyCategories() {
        binding.rvCategories.visibility = View.GONE
        binding.emptyCategoryLayout.visibility = View.VISIBLE
    }

    private fun buildCategoryTree(list: List<Category>): List<Category> {
        // 将所有节点收集到map中（递归展开嵌套的children）
        val map = mutableMapOf<Int, Category>()
        fun collect(items: List<Category>) {
            items.forEach { item ->
                map[item.id] = item.copy(children = null)
                item.children?.let { collect(it) }
            }
        }
        collect(list)

        // 用parentId重建树形结构
        val childrenMap = mutableMapOf<Int, MutableList<Category>>()
        val roots = mutableListOf<Category>()

        map.values.forEach { item ->
            val pid = item.parentId
            if (pid != null && pid > 0 && map.containsKey(pid)) {
                childrenMap.getOrPut(pid) { mutableListOf() }.add(item)
            } else {
                roots.add(item)
            }
        }

        // 排序并递归挂载子节点
        fun attachChildren(node: Category): Category {
            val children = childrenMap[node.id]
                ?.sortedWith(compareBy({ it.sort ?: 0 }, { it.id }))
                ?.map { attachChildren(it) }
            return node.copy(children = children)
        }

        return roots
            .sortedWith(compareBy({ it.sort ?: 0 }, { it.id }))
            .map { attachChildren(it) }
    }

    private fun startPractice(type: Int) {
        // 顺序练习(1)和随机练习(2)从整个题库抽题，专项练习(3)从选中分类抽题
        val effectiveCategoryId = if (type == 3) selectedCategoryId else null
        PracticeActivity.start(this, bankId, effectiveCategoryId, type)
    }
}
