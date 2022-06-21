package com.koenig.chatapp.models

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface UserStore {

    fun getUserById(userId: String, user: MutableLiveData<UserModel>)
    fun createUser(firebaseUser: FirebaseUser)
    fun updateUser(userId: String, user: UserModel)
    fun addContact(currentUserId: String, userAdd: UserModel)
    fun updateUserImage(userId: String, photoUri: Uri)
    fun getAllUsers(userList: MutableLiveData<List<UserModel>>)
    fun getAllContactsForUser(userId: String, contactList: MutableLiveData<List<UserModel>>)
}