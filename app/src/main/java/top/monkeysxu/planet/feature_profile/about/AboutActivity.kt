package top.monkeysxu.planet.feature_profile.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.core.config.LegalUrls
import top.monkeysxu.planet.databinding.ActivityAboutBinding
import top.monkeysxu.planet.feature_common.webview.WebViewActivity

class AboutActivity : BaseActivity<ActivityAboutBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AboutActivity::class.java))
        }
    }

    override fun inflateBinding(): ActivityAboutBinding {
        return ActivityAboutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.toolbar.ivBack.setOnClickListener { finish() }
        binding.toolbar.tvTitle.text = "关于我们"
        binding.tvUserAgreement.setOnClickListener {
            WebViewActivity.start(this, "用户服务协议", LegalUrls.USER_AGREEMENT)
        }
        binding.tvPrivacyPolicy.setOnClickListener {
            WebViewActivity.start(this, "隐私政策", LegalUrls.PRIVACY_POLICY)
        }
    }
}
