package com.koenig.chatapp.firebase

import android.app.Application
import android.os.Build
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.koenig.chatapp.R

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

    fun register(email: String?, password: String?)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            firebaseAuth!!.createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(application!!.mainExecutor) {task ->
                    if (task.isSuccessful)
                    {
                        liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
                        errorStatus.postValue(false)
                    }
                    else
                    {
                        errorStatus.postValue(true)
                    }
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