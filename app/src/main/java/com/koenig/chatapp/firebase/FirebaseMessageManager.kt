package com.koenig.chatapp.firebase


import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.models.MessageStore
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

object FirebaseMessageManager: MessageStore {

    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun sendMessage(message: MessageModel) {

        val key = database.child("messages").push().key ?: return
        message.uid = key

        val messageValues = message.toMap()
        val childAdd = HashMap<String, Any>()

        // Add the message to DB
        childAdd["/messages/$key"] = messageValues

        // Add has conversation flag
        childAdd["users/${message.fromUserId}/contacts/${message.toUserId}/hasConversation"] = true
        childAdd["users/${message.toUserId}/contacts/${message.fromUserId}/hasConversation"] = true

        // Add recent message for both contacts
        childAdd["users/${message.fromUserId}/contacts/${message.toUserId}/recentMessage"] = message
        childAdd["users/${message.toUserId}/contacts/${message.fromUserId}/recentMessage"] = message

        // Add has new message flag
        childAdd["users/${message.toUserId}/contacts/${message.fromUserId}/hasNewMessage"] = true

        database.updateChildren(childAdd)

    }

    override fun retrieveMessages(fromUserId: String, toUserId: String, messages: MutableLiveData<List<MessageModel>>){

        Log.d("MessageManager", "RetrieveMessage")

        // Sent Messages
        database.child("messages")
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val localSentMessages = ArrayList<MessageModel>()
                val children = snapshot.children

                children.forEach{
                    val sentMessage = it.getValue(MessageModel::class.java)

                    if (((sentMessage!!.fromUserId == fromUserId) && (sentMessage.toUserId == toUserId))
                        || ((sentMessage.fromUserId == toUserId) && (sentMessage.toUserId == fromUserId))
                    )
                    {
                        localSentMessages.add(sentMessage!!)
                    }

                }
                messages.value = localSentMessages

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun retrieveRecentMessage(fromUserId: String, toUserId: String, message: MutableLiveData<MessageModel>)
    {
     /*   database.child("messages").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val allMessages = ArrayList<MessageModel>()
                var recentMessage = MessageModel()
                val children = snapshot.children

                children.forEach{
                    val currentMessage = it.getValue(MessageModel::class.java)

                    if (((currentMessage!!.fromUserId == fromUserId) && (currentMessage.toUserId == toUserId))
                        || ((currentMessage.fromUserId == toUserId) && (currentMessage.toUserId == fromUserId)))
                    {
                        allMessages.add(currentMessage)
                    }

                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    allMessages.sortBy { Instant.parse(it.timeStamp) }
                    recentMessage = allMessages[allMessages.size-1]
                }
                message.value = recentMessage
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })*/
    }

    override fun removeHasNewMessageFlag(fromUserId: String, toUserId: String) {
        database
            .child("users")
            .child(fromUserId)
            .child("contacts")
            .child(toUserId)
            .child("hasNewMessage")
            .setValue(false)
    }
}