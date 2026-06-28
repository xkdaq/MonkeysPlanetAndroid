package top.monkeysxu.planet.feature_profile.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.InputType
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.config.LegalUrls
import top.monkeysxu.planet.core.ext.loadSystemBar
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.databinding.ActivityRegisterBinding
import top.monkeysxu.planet.feature_common.webview.WebViewActivity
import top.monkeysxu.planet.feature_profile.api.UserApiService

class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, RegisterActivity::class.java)

        fun start(context: Context) {
            context.startActivity(createIntent(context))
        }
    }

    private val tokenManager by lazy { TokenManager(this) }
    private val repository by lazy {
        LoginRepository(
            RetrofitClient.createMpRetrofit(tokenManager).create(UserApiService::class.java),
            tokenManager
        )
    }
    private val viewModel: RegisterViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return RegisterViewModel(repository) as T
            }
        }
    }

    private var passwordVisible = false
    private var confirmPasswordVisible = false
    private var countdown: CountDownTimer? = null

    override fun inflateBinding() = ActivityRegisterBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.loadSystemBar(0)
        initView()
        observeViewModel()
    }

    private fun initView() {
        binding.btnBack.setOnClickListener { finish() }
        binding.tvLogin.setOnClickListener { finish() }
        binding.btnPasswordVisibility.setOnClickListener {
            passwordVisible = !passwordVisible
            val inputType = if (passwordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etPassword.inputType = inputType
            binding.etConfirmPassword.inputType = inputType
            binding.btnPasswordVisibility.setImageResource(
                if (passwordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off
            )
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
        }
        binding.btnConfirmPasswordVisibility.setOnClickListener {
            confirmPasswordVisible = !confirmPasswordVisible
            binding.etConfirmPassword.inputType = if (confirmPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.btnConfirmPasswordVisibility.setImageResource(
                if (confirmPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off
            )
            binding.etConfirmPassword.setSelection(
                binding.etConfirmPassword.text?.length ?: 0
            )
        }
        binding.btnSendCode.setOnClickListener {
            viewModel.sendCode(binding.etPhone.text.toString().trim())
        }
        setupLegalLinks()
        binding.btnRegister.setOnClickListener {
            viewModel.register(
                binding.etPhone.text.toString().trim(),
                binding.etCode.text.toString().trim(),
                binding.etPassword.text.toString(),
                binding.etConfirmPassword.text.toString()
            )
        }
    }

    private fun setupLegalLinks() {
        val content = "注册即表示你已阅读并同意用户协议与隐私政策"
        val spannable = SpannableString(content)
        addLegalLink(spannable, content, "用户协议") {
            WebViewActivity.start(this, "用户服务协议", LegalUrls.USER_AGREEMENT)
        }
        addLegalLink(spannable, content, "隐私政策") {
            WebViewActivity.start(this, "隐私政策", LegalUrls.PRIVACY_POLICY)
        }
        binding.tvLegalAgreement.apply {
            text = spannable
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = android.graphics.Color.TRANSPARENT
        }
    }

    private fun addLegalLink(
        spannable: SpannableString,
        content: String,
        label: String,
        onClick: () -> Unit
    ) {
        val start = content.indexOf(label)
        spannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) = onClick()

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = getColor(R.color.primary)
                    ds.isUnderlineText = false
                    ds.isFakeBoldText = true
                }
            },
            start,
            start + label.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sendCodeResult.observe(this@RegisterActivity) { result ->
                    when (result) {
                        is Resource.Loading -> binding.btnSendCode.isEnabled = false
                        is Resource.Success -> {
                            Toast.makeText(this@RegisterActivity, "验证码已发送", Toast.LENGTH_SHORT).show()
                            startCountdown()
                        }
                        is Resource.Error -> {
                            binding.btnSendCode.isEnabled = true
                            Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                viewModel.registerResult.observe(this@RegisterActivity) { result ->
                    when (result) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnRegister.isEnabled = false
                            binding.btnRegister.text = "注册中..."
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@RegisterActivity, "注册成功", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                            binding.btnRegister.text = "立即注册"
                            Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_SHORT).show()
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
}
