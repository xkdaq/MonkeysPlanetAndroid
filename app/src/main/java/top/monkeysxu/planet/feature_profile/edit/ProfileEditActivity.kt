package top.monkeysxu.planet.feature_profile.edit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import kotlinx.coroutines.launch
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.ext.loadSystemBar
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.model.UserInfo
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.databinding.ActivityProfileEditBinding
import top.monkeysxu.planet.feature_profile.api.UserApiService

class ProfileEditActivity : BaseActivity<ActivityProfileEditBinding>() {

    private val tokenManager by lazy { TokenManager(this) }
    private val api by lazy {
        RetrofitClient.createMpRetrofit(tokenManager).create(UserApiService::class.java)
    }
    private val viewModel: ProfileEditViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                ProfileEditViewModel(api, tokenManager) as T
        }
    }
    private var currentUser: UserInfo? = null
    private var avatarUrl: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(::uploadAvatar)
    }

    override fun inflateBinding() = ActivityProfileEditBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.loadSystemBar(0)
        binding.btnBack.setOnClickListener { finish() }
        binding.spinnerGender.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("未知", "男", "女")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.avatarContainer.setOnClickListener { imagePicker.launch("image/*") }
        binding.btnSave.setOnClickListener {
            currentUser?.let {
                viewModel.save(
                    it,
                    binding.etNickname.text.toString().trim(),
                    binding.spinnerGender.selectedItemPosition,
                    avatarUrl
                )
            }
        }
        observe()
        viewModel.load()
    }

    private fun uploadAvatar(uri: Uri) {
        val type = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = type.substringAfter("/", "jpg")
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null) {
            Toast.makeText(this, "无法读取图片", Toast.LENGTH_SHORT).show()
            return
        }
        if (bytes.size > 5 * 1024 * 1024) {
            Toast.makeText(this, "头像不能超过5MB", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.uploadAvatar(bytes, type, "avatar_${System.currentTimeMillis()}.$extension")
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userInfo.observe(this@ProfileEditActivity) { result ->
                    if (result is Resource.Success) {
                        currentUser = result.data
                        avatarUrl = result.data.avatarUrl
                        binding.etNickname.setText(result.data.nickname.orEmpty())
                        binding.tvUsername.text = result.data.username ?: "用户_${result.data.id}"
                        binding.spinnerGender.setSelection(result.data.gender ?: 0)
                        binding.ivAvatar.load(result.data.avatarUrl) {
                            placeholder(R.drawable.ic_avatar_default)
                            error(R.drawable.ic_avatar_default)
                        }
                    }
                }
                viewModel.avatarResult.observe(this@ProfileEditActivity) { result ->
                    when (result) {
                        is Resource.Loading -> binding.avatarProgress.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.avatarProgress.visibility = View.GONE
                            avatarUrl = result.data
                            binding.ivAvatar.load(result.data)
                        }
                        is Resource.Error -> {
                            binding.avatarProgress.visibility = View.GONE
                            Toast.makeText(this@ProfileEditActivity, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                viewModel.saveResult.observe(this@ProfileEditActivity) { result ->
                    when (result) {
                        is Resource.Loading -> {
                            binding.btnSave.isEnabled = false
                            binding.btnSave.text = "保存中..."
                        }
                        is Resource.Success -> {
                            Toast.makeText(this@ProfileEditActivity, "保存成功", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is Resource.Error -> {
                            binding.btnSave.isEnabled = true
                            binding.btnSave.text = "保存"
                            Toast.makeText(this@ProfileEditActivity, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun start(context: Context) =
            context.startActivity(Intent(context, ProfileEditActivity::class.java))
    }
}
