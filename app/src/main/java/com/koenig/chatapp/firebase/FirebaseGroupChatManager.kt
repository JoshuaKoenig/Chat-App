package com.koenig.chatapp.firebase

import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.koenig.chatapp.models.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object FirebaseGroupChatManager: GroupStore {

    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun createGroupChat(group: GroupModel, firstMessage: MessageModel, imageView: ImageView)
    {
        val key = database.child("groups").push().key ?: return
        group.groupId = key
        group.recentMessage = firstMessage

        // Send first message to group
        firstMessage.toUserId = key
        FirebaseMessageManager.sendGroupMessage(firstMessage, ArrayList(group.groupMembers.values))

        Log.d("PhotoUriCreate", group.groupId)

        val groupValues = group.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/groups/$key"] = groupValues
        database.updateChildren(childAdd)

        FirebaseImageManager.updateGroupImage(
            group.groupId,
            group.photoUri.toUri(),
            imageView,
            false
        )
    }

    override fun getGroupChatsForUser(userId: String, groups: MutableLiveData<List<GroupModel>>, groupFilter: String)
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
                            if(ds.child("groupName").value.toString().lowercase(Locale.getDefault()).contains(groupFilter.lowercase(Locale.getDefault())))
                            {
                                localGroupList.add(currentGroup)
                            }
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

    override fun removeUserFromGroupChat(groupId: String, contactToRemoveId: String)
    {
        database
            .child("groups")
            .child(groupId)
            .child("groupMembers")
            .child(contactToRemoveId)
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

    override fun updateGroupName(groupId: String, newGroupName: String) {
        database
            .child("groups")
            .child(groupId)
            .child("groupName")
            .setValue(newGroupName)
    }

    override fun updateGroupDescription(groupId: String, newDescription: String) {
        database
            .child("groups")
            .child(groupId)
            .child("description")
            .setValue(newDescription)
    }

    override fun updateGroupImage(groupId: String, newImageUrl: String) {
       Log.d("Debug_UpdateImage", newImageUrl)
        database
            .child("groups")
            .child(groupId)
            .child("photoUri")
            .setValue(newImageUrl)
    }

    override fun disbandGroup(groupId: String) {
        database
            .child("groups")
            .child(groupId)
            .removeValue()
    }
}