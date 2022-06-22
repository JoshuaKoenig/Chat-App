package com.koenig.chatapp.adapters

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.R
import com.koenig.chatapp.databinding.ListItemMessageBinding
import com.koenig.chatapp.models.MessageModel


class ChatAdapter constructor(private var messages: ArrayList<MessageModel>, private var currentUserId: String, private var context: Context) : RecyclerView.Adapter<ChatAdapter.MainHolder>()
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

            // TODO: Set Background and Background Tint and layout gravity
            if(currentUserId == message.fromUserId)
            {
                binding.parentLayout.gravity = Gravity.END
                val drawable = ContextCompat.getDrawable(context, R.drawable.message_shape_sent)
                drawable!!.setTint(Color.parseColor("#4CAF50"))
                binding.cardLayout.background = drawable
            }
            else
            {
                binding.parentLayout.gravity = Gravity.START
                val drawable = ContextCompat.getDrawable(context, R.drawable.message_shape_received)
                drawable!!.setTint(Color.parseColor("#333742"))
                binding.cardLayout.background = drawable
            }

            binding.executePendingBindings()
        }
    }
}