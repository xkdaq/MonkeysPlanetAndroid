package top.monkeysxu.planet.feature_profile.changepassword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.ext.loadSystemBar
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.databinding.ActivityChangePasswordBinding
import top.monkeysxu.planet.feature_profile.ProfileRepository
import top.monkeysxu.planet.feature_profile.ProfileViewModel
import top.monkeysxu.planet.feature_profile.api.UserApiService
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.TokenManager

class ChangePasswordActivity : BaseActivity<ActivityChangePasswordBinding>() {

    private val tokenManager by lazy { TokenManager(this) }
    private val apiService by lazy {
        RetrofitClient.createMpRetrofit(tokenManager)
            .create(UserApiService::class.java)
    }
    private val repository by lazy { ProfileRepository(apiService, tokenManager) }
    private val viewModel: ProfileViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(repository) as T
            }
        }
    }

    override fun inflateBinding() = ActivityChangePasswordBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.loadSystemBar(0)
        initView()
        observeViewModel()
    }

    private fun initView() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnConfirm.setOnClickListener {
            val oldPassword = binding.etOldPassword.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            clearErrors()
            if (oldPassword.isEmpty()) {
                binding.layoutOldPassword.error = "请输入当前密码"
                binding.etOldPassword.requestFocus()
                return@setOnClickListener
            }
            if (newPassword.length !in 6..20) {
                binding.layoutNewPassword.error = "新密码长度应为6-20位"
                binding.etNewPassword.requestFocus()
                return@setOnClickListener
            }
            if (newPassword == oldPassword) {
                binding.layoutNewPassword.error = "新密码不能与当前密码相同"
                binding.etNewPassword.requestFocus()
                return@setOnClickListener
            }
            if (newPassword != confirmPassword) {
                binding.layoutConfirmPassword.error = "两次输入的密码不一致"
                binding.etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            viewModel.changePassword(oldPassword, newPassword)
        }
    }

    private fun clearErrors() {
        binding.layoutOldPassword.error = null
        binding.layoutNewPassword.error = null
        binding.layoutConfirmPassword.error = null
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.changePasswordResult.observe(this@ChangePasswordActivity) { result ->
                    when (result) {
                        is Resource.Loading -> {
                            binding.btnConfirm.isEnabled = false
                            binding.btnConfirm.text = "修改中..."
                        }
                        is Resource.Success -> {
                            binding.btnConfirm.isEnabled = true
                            binding.btnConfirm.text = "确认修改"
                            Toast.makeText(this@ChangePasswordActivity, "密码修改成功", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is Resource.Error -> {
                            binding.btnConfirm.isEnabled = true
                            binding.btnConfirm.text = "确认修改"
                            Toast.makeText(this@ChangePasswordActivity, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ChangePasswordActivity::class.java))
        }
    }
}
