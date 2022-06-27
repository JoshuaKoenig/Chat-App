package com.koenig.chatapp.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.koenig.chatapp.models.*

object FirebaseGroupChatManager: GroupStore {

    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun createGroupChat(group: GroupModel, firstMessage: MessageModel)
    {
        val key = database.child("groups").push().key ?: return
        group.groupId = key
        group.recentMessage = firstMessage

        // Send first message to group
        firstMessage.toUserId = key
        FirebaseMessageManager.sendGroupMessage(firstMessage, ArrayList(group.groupMembers.values))

        val groupValues = group.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/groups/$key"] = groupValues
        database.updateChildren(childAdd)
    }

    override fun getGroupChatsForUser(userId: String, groups: MutableLiveData<List<GroupModel>>)
    {
        database.child("groups").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val localGroupList = ArrayList<GroupModel>()
                val children = snapshot.children

                children.forEach { ds ->
                    val currentGroup = ds.getValue(GroupModel::class.java)

                    currentGroup!!.groupMembers.forEach{

                        // All members get the group
                        if(it.value.userId == userId)
                        {
                            localGroupList.add(currentGroup)
                        }
                    }
                }

                database.child("groups").removeEventListener(this)
                groups.value = localGroupList
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun addUserToGroupChat(groupId: String, contactToAdd: ContactModel)
    {
        database
            .child("groups")
            .child(groupId)
            .child("groupMembers")
            .child(contactToAdd.userId)
            .setValue(contactToAdd)
    }

    override fun removeUserFromGroupChat(groupId: String, contactToRemove: ContactModel)
    {
        database
            .child("groups")
            .child(groupId)
            .child("groupMembers")
            .child(contactToRemove.userId)
            .removeValue()
    }

    override fun getGroupById(groupId: String, group: MutableLiveData<GroupModel>) {

        database.child("groups").child(groupId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                group.value = snapshot.getValue<GroupModel>()
                database.child("groups").child(groupId).removeEventListener(this)

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }
}