package com.koenig.chatapp.models

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface UserStore {

    fun getUserById(userId: String, user: MutableLiveData<UserModel>)
    fun createUser(firebaseUser: FirebaseUser)
    fun updateUser(userId: String, user: UserModel)
    fun addContact(userId: String, user: UserModel)
    fun updateUserImage(userId: String, photoUri: Uri)
}