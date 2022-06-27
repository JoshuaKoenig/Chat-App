package com.koenig.chatapp.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class GroupModel(
    var groupId: String = "",
    var groupName: String = "",
    var adminUid: String = "",
    var photoUri: String = "",
    var description: String = "",
    var groupMembers: HashMap<String, ContactModel> = hashMapOf(),
    var recentMessage: MessageModel = MessageModel(),

) : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "groupId" to groupId,
            "groupName" to groupName,
            "adminUid" to adminUid,
            "photoUri" to photoUri,
            "description" to description,
            "groupMembers" to groupMembers,
            "recentMessage" to recentMessage,
        )
    }
}