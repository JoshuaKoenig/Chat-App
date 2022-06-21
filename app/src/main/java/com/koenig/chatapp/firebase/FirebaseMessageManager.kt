package com.koenig.chatapp.firebase


import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.models.MessageStore
import java.util.ArrayList

object FirebaseMessageManager: MessageStore {

    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun sendMessage(message: MessageModel) {

        val key = database.child("messages").push().key ?: return
        message.uid = key

        val messageValues = message.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/messages/$key"] = messageValues

        database.updateChildren(childAdd)

    }

    override fun retrieveMessage(fromUserId: String, toUserId: String, messages: MutableLiveData<List<MessageModel>>){

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
}