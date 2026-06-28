package top.monkeysxu.planet.feature_profile.feedback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import top.monkeysxu.planet.core.base.BaseActivity
import top.monkeysxu.planet.databinding.ActivityFeedbackBinding

class FeedbackActivity : BaseActivity<ActivityFeedbackBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, FeedbackActivity::class.java))
        }
    }

    override fun inflateBinding(): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.toolbar.ivBack.setOnClickListener { finish() }
        binding.toolbar.tvTitle.text = "问题反馈"

        binding.btnSubmit.setOnClickListener {
            val content = binding.etContent.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入反馈内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "反馈已提交，感谢您的建议", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
