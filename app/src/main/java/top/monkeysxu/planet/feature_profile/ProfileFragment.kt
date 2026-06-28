package top.monkeysxu.planet.feature_profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import top.monkeysxu.planet.R
import top.monkeysxu.planet.core.base.BaseFragment
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.model.UserInfo
import top.monkeysxu.planet.core.network.RetrofitClient
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.databinding.FragmentProfileBinding
import top.monkeysxu.planet.feature_profile.about.AboutActivity
import top.monkeysxu.planet.feature_profile.api.UserApiService
import top.monkeysxu.planet.feature_profile.bindphone.BindPhoneActivity
import top.monkeysxu.planet.feature_profile.changepassword.ChangePasswordActivity
import top.monkeysxu.planet.feature_profile.edit.ProfileEditActivity
import top.monkeysxu.planet.feature_profile.feedback.FeedbackActivity
import top.monkeysxu.planet.feature_profile.login.LoginActivity
import top.monkeysxu.planet.feature_profile.records.StudyRecordsActivity
import top.monkeysxu.planet.feature_common.setting.SettingsActivity

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    private val tokenManager by lazy { TokenManager(requireContext()) }
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

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenuItems()
        initView()
        observeViewModel()
        viewModel.checkLoginState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLoginState()
        if (viewModel.isLoggedIn.value == true) {
            viewModel.loadUserInfo()
        }
    }

    private fun setupMenuItems() {
        // 账户卡片
        binding.menuBindPhone.tvMenuText.text = "绑定手机号"
        binding.menuBindPhone.ivMenuIcon.setImageResource(R.drawable.ic_phone)

        binding.menuChangePassword.tvMenuText.text = "修改密码"
        binding.menuChangePassword.ivMenuIcon.setImageResource(R.drawable.ic_lock)

        binding.menuStudyRecords.tvMenuText.text = "学习记录"
        binding.menuStudyRecords.ivMenuIcon.setImageResource(R.drawable.ic_records)

        // 通用卡片
        binding.menuSettings.tvMenuText.text = "设置"
        binding.menuSettings.ivMenuIcon.setImageResource(R.drawable.ic_settings)

        binding.menuAbout.tvMenuText.text = "关于我们"
        binding.menuAbout.ivMenuIcon.setImageResource(R.drawable.ic_about)

        binding.menuFeedback.tvMenuText.text = "问题反馈"
        binding.menuFeedback.ivMenuIcon.setImageResource(R.drawable.ic_feedback)
    }

    private fun initView() {
        // 头像点击：未登录时跳登录
        binding.ivAvatar.setOnClickListener {
            if (viewModel.isLoggedIn.value != true) {
                LoginActivity.start(requireContext())
            }
        }
        binding.btnLogin.setOnClickListener {
            LoginActivity.start(requireContext())
        }
        binding.btnEditProfile.setOnClickListener {
            checkLogin { ProfileEditActivity.start(requireContext()) }
        }

        // 菜单点击
        binding.menuBindPhone.menuRoot.setOnClickListener {
            checkLogin { BindPhoneActivity.start(requireContext()) }
        }
        binding.menuChangePassword.menuRoot.setOnClickListener {
            checkLogin { ChangePasswordActivity.start(requireContext()) }
        }
        binding.menuStudyRecords.menuRoot.setOnClickListener {
            checkLogin { StudyRecordsActivity.start(requireContext()) }
        }
        binding.menuSettings.menuRoot.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        binding.menuFeedback.menuRoot.setOnClickListener {
            startActivity(Intent(requireContext(), FeedbackActivity::class.java))
        }
        binding.menuAbout.menuRoot.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("确认退出")
                .setMessage("确定要退出当前账号吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("退出") { _, _ -> viewModel.logout() }
                .show()
        }
    }

    private fun checkLogin(action: () -> Unit) {
        if (viewModel.isLoggedIn.value == true) {
            action()
        } else {
            LoginActivity.start(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { loggedIn ->
            updateLoginState(loggedIn)
        }
        viewModel.userInfo.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    result.data?.let { updateUserInfo(it) }
                }
                is Resource.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutResult.collectLatest { result ->
                    if (result is Resource.Success) {
                        Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                    } else if (result is Resource.Error) {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateLoginState(loggedIn: Boolean) {
        if (loggedIn) {
            binding.layoutLoggedIn.visibility = View.VISIBLE
            binding.layoutNotLoggedIn.visibility = View.GONE
            binding.tvAccountSection.visibility = View.VISIBLE
            binding.cardAccount.visibility = View.VISIBLE
            binding.cardLogout.visibility = View.VISIBLE
            viewModel.loadUserInfo()
        } else {
            binding.layoutLoggedIn.visibility = View.GONE
            binding.layoutNotLoggedIn.visibility = View.VISIBLE
            binding.tvAccountSection.visibility = View.GONE
            binding.cardAccount.visibility = View.GONE
            binding.cardLogout.visibility = View.GONE
            binding.ivAvatar.setImageResource(R.drawable.ic_avatar_default)
        }
    }

    private fun updateUserInfo(userInfo: UserInfo) {
        binding.tvNickname.text = userInfo.nickname?.takeIf { it.isNotBlank() } ?: "微信用户"
        binding.tvUserId.text = "ID: 用户_${userInfo.id}"

        // 已绑定手机号才显示修改密码
        val hasPhone = userInfo.hasPhone == true || !userInfo.phone.isNullOrEmpty()
        binding.menuChangePassword.menuRoot.visibility = if (hasPhone) View.VISIBLE else View.GONE
        binding.dividerChangePassword.visibility = if (hasPhone) View.VISIBLE else View.GONE

        // 绑定手机号 badge
        binding.menuBindPhone.tvMenuBadge.visibility = View.VISIBLE
        binding.menuBindPhone.tvMenuBadge.text = if (hasPhone) "已绑定" else "未绑定"
        binding.menuBindPhone.tvMenuBadge.setTextColor(
            requireContext().getColor(if (hasPhone) R.color.primary else R.color.text_hint)
        )

        if (!userInfo.avatarUrl.isNullOrEmpty()) {
            binding.ivAvatar.load(userInfo.avatarUrl) {
                placeholder(R.drawable.ic_avatar_default)
                error(R.drawable.ic_avatar_default)
                transformations(CircleCropTransformation())
            }
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_avatar_default)
        }
    }
}
