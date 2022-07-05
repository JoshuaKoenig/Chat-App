package com.koenig.chatapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.databinding.ListItemContactBinding
import com.koenig.chatapp.enums.ContactClickModes
import com.koenig.chatapp.models.ContactModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

interface ContactsClickListener{
    fun onClickOpenChat(selectedUser: ContactModel)
    fun onClickSelectUser(selectedUser: ContactModel)
    fun onClickAddUserToGroup(selectedUser: ContactModel)
}

class ContactsAdapter constructor(private var contacts: ArrayList<ContactModel>, private val listener: ContactsClickListener, private val clickMode: ContactClickModes) : RecyclerView.Adapter<ContactsAdapter.MainHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = ListItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val selectedContact = contacts[holder.adapterPosition]
        holder.bind(selectedContact, listener)
    }

    override fun getItemCount(): Int = contacts.size

    inner class MainHolder(val binding: ListItemContactBinding): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(user: ContactModel, listener: ContactsClickListener)
        {
            binding.root.tag = user
            binding.contact = user

            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-4dffc.appspot.com/o/photos%2F${user.userId}.jpg?alt=media&token=3a3b9aeb-8193-44bd-b1d3-54b96a8de90f")
                .resize(200, 200)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(binding.imageUser)

            when(clickMode)
            {
                ContactClickModes.DEFAULTMODE -> { binding.root.setOnClickListener { listener.onClickOpenChat(user) } }
                ContactClickModes.CREATEGROUPMODE -> { binding.root.setOnClickListener { listener.onClickSelectUser(user) } }
                ContactClickModes.ADDCONTACTMODE -> { binding.root.setOnClickListener { listener.onClickAddUserToGroup(user) } }
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