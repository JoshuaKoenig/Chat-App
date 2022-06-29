package com.koenig.chatapp.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class UserModel(

    var userId: String = "",
    var userName: String = "",
    var email: String = "",
    var photoUri: String = "",
    var status: String = "",
    var contacts: HashMap<String, UserModel> = hashMapOf(),
    var openRequests: HashMap<String, ContactModel> = hashMapOf(),
    var receivedRequests: HashMap<String, ContactModel> = hashMapOf(),
    var isMapEnabled: Boolean = false,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0

) : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "userName" to userName,
            "email" to email,
            "photoUri" to photoUri,
            "status" to status,
            "contacts" to contacts,
            "openRequests" to openRequests,
            "receivedRequests" to receivedRequests,
            "isMapEnabled" to isMapEnabled,
            "latitude" to latitude,
            "longitude" to longitude
        )
    }
}