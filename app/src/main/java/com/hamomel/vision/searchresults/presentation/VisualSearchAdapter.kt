package com.hamomel.vision.searchresults.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hamomel.vision.R
import com.hamomel.vision.searchresults.data.model.VisualSearchItem

/**
 * @author Роман Зотов on 12.12.2021
 */
class VisualSearchAdapter(
    private val onItemClickListener: (VisualSearchItem) -> Unit
) : RecyclerView.Adapter<VisualSearchAdapter.ItemViewHolder>() {

    private var items: List<VisualSearchItem> = emptyList()

    fun setData(data: List<VisualSearchItem>) {
        items = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(parent) {
            onItemClickListener(it)
        }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class ItemViewHolder(
        parent: ViewGroup,
        private val onClick: (VisualSearchItem) -> Unit
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_visual_search, parent, false)
    ) {
        private val imageView = itemView.findViewById<ImageView>(R.id.item_image_view)
        private val descriptionView = itemView.findViewById<TextView>(R.id.description_text_view)

        fun bind(item: VisualSearchItem) {
            imageView.load(item.contentUrl)
            descriptionView.text = item.name
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
