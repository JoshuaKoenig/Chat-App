package com.koenig.chatapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.databinding.ListItemContactBinding
import com.koenig.chatapp.models.UserModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

interface ContactsClickListener{
    fun onClickOpenChat(selectedUser: UserModel)
}

class ContactsAdapter constructor(private var contacts: ArrayList<UserModel>, private val listener: ContactsClickListener) : RecyclerView.Adapter<ContactsAdapter.MainHolder>()
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
        fun bind(user: UserModel, listener: ContactsClickListener)
        {
            binding.root.tag = user
            binding.user = user

            Picasso.get().load(user.photoUri)
                .resize(200, 200)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(binding.imageUser)

            binding.root.setOnClickListener { listener.onClickOpenChat(user) }

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