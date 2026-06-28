package top.monkeysxu.planet.feature_profile.bindphone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.ext.loadSystemBar
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.databinding.ActivityBindPhoneBinding
import top.monkeysxu.planet.feature_profile.api.UserApiService

class BindPhoneActivity : BaseActivity<ActivityBindPhoneBinding>() {

    private val tokenManager by lazy { TokenManager(this) }
    private val api by lazy {
        RetrofitClient.createMpRetrofit(tokenManager).create(UserApiService::class.java)
    }
    private val viewModel: BindPhoneViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                BindPhoneViewModel(api, tokenManager) as T
        }
    }
    private var countdown: CountDownTimer? = null
    private var passwordVisible = false
    private var confirmVisible = false

    override fun inflateBinding() = ActivityBindPhoneBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.loadSystemBar(0)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSendCode.setOnClickListener {
            viewModel.sendCode(binding.etPhone.text.toString().trim())
        }
        binding.btnBind.setOnClickListener {
            viewModel.bind(
                binding.etPhone.text.toString().trim(),
                binding.etCode.text.toString().trim(),
                binding.etPassword.text.toString(),
                binding.etConfirmPassword.text.toString()
            )
        }
        binding.btnPasswordVisibility.setOnClickListener {
            passwordVisible = togglePassword(
                binding.etPassword,
                binding.btnPasswordVisibility,
                passwordVisible
            )
        }
        binding.btnConfirmPasswordVisibility.setOnClickListener {
            confirmVisible = togglePassword(
                binding.etConfirmPassword,
                binding.btnConfirmPasswordVisibility,
                confirmVisible
            )
        }
        observe()
        viewModel.loadUserInfo()
    }

    private fun togglePassword(
        editText: android.widget.EditText,
        button: android.widget.ImageButton,
        visible: Boolean
    ): Boolean {
        val next = !visible
        editText.inputType = if (next) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        button.setImageResource(if (next) R.drawable.ic_eye else R.drawable.ic_eye_off)
        editText.setSelection(editText.text?.length ?: 0)
        return next
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userInfo.observe(this@BindPhoneActivity) { result ->
                    if (result is Resource.Success) {
                        val bound = result.data.hasPhone == true || !result.data.phone.isNullOrBlank()
                        binding.layoutBound.visibility = if (bound) View.VISIBLE else View.GONE
                        binding.layoutForm.visibility = if (bound) View.GONE else View.VISIBLE
                        binding.tvBoundPhone.text = result.data.phone.orEmpty()
                    }
                }
                viewModel.sendCodeResult.observe(this@BindPhoneActivity) { result ->
                    when (result) {
                        is Resource.Loading -> binding.btnSendCode.isEnabled = false
                        is Resource.Success -> {
                            Toast.makeText(this@BindPhoneActivity, "验证码已发送", Toast.LENGTH_SHORT).show()
                            startCountdown()
                        }
                        is Resource.Error -> {
                            binding.btnSendCode.isEnabled = true
                            Toast.makeText(this@BindPhoneActivity, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                viewModel.bindResult.observe(this@BindPhoneActivity) { result ->
                    when (result) {
                        is Resource.Loading -> {
                            binding.btnBind.isEnabled = false
                            binding.btnBind.text = "绑定中..."
                        }
                        is Resource.Success -> {
                            Toast.makeText(this@BindPhoneActivity, "绑定成功", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is Resource.Error -> {
                            binding.btnBind.isEnabled = true
                            binding.btnBind.text = "绑定手机号"
                            Toast.makeText(this@BindPhoneActivity, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun startCountdown() {
        countdown?.cancel()
        countdown = object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.btnSendCode.text = "${millisUntilFinished / 1_000}s"
            }
            override fun onFinish() {
                binding.btnSendCode.isEnabled = true
                binding.btnSendCode.text = "获取验证码"
            }
        }.start()
    }

    override fun onDestroy() {
        countdown?.cancel()
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) =
            context.startActivity(Intent(context, BindPhoneActivity::class.java))
    }
}
