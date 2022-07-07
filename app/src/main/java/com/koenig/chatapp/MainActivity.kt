package com.koenig.chatapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.koenig.chatapp.databinding.ActivityMainBinding
import com.koenig.chatapp.databinding.NavHeaderMainBinding
import com.koenig.chatapp.firebase.FirebaseImageManager
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.auth.LoginActivity
import com.koenig.chatapp.ui.chatManager.ChatViewModel
import com.koenig.chatapp.ui.mapManager.MapsViewModel
import com.koenig.chatapp.ui.settingsManager.SettingsViewModel
import com.koenig.chatapp.utils.checkLocationPermissions
import com.koenig.chatapp.utils.isPermissionGranted


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var toolbar: ActionBar
    private  lateinit var loggedInViewModel: LoggedInViewModel
    private  lateinit var navHeaderBinding: NavHeaderMainBinding
    private lateinit var headerView : View
    private val mapsViewModel: MapsViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    // NOTIFICATION
    private lateinit var notificationManager: NotificationManager
    private val channelId = "com.koenig.chatapp.notifications"
    private val description = "NotificationMessage"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // NOTIFICATION
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // BINDING
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        toolbar = supportActionBar!!
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.profileFragment,
                R.id.friendRequestFragment,
                R.id.chatOverviewFragment,
                R.id.settingsFragment,
                R.id.aboutFragment
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        initProfileHeader()

        if(checkLocationPermissions(this))
        {
            mapsViewModel.updateCurrentLocation()
        }
    }

    @SuppressLint("RemoteViewLayout")
    public override fun onStart() {
        super.onStart()

        loggedInViewModel = ViewModelProvider(this)[LoggedInViewModel::class.java]

        // User logged in => Update the profile
        loggedInViewModel.liveFirebaseUser.observe(this) { firebaseUser ->
            if (firebaseUser != null) {
                updateProfile(firebaseUser)
                // MESSAGE RECEIVE
                chatViewModel.receiveMessageForUser(firebaseUser.uid)
                settingsViewModel.getNotificationEnabled(firebaseUser.uid)
            }
        }
        // User logged out => Navigate to login page
        loggedInViewModel.loggedOut.observe(this) { loggedOut ->
            if (loggedOut) {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        chatViewModel.observableMessageForUser.observeForever {
            // Just add notification when message wasn't read and when notifications are enabled
            if(!it.wasRead && settingsViewModel.areNotificationsEnabled.value!!)
            {
                addNotification(it.fromUserName, it.message)
            }
        }
    }

    fun signOut(item: MenuItem)
    {
        loggedInViewModel.logOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private  fun initProfileHeader()
    {
        headerView = binding.navView.getHeaderView(0)
        navHeaderBinding = NavHeaderMainBinding.bind(headerView)
    }

    private fun updateProfile(currentUser: FirebaseUser)
    {
        navHeaderBinding.textUserMail.text = currentUser.email
        navHeaderBinding.textUserName.text = currentUser.displayName

        FirebaseImageManager.imageUri.observe(this) { result ->
            if (result == Uri.EMPTY)
            {
                if (currentUser.photoUrl != null)
                {
                    FirebaseImageManager.updateUserImage(
                        currentUser.uid,
                        currentUser.photoUrl,
                        navHeaderBinding.imageUser,
                        false
                    )
                }
                else
                {
                    FirebaseImageManager.updateDefaultImage(
                        currentUser.uid,
                        R.drawable.empty_profile,
                        navHeaderBinding.imageUser
                    )
                }
            }
            else
            {
                FirebaseImageManager.updateUserImage(
                    currentUser.uid,
                    FirebaseImageManager.imageUri.value,
                    navHeaderBinding.imageUser,
                    false
                )
            }
        }
    }

    private fun addNotification(fromUserName: String, message: String)
    {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(fromUserName)
            .setSmallIcon(R.drawable.ic_notification_message)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationIntent = Intent(this, MainActivity::class.java)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)

        val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Message Channel"
            val descriptionText = description
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            manager.createNotificationChannel(channel)
            manager.notify(0, builder.build())
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isPermissionGranted(requestCode, grantResults))
        {
            // Set current location in database
            mapsViewModel.updateCurrentLocation()
            mapsViewModel.setLocationPermission(loggedInViewModel.liveFirebaseUser.value!!.uid, true)
            mapsViewModel.setMapEnabled(loggedInViewModel.liveFirebaseUser.value!!.uid, true)

            mapsViewModel.currentLocation.observe(this, object: Observer<Location> {
                override fun onChanged(t: Location?) {
                    mapsViewModel.setUserLocation(
                        loggedInViewModel.liveFirebaseUser.value!!.uid,
                        t!!.latitude,
                        t.longitude)

                    mapsViewModel.currentLocation.removeObserver(this)
                }
            })
        }
        else {
            // Disable map in database
            mapsViewModel.setLocationPermission(loggedInViewModel.liveFirebaseUser.value!!.uid, false)
            mapsViewModel.setMapEnabled(loggedInViewModel.liveFirebaseUser.value!!.uid, false)
        }
    }
}
