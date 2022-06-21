package com.koenig.chatapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.databinding.ListItemMessageBinding
import com.koenig.chatapp.models.MessageModel


class ChatAdapter constructor(private var messages: ArrayList<MessageModel>) : RecyclerView.Adapter<ChatAdapter.MainHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = ListItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val message = messages[holder.adapterPosition]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    inner class MainHolder(val binding: ListItemMessageBinding): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(message: MessageModel)
        {
            binding.root.tag = message
            binding.message = message

            binding.executePendingBindings()
        }
    }
}