package com.koenig.chatapp.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.databinding.ListItemAddUserBinding
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.UserModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

interface FoundUserClickListener{
    fun onUserAddClick(addUser: ContactModel)
}

class FoundUserAdapter constructor(private var users: ArrayList<UserModel>, private val listener: FoundUserClickListener, private val currentUsersContactIds: ArrayList<String>, private val currentTab: Int) : RecyclerView.Adapter<FoundUserAdapter.MainHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = ListItemAddUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
       val user = users[holder.adapterPosition]
        holder.bind(user, listener)
    }

    override fun getItemCount(): Int = users.size

    inner class MainHolder(val binding: ListItemAddUserBinding): RecyclerView.ViewHolder(binding.root)
    {
        @SuppressLint("SetTextI18n")
        fun bind(user: UserModel, listener: FoundUserClickListener)
        {
            binding.root.tag = user
            binding.user = user

            if(currentTab == 0)
            {
                // Get the amount of common contacts
                val contactsContactIds = ArrayList<String>()
                user.contacts.values.forEach {
                    contactsContactIds.add(it.userId)
                }
                val commonContacts = currentUsersContactIds.intersect(contactsContactIds.toSet()).size

                // Set the amount to text
                binding.textCommonUsers.text = "Common Contacts: $commonContacts"

                // Remove email
                binding.textUserMail.visibility = View.GONE
                // show common user amount
                binding.textCommonUsers.visibility = View.VISIBLE
            }
            else
            {
                // Remove common user amount
                binding.textCommonUsers.visibility = View.GONE
                // Show email
                binding.textUserMail.visibility = View.VISIBLE
            }

            if(user.photoUri.isNotEmpty())
            {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-4dffc.appspot.com/o/photos%2F${user.userId}.jpg?alt=media&token=3a3b9aeb-8193-44bd-b1d3-54b96a8de90f")
                    .resize(200, 200)
                    .transform(customTransformation())
                    .centerCrop()
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(binding.imageUser)
            }

            val contactModel = ContactModel(
                userId = user.userId,
                userName = user.userName,
                email = user.email,
                photoUri = user.photoUri,
                status = user.status
            )

            binding.buttonAddUser.setOnClickListener { listener.onUserAddClick(contactModel) }
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