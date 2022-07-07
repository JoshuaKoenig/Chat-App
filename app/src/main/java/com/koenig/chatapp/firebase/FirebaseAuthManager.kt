package com.koenig.chatapp.firebase

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.koenig.chatapp.R
import com.koenig.chatapp.models.UserModel
import com.koenig.chatapp.ui.profileManager.ProfileViewModel

class FirebaseAuthManager(application: Application) {

    private var application: Application? = null
    var firebaseAuth: FirebaseAuth? = null
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()
    var loggedOut = MutableLiveData<Boolean>()
    var errorStatus = MutableLiveData<Boolean>()
    var googleSignInClient = MutableLiveData<GoogleSignInClient>()

    init
    {
        this.application = application
        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth!!.currentUser != null)
        {
            liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
            loggedOut.postValue(false)
            errorStatus.postValue(false)
            FirebaseImageManager.checkStorageForExistingProfilePic(firebaseAuth!!.currentUser!!.uid)
        }

        configureGoogleSignIn()
    }

    fun login(email: String?, password: String?)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            firebaseAuth!!.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(application!!.mainExecutor) { task ->
                    if (task.isSuccessful) {
                        liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
                        errorStatus.postValue(false)
                    } else {
                        errorStatus.postValue(true)
                    }
                }
        }
    }

    fun register(email: String?, password: String?, userName: String?)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            firebaseAuth!!.createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(application!!.mainExecutor) {task ->
                    if (task.isSuccessful)
                    {
                        initProfileData(userName)
                        errorStatus.postValue(false)
                    }
                    else
                    {
                        errorStatus.postValue(true)
                    }
                }
        }
    }

    //TODO: Change to Profile View Model
    private fun initProfileData(userName: String?){

        val user = firebaseAuth!!.currentUser

        val newUserName: String = userName ?: "New User"

        val profileUpdates = userProfileChangeRequest {
            displayName = newUserName
            photoUri = Uri.parse("android.resource://com.koenig.chatapp/drawable/empty_profile")
        }

        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    liveFirebaseUser.postValue(user!!)
                    FirebaseDBManager.createUser(firebaseAuth!!.currentUser!!)
                }
            }
    }

    private fun configureGoogleSignIn()
    {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application!!.getString(R.string.server_client_id))
            .requestEmail()
            .build()

        googleSignInClient.value = GoogleSignIn.getClient(application!!.applicationContext, gso)
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount)
    {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            firebaseAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(application!!.mainExecutor){ task ->

                    if (task.isSuccessful)
                    {
                        liveFirebaseUser.postValue(firebaseAuth!!.currentUser)

                        if(task.result.additionalUserInfo!!.isNewUser)
                        {
                            FirebaseDBManager.createUser(firebaseAuth!!.currentUser!!)
                        }
                    }
                    else
                    {
                        errorStatus.postValue(true)
                    }
                }
        }
    }

    fun logout()
    {
        firebaseAuth!!.signOut()
        googleSignInClient.value!!.signOut()
        loggedOut.postValue(true)
        errorStatus.postValue(false)
    }
}