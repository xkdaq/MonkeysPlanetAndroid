package top.monkeysxu.planet.feature_exam

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
import kotlinx.coroutines.launch
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseFragment
import top.monkeysxu.planet.core.base.Refreshable
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.databinding.FragmentExamBinding
import top.monkeysxu.planet.feature_exam.adapter.BankAdapter
import top.monkeysxu.planet.feature_exam.api.ExamApiService

class ExamFragment : BaseFragment<FragmentExamBinding>(), Refreshable {

    private val tokenManager by lazy { TokenManager(requireContext()) }
    private val apiService by lazy {
        RetrofitClient.createMpRetrofit(tokenManager)
            .create(ExamApiService::class.java)
    }
    private val repository by lazy { ExamRepository(apiService) }
    private val viewModel: ExamViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ExamViewModel(repository) as T
            }
        }
    }

    private val bankAdapter by lazy { BankAdapter() }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentExamBinding {
        return FragmentExamBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<android.widget.TextView>(R.id.tvTitle).text = "题库"
        initView()
        observeViewModel()
        viewModel.loadBanks()
    }

    override fun onTabSelected() {
        viewModel.loadBanks()
    }

    private fun initView() {
        binding.rvBanks.layoutManager = LinearLayoutManager(context)
        binding.rvBanks.adapter = bankAdapter
        bankAdapter.onItemClick = { bank ->
            BankDetailActivity.start(
                requireContext(),
                bank.id,
                bank.name,
                bank.description
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.banks.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Resource.Success -> {
                            bankAdapter.setData(result.data)
                            if (result.data.isEmpty()) {
                                binding.emptyLayout.visibility = View.VISIBLE
                                binding.rvBanks.visibility = View.GONE
                            } else {
                                binding.emptyLayout.visibility = View.GONE
                                binding.rvBanks.visibility = View.VISIBLE
                            }
                        }
                        is Resource.Error -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
