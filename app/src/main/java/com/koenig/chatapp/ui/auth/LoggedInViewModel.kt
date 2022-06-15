package com.koenig.chatapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.koenig.chatapp.firebase.FirebaseAuthManager

class LoggedInViewModel(app: Application): AndroidViewModel(app) {

    var firebaseAuthManager: FirebaseAuthManager = FirebaseAuthManager(app)
    var liveFirebaseUser: MutableLiveData<FirebaseUser> = firebaseAuthManager.liveFirebaseUser
    var loggedOut: MutableLiveData<Boolean> = firebaseAuthManager.loggedOut

    fun logOut()
    {
        firebaseAuthManager.logout()
    }
}