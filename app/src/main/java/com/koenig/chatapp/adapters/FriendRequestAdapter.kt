package com.koenig.chatapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.databinding.ListItemFriendRequestBinding
import com.koenig.chatapp.models.ContactModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

interface FriendRequestClickListener{
    fun onAcceptRequest(addUser: ContactModel)
    fun onRejectRequest(rejectUser: ContactModel)
    fun withdrawRequest(withdrawUser: ContactModel)
}

class FriendRequestAdapter constructor(private var requests: ArrayList<ContactModel>,private val mode: String, private val listener: FriendRequestClickListener) : RecyclerView.Adapter<FriendRequestAdapter.MainHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = ListItemFriendRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val selectedContact = requests[holder.adapterPosition]
        holder.bind(selectedContact, listener)
    }

    override fun getItemCount(): Int = requests.size

    inner class MainHolder(val binding: ListItemFriendRequestBinding): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(user: ContactModel, listener: FriendRequestClickListener)
        {
            binding.root.tag = user
            binding.currentContact = user

            Picasso.get().load(user.photoUri)
                .resize(200, 200)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(binding.imageUser)

            if (mode == "sentTab")
            {
                binding.buttonRejectUser.visibility = View.GONE
                binding.buttonAcceptUser.visibility = View.GONE
                binding.buttonRemoveOpenRequest.visibility = View.VISIBLE
                binding.buttonRemoveOpenRequest.setOnClickListener { listener.withdrawRequest(user) }
            }
            else if(mode == "receiveTab")
            {
                binding.buttonRejectUser.visibility = View.VISIBLE
                binding.buttonAcceptUser.visibility = View.VISIBLE
                binding.buttonRemoveOpenRequest.visibility = View.GONE
                binding.buttonAcceptUser.setOnClickListener { listener.onAcceptRequest(user) }
                binding.buttonRejectUser.setOnClickListener { listener.onRejectRequest(user) }
            }

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