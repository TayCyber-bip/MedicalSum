package com.example.medicalsum

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medicalsum.model.ChatMessage

class ChatAdapter :
    ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).fromUser) 1 else 2

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val layout = if (viewType == 1)
            R.layout.item_chat_send
        else
            R.layout.item_chat_receive

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)

        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.txtMessage).text =
            getItem(position).text
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(old: ChatMessage, new: ChatMessage) =
            old === new

        override fun areContentsTheSame(old: ChatMessage, new: ChatMessage) =
            old == new
    }
}

