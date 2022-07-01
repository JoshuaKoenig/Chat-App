package com.koenig.chatapp.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class MessageModel(

    var uid: String = "",
    var fromUserId: String = "",
    var toUserId: String = "",
    var message: String = "",
    var timeStamp: String = "",
    var fromUserName: String = "",
    var firstMessage: Boolean = false,
    var wasRead: Boolean = false

) : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "fromUserId" to fromUserId,
            "toUserId" to toUserId,
            "message" to message,
            "timeStamp" to timeStamp,
            "fromUserName" to fromUserName,
            "firstMessage" to firstMessage,
            "wasRead" to wasRead
        )
    }
}