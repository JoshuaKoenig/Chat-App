package com.koenig.chatapp.firebase

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.koenig.chatapp.models.UserModel
import com.koenig.chatapp.models.UserStore

object FirebaseDBManager: UserStore {

    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun getUserById(userId: String, user: MutableLiveData<UserModel>) {

        database.child("users").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user.value = snapshot.getValue<UserModel>()
                database.child("users").child(userId).removeEventListener(this)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun createUser(firebaseUser: FirebaseUser) {

        val user = UserModel()
        val userId = firebaseUser.uid

        user.email = firebaseUser.email.toString()
        user.userName = firebaseUser.displayName.toString()
        user.userId = userId
        user.status = "Hello i'm new here"
        user.photoUri = firebaseUser.photoUrl.toString()

        val userValues = user.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/users/$userId"] = userValues

        database.updateChildren(childAdd)
    }

    override fun updateUser(userId: String, user: UserModel) {

        val userValues = user.toMap()

        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["users/$userId"] = userValues

        database.updateChildren(childUpdate)
    }

    override  fun updateUserImage(userId: String, photoUri: Uri)
    {
        database.child("users").child(userId).child("photoUri").setValue(photoUri.toString())
    }

    override fun addContact(userId: String, user: UserModel) {

        // TODO

    }


}