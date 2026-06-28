package top.monkeysxu.planet.feature_home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import top.monkeysxu.planet.databinding.ItemArticleBinding
import top.monkeysxu.planet.feature_home.model.ArticleItem

class ArticleAdapter : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    private val items = mutableListOf<ArticleItem>()
    var onItemClick: ((ArticleItem) -> Unit)? = null

    /** 是否限制最多显示条数（首页用），0=不限制 */
    var maxItems: Int = 0

    fun setData(data: List<ArticleItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun appendData(data: List<ArticleItem>) {
        val start = items.size
        items.addAll(data)
        notifyItemRangeInserted(start, data.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == itemCount - 1)
    }

    override fun getItemCount(): Int {
        return if (maxItems > 0) items.size.coerceAtMost(maxItems) else items.size
    }

    inner class ViewHolder(private val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ArticleItem, isLast: Boolean) {
            binding.tvTitle.text = item.title
            binding.tvDate.text = item.date ?: ""

            // 置顶标签
            if (item.isTop == 1) {
                binding.tvTopTag.visibility = View.VISIBLE
            } else {
                binding.tvTopTag.visibility = View.GONE
            }

            // 最后一项隐藏分割线
            binding.divider.visibility = if (isLast) View.GONE else View.VISIBLE

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
