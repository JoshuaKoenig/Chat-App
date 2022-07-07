package com.koenig.chatapp.ui.contactProfileManager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koenig.chatapp.firebase.FirebaseDBManager

class ContactProfileViewModel : ViewModel() {

    private val hasAlreadyLiked = MutableLiveData<Boolean>()

    var hasAlreadyLikedObserver: LiveData<Boolean>
        get() = hasAlreadyLiked
        set(value) {hasAlreadyLiked.value = value.value}


    fun increaseLike(forUserId: String, currentLikeAmount: Int)
    {
        try {
            FirebaseDBManager.increaseLikeForUser(forUserId, currentLikeAmount)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }

    fun getHasAlreadyLiked(currentUserId: String, contactId: String)
    {
        try {
            FirebaseDBManager.hasAlreadyLiked(currentUserId, contactId, hasAlreadyLiked)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }

    fun setHasLiked(currentUserId: String, contactId: String, hasLiked: Boolean)
    {
        FirebaseDBManager.setHasLiked(currentUserId, contactId, hasLiked)
        hasAlreadyLiked.postValue(hasLiked)
    }
}