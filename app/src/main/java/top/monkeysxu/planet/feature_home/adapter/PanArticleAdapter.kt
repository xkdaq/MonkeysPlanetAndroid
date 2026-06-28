package top.monkeysxu.planet.feature_home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import top.monkeysxu.planet.databinding.ItemPanArticleBinding
import top.monkeysxu.planet.feature_home.model.PanArticle

class PanArticleAdapter : RecyclerView.Adapter<PanArticleAdapter.ViewHolder>() {

    private val items = mutableListOf<PanArticle>()
    var onItemClick: ((PanArticle) -> Unit)? = null

    fun setData(data: List<PanArticle>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPanArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size.coerceAtMost(5)

    inner class ViewHolder(private val binding: ItemPanArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PanArticle) {
            binding.tvTitle.text = item.title
            binding.tvTags.text = item.categoryList?.joinToString(" · ") ?: ""
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
