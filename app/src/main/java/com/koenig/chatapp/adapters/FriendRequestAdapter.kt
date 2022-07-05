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

            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-4dffc.appspot.com/o/photos%2F${user.userId}.jpg?alt=media&token=3a3b9aeb-8193-44bd-b1d3-54b96a8de90f")
                .resize(200, 200)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(binding.imageUser)

            if (mode == "sentTab")
            {
                binding.imageAccept.visibility = View.GONE
                binding.imageReject.visibility = View.VISIBLE
            }
            else if(mode == "receiveTab")
            {
                binding.imageAccept.visibility = View.VISIBLE
                binding.imageReject.visibility = View.VISIBLE
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