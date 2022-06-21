package com.koenig.chatapp.firebase

import android.net.Uri
import android.os.Debug
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
                Log.d("GetUserById", snapshot.getValue().toString())
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
        user.contacts = hashMapOf()

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

    override fun getAllUsers(userList: MutableLiveData<List<UserModel>>) {

        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               val localUserList = ArrayList<UserModel>()
                val users = snapshot.children

                users.forEach{
                    val userModel = it.getValue(UserModel::class.java)
                        localUserList.add(userModel!!)
                }

                database.child("users").removeEventListener(this)
                userList.value = localUserList
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun getAllContactsForUser(userId: String, contactList: MutableLiveData<List<UserModel>>) {
        database.child("users").child(userId).child("contacts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val localContactList = ArrayList<UserModel>()
                val contacts = snapshot.children

                contacts.forEach {
                    val contactUserModel = it.getValue(UserModel::class.java)
                    localContactList.add(contactUserModel!!)
                }

                database.child("users").child(userId).child("contacts").removeEventListener(this)

                contactList.value = localContactList
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun addContact(currentUserId: String, userAdd: UserModel) {

        val contactValues = userAdd.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/users/$currentUserId/contacts/${userAdd.userId}"] = contactValues

        database.updateChildren(childAdd)
    }


}