package top.monkeysxu.planet.feature_common.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    override fun inflateBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.toolbar.ivBack.setOnClickListener { finish() }
        binding.toolbar.tvTitle.text = "设置"
    }
}
