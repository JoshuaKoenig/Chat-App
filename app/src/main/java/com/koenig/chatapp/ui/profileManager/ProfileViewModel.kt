package com.koenig.chatapp.ui.profileManager

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.koenig.chatapp.firebase.FirebaseDBManager
import com.koenig.chatapp.models.UserModel
import java.lang.Exception

class ProfileViewModel : ViewModel() {

    private val userModel = MutableLiveData<UserModel>()

    var observableProfile: LiveData<UserModel>
        get() = userModel
        set(value) {userModel.value = value.value}

    fun getProfile(userId: String)
    {
        try {
            FirebaseDBManager.getUserById(userId, userModel)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }

    fun updateProfile(userId: String, userModel: UserModel, user: MutableLiveData<FirebaseUser>)
    {
        try {
            // Update user in Database
            FirebaseDBManager.updateUser(userId, userModel)

            // Update firebase user
            val profileUpdates = userProfileChangeRequest {
                displayName = userModel.userName
                photoUri = userModel.photoUri.toUri()
            }

            user.value!!.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        user.postValue(user.value!!)
                    }
                }

        }
        catch(e: Exception) {
            //TODO: Catch exception
        }
    }

    fun updateProfileImage(userId: String, photoUri: Uri)
    {
        try {
            FirebaseDBManager.updateUserImage(userId, photoUri)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }
}