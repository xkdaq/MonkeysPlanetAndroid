package top.monkeysxu.planet.feature_material

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseFragment
import top.monkeysxu.planet.core.base.Refreshable
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.ViewLimitStore
import top.monkeysxu.planet.databinding.FragmentMaterialBinding
import top.monkeysxu.planet.feature_common.material.MaterialDetailActivity
import top.monkeysxu.planet.feature_common.search.SearchActivity
import top.monkeysxu.planet.feature_material.adapter.MaterialAdapter
import top.monkeysxu.planet.feature_material.api.MaterialApiService
import top.monkeysxu.planet.feature_material.model.MaterialCategory
import top.monkeysxu.planet.feature_material.model.MaterialSubject

class MaterialFragment : BaseFragment<FragmentMaterialBinding>(), Refreshable {

    private val apiService by lazy {
        RetrofitClient.createMaterialRetrofit()
            .create(MaterialApiService::class.java)
    }
    private val repository by lazy { MaterialRepository(apiService) }
    private val viewModel: MaterialViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MaterialViewModel(repository) as T
            }
        }
    }

    private val materialAdapter by lazy { MaterialAdapter() }
    private var selectedSubjectId: Int? = null
    private var selectedCategoryId: Int? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMaterialBinding {
        return FragmentMaterialBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<android.widget.TextView>(R.id.tvTitle).text = "资料"
        initView()
        observeViewModel()
        viewModel.loadSubjects()
        viewModel.loadCategories()
        viewModel.loadMaterials()
    }

    override fun onTabSelected() {
        viewModel.loadSubjects()
        viewModel.loadCategories(selectedSubjectId)
        viewModel.loadMaterials(refresh = true)
    }

    private fun initView() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadSubjects()
            viewModel.loadCategories(selectedSubjectId)
            viewModel.loadMaterials(selectedSubjectId, selectedCategoryId, refresh = true)
        }

        binding.searchBox.setOnClickListener {
            SearchActivity.start(requireContext(), SearchActivity.FROM_MATERIAL)
        }

        val viewLimitStore = ViewLimitStore(requireContext())
        binding.rvMaterials.layoutManager = LinearLayoutManager(context)
        binding.rvMaterials.adapter = materialAdapter
        materialAdapter.onItemClick = { material ->
            MaterialDetailActivity.start(requireContext(), material.id)
        }

        // 滚动加载更多
        binding.rvMaterials.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                if (!rv.canScrollVertically(1)) {
                    viewModel.loadMore()
                }
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subjects.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Resource.Success -> {
                            setupSubjectChips(result.data)
                        }
                        else -> {}
                    }
                }
                viewModel.categories.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Resource.Success -> {
                            setupCategoryChips(result.data)
                        }
                        else -> {}
                    }
                }
                viewModel.materials.observe(viewLifecycleOwner) { result ->
                    binding.swipeRefresh.isRefreshing = false
                    when (result) {
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            materialAdapter.setData(result.data)
                            if (result.data.isEmpty()) {
                                binding.emptyLayout.visibility = View.VISIBLE
                                binding.rvMaterials.visibility = View.GONE
                            } else {
                                binding.emptyLayout.visibility = View.GONE
                                binding.rvMaterials.visibility = View.VISIBLE
                            }
                        }
                        is Resource.Error -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupSubjectChips(subjects: List<MaterialSubject>) {
        binding.subjectChipGroup.removeAllViews()

        val allChip = createSubjectChip("全部", null, selectedSubjectId == null)
        binding.subjectChipGroup.addView(allChip)

        subjects.forEach { subject ->
            val chip = createSubjectChip(subject.name, subject.id, selectedSubjectId == subject.id)
            binding.subjectChipGroup.addView(chip)
        }
    }

    private fun setupCategoryChips(categories: List<MaterialCategory>) {
        binding.categoryChipGroup.removeAllViews()
        binding.categoryScroll.visibility = if (categories.isEmpty()) View.GONE else View.VISIBLE

        val allChip = createCategoryChip("全部分类", null, selectedCategoryId == null)
        binding.categoryChipGroup.addView(allChip)

        categories.forEach { category ->
            val chip = createCategoryChip(category.name, category.id, selectedCategoryId == category.id)
            binding.categoryChipGroup.addView(chip)
        }
    }

    private fun createSubjectChip(text: String, subjectId: Int?, checked: Boolean): Chip {
        return createFilterChip(text, checked) {
            selectedSubjectId = subjectId
            selectedCategoryId = null
            viewModel.loadCategories(subjectId)
            viewModel.loadMaterials(subjectId, null, refresh = true)
        }
    }

    private fun createCategoryChip(text: String, categoryId: Int?, checked: Boolean): Chip {
        return createFilterChip(text, checked) {
            selectedCategoryId = categoryId
            viewModel.loadMaterials(selectedSubjectId, categoryId, refresh = true)
        }
    }

    private fun createFilterChip(text: String, checked: Boolean, onChecked: () -> Unit): Chip {
        return Chip(requireContext()).apply {
            this.text = text
            isCheckable = true
            chipStrokeWidth = 0f
            isChecked = checked
            if (checked) {
                setChipBackgroundColorResource(R.color.primary_light)
                setTextColor(resources.getColor(R.color.primary, null))
            } else {
                setChipBackgroundColorResource(R.color.bg_gray)
                setTextColor(resources.getColor(R.color.text_secondary, null))
            }
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    setChipBackgroundColorResource(R.color.primary_light)
                    setTextColor(resources.getColor(R.color.primary, null))
                    onChecked()
                } else {
                    setChipBackgroundColorResource(R.color.bg_gray)
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                }
            }
        }
    }
}
