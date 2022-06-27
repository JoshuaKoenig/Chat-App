package com.koenig.chatapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.databinding.ListItemGroupContactBinding
import com.koenig.chatapp.models.ContactModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

interface GroupContactsClickListener{
   fun onClickShowUserProfile(user: ContactModel)
   fun onClickRemoveUser(user: ContactModel)
}

class GroupContactsAdapter constructor(private var contacts: ArrayList<ContactModel>, private val listener: GroupContactsClickListener, private val adminUid: String, private val currentUserId: String) : RecyclerView.Adapter<GroupContactsAdapter.MainHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = ListItemGroupContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val selectedContact = contacts[holder.adapterPosition]
        holder.bind(selectedContact, listener)
    }

    override fun getItemCount(): Int = contacts.size

    inner class MainHolder(val binding: ListItemGroupContactBinding): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(user: ContactModel, listener: GroupContactsClickListener)
        {
            binding.root.tag = user
            binding.contact = user

            Picasso.get().load(user.photoUri)
                .resize(200, 200)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(binding.imageUser)

            binding.root.setOnClickListener { listener.onClickShowUserProfile(user) }

            // Show Admin
            if (user.userId == adminUid)
            {
                binding.textisAdmin.visibility = View.VISIBLE
            }
            else
            {
                binding.textisAdmin.visibility = View.GONE
            }

            // Show Remove Button
            if(currentUserId == adminUid && user.userId != adminUid)
            {
                binding.buttonRemoveUser.visibility = View.VISIBLE
            }
            else
            {
                binding.buttonRemoveUser.visibility = View.GONE
            }

            binding.buttonRemoveUser.setOnClickListener {
                listener.onClickRemoveUser(user)
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