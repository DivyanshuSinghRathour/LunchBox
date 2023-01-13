package com.intershala.myapplication.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import com.intershala.myapplication.R
import com.intershala.myapplication.fragment.ForgotPassword


class LoginUserActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private lateinit var emailTIL: TextInputLayout
    private lateinit var passwordTIL: TextInputLayout

    private var doubleBackToExit =false
    override fun onBackPressed() {
        if(doubleBackToExit){
            super.onBackPressed()
            return
        }
        doubleBackToExit= true
        Toast.makeText(this,"Press Again To Exit",Toast.LENGTH_SHORT).show()
        Handler().postDelayed({doubleBackToExit=false},2000)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_user)

        auth = FirebaseAuth.getInstance()
        databaseRef=FirebaseDatabase.getInstance().reference

        emailTIL=findViewById(R.id.login_email_til)
        passwordTIL=findViewById(R.id.login_password_til)

        findViewById<TextView>(R.id.login_forgot_password_tv).setOnClickListener{userForgotPassword()}
    }

    override fun onStart(){
        super.onStart()
        val user = auth.currentUser
        if(user!= null && user.isEmailVerified){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun validateEmail():Boolean{
        val email = emailTIL.editText!!.text.toString().trim()
        if(email.isEmpty()){
            emailTIL.error="Field Can't Be Empty"
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailTIL.error="Invalid Email"
            return false
        }
        emailTIL.error=null
        return true
    }
    private fun validatePassword(): Boolean {

        val pass = passwordTIL.editText!!.text.toString().trim()

        if(pass.isEmpty()) {
            passwordTIL.error = getString(R.string.field_empty)
            return false
        }

        if(pass.length < 6) {
            passwordTIL.error = "Password is too short (Min. 6 Characters)"
            return false
        }

        passwordTIL.error = null
        return true
    }
    fun loginStudent(view: View) {
        if(!validateEmail() or !validatePassword())
            return

        findViewById<ProgressBar>(R.id.login_progress_bar).visibility = ViewGroup.VISIBLE
        auth.signInWithEmailAndPassword(emailTIL.editText!!.text.toString(), passwordTIL.editText!!.text.toString())
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    if(auth.currentUser!!.isEmailVerified) {
                        checkGenderSavedOrNot()
                    } else {
                        findViewById<ProgressBar>(R.id.login_progress_bar).visibility = ViewGroup.INVISIBLE
                        AlertDialog.Builder(this)
                            .setTitle("Email Verification")
                            .setMessage("Please verify your Email address.\nA verification link has been sent to your Email address")
                            .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, _ ->
                                dialogInterface.dismiss()
                            })
                            .setCancelable(false)
                            .create()
                            .show()
                    }
                }
            }

            .addOnFailureListener {
                AlertDialog.Builder(this)
                    .setTitle("Attention")
                    .setMessage("${it.message}")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    })
                    .setCancelable(false)
                    .create()
                    .show()
                findViewById<ProgressBar>(R.id.login_progress_bar).visibility = ViewGroup.INVISIBLE
            }

    }
    private fun checkGenderSavedOrNot() {
        val user = auth.currentUser!!
        val studentName = user.displayName!!

        databaseRef.child("students")
            .child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val studentGender = snapshot.child("gender").value.toString()
                    if(studentGender == "none") {
                        val intent = Intent(this@LoginUserActivity, GenderSelectionActivity::class.java)
                        intent.putExtra("name", studentName)
                        intent.putExtra("uid", user.uid)
                        startActivity(intent)
                        finish()
                    } else {
                        startActivity(Intent(this@LoginUserActivity, MainActivity::class.java))
                        finish()
                        Toast.makeText(this@LoginUserActivity, "Welcome to Cafy", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
    fun openRegisterActivity(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }

    private fun userForgotPassword() {
        val forgotDialog = ForgotPassword()
        forgotDialog.show(supportFragmentManager, "Forgot Password Dialog")
    }
}