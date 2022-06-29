package com.koenig.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
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
import com.koenig.chatapp.ui.mapManager.MapsViewModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                R.id.chatOverviewFragment
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

    public override fun onStart() {
        super.onStart()

        loggedInViewModel = ViewModelProvider(this)[LoggedInViewModel::class.java]

        // User logged in => Update the profile
        loggedInViewModel.liveFirebaseUser.observe(this) { firebaseUser ->
            if (firebaseUser != null) {
                updateProfile(firebaseUser)
            }
        }
        // User logged out => Navigate to login page
        loggedInViewModel.loggedOut.observe(this) { loggedOut ->
            if (loggedOut) {
                startActivity(Intent(this, LoginActivity::class.java))
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
            mapsViewModel.setMapEnabled(loggedInViewModel.liveFirebaseUser.value!!.uid, false)
        }
    }
}