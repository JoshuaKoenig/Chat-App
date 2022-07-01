package com.koenig.chatapp.ui.settingsManager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koenig.chatapp.firebase.FirebaseDBManager

class SettingsViewModel: ViewModel() {

    val areNotificationsEnabled = MutableLiveData<Boolean>()

    var observableNotifications: LiveData<Boolean>
        get() = areNotificationsEnabled
        set(value) {areNotificationsEnabled.value = value.value}


    fun getNotificationEnabled(userId: String)
    {
        FirebaseDBManager.areNotificationsEnabled(userId, areNotificationsEnabled)
    }

    fun setNotificationEnabled(userId: String, areNotificationsEnabled: Boolean)
    {
        try {
            FirebaseDBManager.setAreNotificationsEnabled(userId, areNotificationsEnabled)
        }
        catch (e: Exception)
        {
            // TODO
        }
    }
}