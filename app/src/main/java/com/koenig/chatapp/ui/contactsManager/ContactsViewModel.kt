package com.koenig.chatapp.ui.contactsManager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koenig.chatapp.firebase.FirebaseDBManager
import com.koenig.chatapp.models.ContactModel

class ContactsViewModel: ViewModel() {

    val contacts = MutableLiveData<List<ContactModel>>()

    val observableContacts: LiveData<List<ContactModel>>
        get() = contacts

    fun loadAllContacts(currentUserId: String, isSelectMode: Boolean, groupContactIds: ArrayList<String>?)
    {
        try {
            FirebaseDBManager.getAllContactsForUser(currentUserId, contacts, isSelectMode, groupContactIds)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }
}