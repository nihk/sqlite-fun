package nick.template.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nick.template.R
import nick.template.data.Item
import nick.template.databinding.ItemBinding

class ItemAdapter(
    private val updates: (Item) -> Unit,
    private val deletes: (Item) -> Unit
) : ListAdapter<Item, ItemViewHolder>(ItemDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> ItemBinding.inflate(inflater, parent, false) }
            .let { binding -> ItemViewHolder(binding, updates, deletes) }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}

class ItemViewHolder(
    private val binding: ItemBinding,
    private val updates: (Item) -> Unit,
    private val deletes: (Item) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Item) {
        binding.id.text = binding.root.resources.getString(R.string.item_id, item.id)
        binding.name.text = item.name
        binding.description.text = item.description
        binding.rating.text = binding.root.resources.getString(R.string.item_rating, item.rating)
        binding.root.setOnClickListener {
            updates(item.copy(rating = item.rating + 1))
        }
        binding.trash.setOnClickListener {
            deletes(item)
        }
    }
}
