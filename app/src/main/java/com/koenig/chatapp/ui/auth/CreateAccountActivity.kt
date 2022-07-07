package com.koenig.chatapp.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.koenig.chatapp.MainActivity
import com.koenig.chatapp.databinding.ActivityCreateAccountBinding

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var loginRegisterViewModel: LoginRegisterViewModel
    private  lateinit var createAccountBinding: ActivityCreateAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginRegisterViewModel = ViewModelProvider(this)[LoginRegisterViewModel::class.java]

        createAccountBinding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(createAccountBinding.root)

        createAccountBinding.emailCreateAccountButton.setOnClickListener {
            createAccount(createAccountBinding.textEmail.text.toString(), createAccountBinding.textPassword.text.toString(), createAccountBinding.textUserName.text.toString())
        }
    }

    override fun onStart() {
        super.onStart()

        loginRegisterViewModel.liveFirebaseUser.observe(this) { firebaseUser ->
            if (firebaseUser != null)
                startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun createAccount(email: String, password: String, userName: String)
    {
        if(!validateEmail() || !validatePassword() || !validateUserName()){ return }

        loginRegisterViewModel.register(email, password, userName)
        createAccountBinding.layoutBlurry.visibility = View.VISIBLE

    }

    private fun validateEmail(): Boolean
    {
        var valid = true
        val email = createAccountBinding.textEmail.text.toString()

        if (TextUtils.isEmpty(email))
        {
            createAccountBinding.textEmail.error = "Required."
            valid = false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            createAccountBinding.textEmail.error = "No valid email"
            valid = false
        }
        else
        {
            createAccountBinding.textEmail.error = null
        }

        return  valid
    }

    private fun validatePassword(): Boolean
    {
        var valid = true
        val password = createAccountBinding.textPassword.text.toString()

        if (TextUtils.isEmpty(password))
        {
            createAccountBinding.textPassword.error = "Required."
            valid = false
        }
        else
        {
            createAccountBinding.textPassword.error = null
        }
        return  valid
    }

    private fun validateUserName(): Boolean
    {
        var valid = true
        val userName = createAccountBinding.textUserName.text.toString()

        if (TextUtils.isEmpty(userName))
        {
            createAccountBinding.textPassword.error = "Required."
            valid = false
        }
        else
        {
            createAccountBinding.textUserName.error = null
        }
        return  valid
    }
}