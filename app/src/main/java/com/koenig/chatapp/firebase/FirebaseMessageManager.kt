package com.koenig.chatapp.firebase


import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.models.MessageStore
import java.util.*
import kotlin.collections.HashMap

object FirebaseMessageManager: MessageStore {

    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun sendMessage(message: MessageModel)
    {
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

    override fun retrieveMessages(fromUserId: String, toUserId: String, messages: MutableLiveData<List<MessageModel>>)
    {
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
                        localSentMessages.add(sentMessage)
                    }

                }
                messages.value = localSentMessages

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun removeHasNewMessageFlag(fromUserId: String, toUserId: String)
    {
        database
            .child("users")
            .child(fromUserId)
            .child("contacts")
            .child(toUserId)
            .child("hasNewMessage")
            .setValue(false)
    }

    override fun removeHasNewGroupMessageFlag(groupId: String, currentUserId: String)
    {
        database
            .child("groups")
            .child(groupId)
            .child("groupMembers")
            .child(currentUserId)
            .child("hasNewMessage")
            .setValue(false)
    }

    override fun sendGroupMessage(message: MessageModel, groupMembers: List<ContactModel>)
    {
        val key = database.child("messages").push().key ?: return
        message.uid = key

        val messageValues = message.toMap()
        val childAdd = HashMap<String, Any>()

        // Add the message to DB
        childAdd["/messages/$key"] = messageValues

        // Add recent message for group members
         childAdd["groups/${message.toUserId}/recentMessage"] = message

        groupMembers.forEach {
            childAdd["groups/${message.toUserId}/groupMembers/${it.userId}/hasNewMessage"] = true
        }

        database.updateChildren(childAdd)
    }

    override fun retrieveGroupMessages(ownUserId: String, toGroupId: String, messages: MutableLiveData<List<MessageModel>>)
    {
        database.child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val localSentMessages = ArrayList<MessageModel>()
                    val children = snapshot.children

                    children.forEach{
                        val sentMessage = it.getValue(MessageModel::class.java)

                        if (sentMessage!!.toUserId == toGroupId)
                        {
                            localSentMessages.add(sentMessage)
                        }
                    }
                    messages.value = localSentMessages
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    override fun receiveRecentMessagesForUser(currentUserId: String, message: MutableLiveData<MessageModel>)
    {
        database.child("messages").orderByChild("toUserId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val localSentMessages = ArrayList<MessageModel>()
                    val children = snapshot.children

                    children.forEach {
                        val receivedMessage = it.getValue(MessageModel::class.java)
                        localSentMessages.add(receivedMessage!!)
                    }

                    // Get the most recent message
                    val recentMessage = localSentMessages.maxByOrNull { it.timeStamp }

                    if(recentMessage != null)
                    {
                        message.value = recentMessage
                        // Show Notification only once
                        database.child("messages").child(recentMessage.uid).child("wasRead").setValue(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    override fun receiveRecentMessagesForGroup(currentGroupId: String, message: MutableLiveData<MessageModel>)
    {
        // TODO
    }
}