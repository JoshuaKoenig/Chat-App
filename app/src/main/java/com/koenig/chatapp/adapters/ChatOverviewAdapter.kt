package com.koenig.chatapp.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.databinding.ListItemChatContactBinding
import com.koenig.chatapp.models.ContactModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.time.Instant

interface ChatOverviewClickListener{
    fun onClickOpenChat(selectedUser: ContactModel)
}

class ChatOverviewAdapter constructor(private var contacts: ArrayList<ContactModel>, private val listener: ChatOverviewClickListener, private val owner: androidx.lifecycle.LifecycleOwner) : RecyclerView.Adapter<ChatOverviewAdapter.MainHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = ListItemChatContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val selectedContact = contacts[holder.adapterPosition]
        holder.bind(selectedContact, listener)
    }

    override fun getItemCount(): Int = contacts.size

    inner class MainHolder(val binding: ListItemChatContactBinding): RecyclerView.ViewHolder(binding.root)
    {
        @SuppressLint("SetTextI18n")
        fun bind(user: ContactModel, listener: ChatOverviewClickListener)
        {
            binding.root.tag = user
            binding.chatContact = user
            binding.root.setOnClickListener { listener.onClickOpenChat(user) }

            if(user.hasNewMessage)
            {
                binding.iconNewMessage.visibility = View.VISIBLE
                binding.recentMessage.setTypeface(binding.recentMessage.typeface, Typeface.BOLD)
            }
            else
            {
                binding.iconNewMessage.visibility = View.GONE
            }

            if(user.userName == user.recentMessage.fromUserName)
            {
                binding.textFromUserName.text = "${user.userName}: "
            }
            else
            {
                binding.textFromUserName.text = "You: "
            }

            val dateString = user.recentMessage.timeStamp.substring(0, 10)
            val timeString = user.recentMessage.timeStamp.substring(11,16)

            val todayDateString =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Instant.now().toString().substring(0, 10)
                else TODO("VERSION.SDK_INT < O")

            if(dateString == todayDateString)
            {
                binding.textMessageTime.text = timeString
            }
            else
            {
                binding.textMessageTime.text = dateString
            }

            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-4dffc.appspot.com/o/photos%2F${user.userId}.jpg?alt=media&token=3a3b9aeb-8193-44bd-b1d3-54b96a8de90f")
                .resize(200, 200)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(binding.imageUser)


            binding.executePendingBindings()
        }
    }
}

private fun customTransformation() : Transformation =
    RoundedTransformationBuilder()
        .borderColor(Color.WHITE)
        .borderWidth(2f)
        .cornerRadius(35f)
        .oval(false)
        .build()