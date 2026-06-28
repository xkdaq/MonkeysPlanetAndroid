package top.monkeysxu.planet.feature_exam.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import top.monkeysxu.planet.R
import top.monkeysxu.planet.databinding.ItemCategoryBinding
import top.monkeysxu.planet.feature_exam.model.Category

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val items = mutableListOf<Category>()
    var onItemClick: ((Category) -> Unit)? = null

    fun setData(data: List<Category>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Category) {
            binding.tvParentName.text = item.name

            val children = item.children
            if (!children.isNullOrEmpty()) {
                // 有子分类：隐藏父级箭头，展示子分类列表
                binding.tvParentArrow.visibility = View.GONE
                binding.childrenContainer.visibility = View.VISIBLE
                binding.childrenContainer.removeAllViews()

                children.forEach { child ->
                    val childView = LayoutInflater.from(binding.root.context)
                        .inflate(R.layout.item_category_child, binding.childrenContainer, false)
                    childView.findViewById<TextView>(R.id.tvChildName).text = child.name
                    childView.setOnClickListener {
                        onItemClick?.invoke(child)
                    }
                    binding.childrenContainer.addView(childView)
                }

                // 父级不可直接点击进入练习
                binding.parentRow.setOnClickListener(null)
            } else {
                // 无子分类：显示箭头，可点击进入练习
                binding.tvParentArrow.visibility = View.VISIBLE
                binding.childrenContainer.visibility = View.GONE
                binding.parentRow.setOnClickListener {
                    onItemClick?.invoke(item)
                }
            }
        }
    }
}
