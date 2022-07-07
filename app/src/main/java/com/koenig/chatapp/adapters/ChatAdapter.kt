package com.koenig.chatapp.adapters

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.R
import com.koenig.chatapp.databinding.ListItemMessageBinding
import com.koenig.chatapp.models.MessageModel
import java.time.Instant

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

            val params: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            // First message
            if (message.firstMessage)
            {
                binding.parentLayout.gravity = Gravity.CENTER
                val drawable = ContextCompat.getDrawable(context, R.drawable.message_shape)
                drawable!!.setTint(Color.parseColor("#3C414C"))
                binding.cardLayout.background = drawable
                binding.textMessage.setTextColor(Color.parseColor("#FFFFFF"))
            }
            // Own Messages
            else if(currentUserId == message.fromUserId)
            {
                binding.parentLayout.gravity = Gravity.END
                val drawable = ContextCompat.getDrawable(context, R.drawable.message_shape_sent)
                drawable!!.setTint(Color.parseColor("#6C63FF"))
                binding.cardLayout.background = drawable
                params.setMargins(1, -1, 0, 0)

                binding.textTimeStamp.layoutParams = params
            }
            // Other Messages
            else
            {
                binding.parentLayout.gravity = Gravity.START
                val drawable = ContextCompat.getDrawable(context, R.drawable.message_shape_received)
                drawable!!.setTint(Color.parseColor("#cdc9ff"))
                binding.cardLayout.background = drawable
                params.setMargins(0, -1, 1, 0)

                binding.textTimeStamp.layoutParams = params
            }

            val dateString = message.timeStamp.substring(0, 10)
            val timeString = message.timeStamp.substring(11,16)

            val todayDateString =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Instant.now().toString().substring(0, 10)
                else TODO("VERSION.SDK_INT < O")

            if(dateString == todayDateString)
            {
                binding.textTimeStamp.text = timeString
            }
            else
            {
                binding.textTimeStamp.text = dateString
            }

            binding.executePendingBindings()
        }
    }
}