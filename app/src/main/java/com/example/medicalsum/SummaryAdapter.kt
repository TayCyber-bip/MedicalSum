package com.example.medicalsum

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medicalsum.data.SummaryEntity

class SummaryAdapter :
    RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

        private val items = mutableListOf<SummaryEntity>()

        fun submitList(newList: List<SummaryEntity>) {
            items.clear()
            items.addAll(newList)
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tvTitle)
            val summary: TextView = view.findViewById(R.id.tvSummaryPreview)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_summary, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.summary.text = item.summaryText

            //Click item
            holder.itemView.setOnClickListener {

                val intent = Intent(holder.itemView.context, SummaryDetailsActivity::class.java)

                intent.putExtra("summary_id", item.id)
                intent.putExtra("summary_title", item.title)
                intent.putExtra("summary_text", item.summaryText)
                //intent.putExtra("original_text", item.originalText)

                holder.itemView.context.startActivity(intent)
            }
        }
}