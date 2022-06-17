package com.koenig.chatapp

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.koenig.chatapp.databinding.ActivityMainBinding
import com.koenig.chatapp.databinding.NavHeaderMainBinding
import com.koenig.chatapp.firebase.FirebaseImageManager
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.auth.LoginActivity
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var toolbar: ActionBar
    private  lateinit var loggedInViewModel: LoggedInViewModel
    private  lateinit var navHeaderBinding: NavHeaderMainBinding
    private lateinit var headerView : View

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
                R.id.contactsFragment,
                R.id.profileFragment
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        initProfileHeader()
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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}