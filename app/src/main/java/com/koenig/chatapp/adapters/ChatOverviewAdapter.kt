package com.koenig.chatapp.adapters

import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.databinding.ListItemChatContactBinding
import com.koenig.chatapp.models.ContactModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.time.Instant
import kotlin.math.log

interface ChatOverviewClickListener{
    fun onClickOpenChat(selectedUser: ContactModel)
}

class ChatOverviewAdapter constructor(private var contacts: ArrayList<ContactModel>, private val listener: ChatOverviewClickListener) : RecyclerView.Adapter<ChatOverviewAdapter.MainHolder>()
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
        fun bind(user: ContactModel, listener: ChatOverviewClickListener)
        {
            binding.root.tag = user
            binding.chatContact = user
            binding.root.setOnClickListener { listener.onClickOpenChat(user) }

            if(user.hasNewMessage)
            {
                binding.iconNewMessage.visibility = View.VISIBLE
                binding.recentMessage.setTextColor(Color.GREEN)
            }
            else
            {
                binding.iconNewMessage.visibility = View.GONE
                binding.recentMessage.setTextColor(Color.parseColor("#99FFFFFF"))
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


            Picasso.get().load(user.photoUri)
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