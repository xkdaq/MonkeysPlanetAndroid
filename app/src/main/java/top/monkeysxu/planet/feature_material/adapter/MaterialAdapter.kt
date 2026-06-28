package top.monkeysxu.planet.feature_material.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import top.monkeysxu.planet.databinding.ItemMaterialBinding
import top.monkeysxu.planet.feature_material.model.MaterialItem

class MaterialAdapter : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    private val items = mutableListOf<MaterialItem>()
    var onItemClick: ((MaterialItem) -> Unit)? = null

    fun setData(data: List<MaterialItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun addData(data: List<MaterialItem>) {
        val oldSize = items.size
        items.addAll(data)
        notifyItemRangeInserted(oldSize, data.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMaterialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == itemCount - 1)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemMaterialBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MaterialItem, isLast: Boolean) {
            binding.tvTitle.text = item.title

            // 置顶标签
            binding.tvTopTag.visibility = if (item.isTop == 1) View.VISIBLE else View.GONE

            // 科目 / 分类标签
            val tags = item.categoryList?.takeIf { it.isNotEmpty() }
                ?: listOfNotNull(item.subjectName, item.categoryName).filter { it.isNotBlank() }
            val categories = tags.joinToString(" · ")
            binding.tvCategory.text = categories
            binding.tvCategory.visibility = if (categories.isNotEmpty()) View.VISIBLE else View.GONE

            // 网盘标识行
            val hasBaidu = !item.baiduUrl.isNullOrEmpty()
            val hasQuark = !item.quarkUrl.isNullOrEmpty()
            if (hasBaidu || hasQuark) {
                binding.panRow.visibility = View.VISIBLE
                binding.tvBaiduHint.visibility = if (hasBaidu) View.VISIBLE else View.GONE
                binding.tvQuarkHint.visibility = if (hasQuark) View.VISIBLE else View.GONE
            } else {
                binding.panRow.visibility = View.GONE
            }

            // 分割线
            binding.divider.visibility = if (isLast) View.GONE else View.VISIBLE

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
