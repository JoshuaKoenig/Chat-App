package com.koenig.chatapp.ui.auth

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.koenig.chatapp.firebase.FirebaseAuthManager
import com.koenig.chatapp.firebase.FirebaseImageManager

class LoggedInViewModel(app: Application): AndroidViewModel(app) {

    private var firebaseAuthManager: FirebaseAuthManager = FirebaseAuthManager(app)
    var liveFirebaseUser: MutableLiveData<FirebaseUser> = firebaseAuthManager.liveFirebaseUser
    var loggedOut: MutableLiveData<Boolean> = firebaseAuthManager.loggedOut

    fun logOut()
    {
        firebaseAuthManager.logout()
        FirebaseImageManager.imageUri.postValue(Uri.EMPTY)
    }
}