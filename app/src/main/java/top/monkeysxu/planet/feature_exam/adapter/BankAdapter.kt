package top.monkeysxu.planet.feature_exam.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import top.monkeysxu.planet.databinding.ItemBankBinding
import top.monkeysxu.planet.feature_exam.model.Bank

class BankAdapter : RecyclerView.Adapter<BankAdapter.ViewHolder>() {

    private val items = mutableListOf<Bank>()
    var onItemClick: ((Bank) -> Unit)? = null

    fun setData(data: List<Bank>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemBankBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Bank) {
            binding.tvName.text = item.name
            binding.tvDesc.text =
                if (item.description.isNullOrEmpty()) "暂无题库描述" else item.description
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
