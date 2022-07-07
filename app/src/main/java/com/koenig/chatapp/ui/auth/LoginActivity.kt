package com.koenig.chatapp.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.koenig.chatapp.MainActivity
import com.koenig.chatapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var loginRegisterViewModel: LoginRegisterViewModel
    private  lateinit var loginBinding: ActivityLoginBinding
    private  lateinit var startForResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginRegisterViewModel = ViewModelProvider(this).get(LoginRegisterViewModel::class.java)


        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        // Default sign in
        loginBinding.emailSignInButton.setOnClickListener {
            signIn(loginBinding.fieldEmail.text.toString(), loginBinding.fieldPassword.text.toString())
        }

        // Create Account
        loginBinding.emailCreateAccountButton.setOnClickListener {
           val intent = Intent(this, CreateAccountActivity::class.java)
           startActivity(intent)
        }

        // Google Button UI
        loginBinding.googleSignInButton.setSize(SignInButton.SIZE_ICON_ONLY)
        loginBinding.googleSignInButton.setColorScheme(1)

        // Google login
        loginBinding.googleSignInButton.setOnClickListener {
            googleSignIn()
        }
    }

    public override fun onStart() {
        super.onStart()

        loginRegisterViewModel.liveFirebaseUser.observe(this) { firebaseUser ->
            if (firebaseUser != null)
                startActivity(Intent(this, MainActivity::class.java))
        }

        loginRegisterViewModel.firebaseAuthManager.errorStatus.observe(this
        ) { status -> checkStatus(status) }

        setupGoogleSignInCallback()
    }

    private  fun signIn(email: String, password: String)
    {
        if(!validateEmail() || !validatePassword()){ return }

        loginRegisterViewModel.login(email, password)
        loginBinding.layoutBlurry.visibility = View.VISIBLE
    }

    private fun googleSignIn()
    {
        val signInIntent = loginRegisterViewModel.firebaseAuthManager
            .googleSignInClient.value!!.signInIntent

        startForResult.launch(signInIntent)
        loginBinding.layoutBlurry.visibility = View.VISIBLE
    }

    private fun setupGoogleSignInCallback()
    {
        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            when(result.resultCode)
            {
                RESULT_OK ->
                {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try{
                        val account = task.getResult(ApiException::class.java)
                        loginRegisterViewModel.authWithGoogle(account!!)
                    }
                    catch (e: ApiException)
                    {
                        Snackbar.make(loginBinding.loginLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    }
                }

                RESULT_CANCELED ->
                {
                    // TODO: Handle Cancellation
                }

                else ->
                {
                    // TODO: Handle other cases
                }
            }
        }
    }


    private fun validateEmail(): Boolean
    {
        var valid = true
        val email = loginBinding.fieldEmail.text.toString()

        if (TextUtils.isEmpty(email))
        {
            loginBinding.fieldEmail.error = "Required."
            valid = false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            loginBinding.fieldEmail.error = "No valid email"
            valid = false
        }
        else
        {
            loginBinding.fieldEmail.error = null
        }

        return  valid
    }

    private fun validatePassword(): Boolean
    {
        var valid = true
        val password = loginBinding.fieldPassword.text.toString()

        if (TextUtils.isEmpty(password))
        {
            loginBinding.fieldPassword.error = "Required."
            valid = false
        }
        else
        {
            loginBinding.fieldPassword.error = null
        }

        return  valid
    }

    private fun checkStatus(error:Boolean) {
        // TODO: Handle Error
        if (error)
            Toast.makeText(this,
                "Error",
                Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Toast.makeText(this,"Click again to Close App...",Toast.LENGTH_SHORT).show()
        finish()
    }
}