package com.koenig.chatapp.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.databinding.ListItemGroupChatBinding
import com.koenig.chatapp.models.GroupModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.time.Instant

interface GroupChatListener{
    fun onClickOpenGroupChat(selectedGroupChat: GroupModel)
}

class GroupChatAdapter constructor(private var groupChats: ArrayList<GroupModel>, private val listener: GroupChatListener, private val currentUserId: String) : RecyclerView.Adapter<GroupChatAdapter.MainHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = ListItemGroupChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val selectedContact = groupChats[holder.adapterPosition]
        holder.bind(selectedContact, listener)
    }

    override fun getItemCount(): Int = groupChats.size

    inner class MainHolder(val binding: ListItemGroupChatBinding): RecyclerView.ViewHolder(binding.root)
    {
        @SuppressLint("SetTextI18n")
        fun bind(groupChat: GroupModel, listener: GroupChatListener)
        {
            binding.root.tag = groupChat
            binding.groupChat = groupChat
            binding.root.setOnClickListener { listener.onClickOpenGroupChat(groupChat) }

            val currentUserHasNewMsgFlag: Boolean = groupChat.groupMembers[currentUserId]!!.hasNewMessage

            if(currentUserHasNewMsgFlag)
            {
                binding.iconNewMessage.visibility = View.VISIBLE
                binding.recentMessage.setTypeface(binding.recentMessage.typeface, Typeface.BOLD)
            }
            else
            {
                binding.iconNewMessage.visibility = View.GONE
            }

            if(groupChat.recentMessage.firstMessage)
            {
                binding.textFromUserName.visibility = View.GONE
            }
            else if(currentUserId == groupChat.recentMessage.fromUserId)
            {
                binding.textFromUserName.text = "You: "
            }
            else
            {
                binding.textFromUserName.text = "${groupChat.recentMessage.fromUserName}: "
            }

             val dateString = groupChat.recentMessage.timeStamp.substring(0, 10)
             val timeString = groupChat.recentMessage.timeStamp.substring(11,16)

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

            Picasso.get().load(groupChat.photoUri)
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