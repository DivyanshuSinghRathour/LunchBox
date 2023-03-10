package com.intershala.myapplication.activity

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.intershala.myapplication.R

import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private lateinit var fullNameTIL: TextInputLayout
    private lateinit var emailTIL: TextInputLayout
    private lateinit var studentIDTIL: TextInputLayout
    private lateinit var mobileNumberTIL: TextInputLayout
    private lateinit var createPasswordTIL: TextInputLayout
    private lateinit var confirmPasswordTIL: TextInputLayout


    private lateinit var agreeCheckBox: CheckBox
    private lateinit var registerBtn: Button

    private lateinit var registerProgressDialog: ProgressDialog

    private var doubleBackToExit = false
    override fun onBackPressed() {
        if(doubleBackToExit) {
            super.onBackPressed()
            return
        }
        doubleBackToExit = true
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExit = false }, 2000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("students")

        fullNameTIL = findViewById(R.id.register_full_name_til)
        emailTIL = findViewById(R.id.register_email_til)
        studentIDTIL = findViewById(R.id.register_student_id_til)
        mobileNumberTIL = findViewById(R.id.register_mobile_num_til)
        createPasswordTIL = findViewById(R.id.register_create_password_til)
        confirmPasswordTIL = findViewById(R.id.register_confirm_password_til)

        agreeCheckBox = findViewById(R.id.register_agree_check_box)
        registerBtn = findViewById(R.id.register_student_btn)

        agreeCheckBox.setOnClickListener {
            registerBtn.isEnabled = agreeCheckBox.isChecked
        }

        registerProgressDialog = ProgressDialog(this)
        registerBtn.setOnClickListener { registerStudent() }
    }


    private fun validateName(): Boolean {
        val fullName = fullNameTIL.editText!!.text.toString().trim()
        if(fullName.isEmpty()) {
            fullNameTIL.error = getString(R.string.field_empty)
            return false
        }
        fullNameTIL.error = null
        return true
    }

    private fun validateEmail(): Boolean {
        val email = emailTIL.editText!!.text.toString().trim()
        if(email.isEmpty()) {
            emailTIL.error = getString(R.string.field_empty)
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTIL.error = getString(R.string.invalid_email)
            return false
        }
        emailTIL.error = null
        return true
    }


    private fun validateStudentID(): Boolean {
        val studentId = studentIDTIL.editText!!.text.toString().trim()
        if(studentId.isEmpty()) {
            studentIDTIL.error = getString(R.string.field_empty)
            return false
        }
        studentIDTIL.error = null
        return true
    }

    private fun validateMobileNum(): Boolean {
        val mobileNum = mobileNumberTIL.editText!!.text.toString().trim()
        if(mobileNum.isEmpty()) {
            mobileNumberTIL.error = getString(R.string.field_empty)
            return false
        }
        if(mobileNum.length < 10) {
            mobileNumberTIL.error = getString(R.string.invalid_mobile_no)
            return false
        }
        mobileNumberTIL.error = null
        return true
    }

    private fun validatePassword(): Boolean {
        createPasswordTIL.error = null
        confirmPasswordTIL.error = null

        val createPass = createPasswordTIL.editText!!.text.toString().trim()
        val confirmPass = confirmPasswordTIL.editText!!.text.toString().trim()

        if(createPass.isEmpty()) { createPasswordTIL.error = getString(R.string.field_empty) }
        if(confirmPass.isEmpty()) { confirmPasswordTIL.error = getString(R.string.field_empty) }
        if(createPass.isEmpty() || confirmPass.isEmpty()) return false

        if(createPass.length < 6) {
            createPasswordTIL.error = "Password is too short (Min. 6 Characters)"
            return false
        }
        if(createPass != confirmPass) {
            confirmPasswordTIL.error = "Password don't match"
            return false
        }
        createPasswordTIL.error = null
        confirmPasswordTIL.error = null
        return true
    }

    private fun registerStudent() {
        if(!validateName() or !validateEmail()  or !validateStudentID() or !validateMobileNum() or !validatePassword())
            return

        val email = emailTIL.editText!!.text.toString()
        val password = confirmPasswordTIL.editText!!.text.toString()
        val name = fullNameTIL.editText!!.text.toString()

        registerProgressDialog.setTitle("Registering...")
        registerProgressDialog.setMessage("We are creating your account")
        registerProgressDialog.show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    val currentUser = auth.currentUser
                    val profileUpdates = userProfileChangeRequest { displayName = name }

                    currentUser!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { it ->
                            if(it.isSuccessful) {
                                //Name Updated
                                addStudentsDetailsToDatabase(currentUser)
                            }
                        }
                } else {
                    showDialog("Registration Failed", task.exception.toString())
                }
            }
    }


    private fun addStudentsDetailsToDatabase(user: FirebaseUser) {
        registerProgressDialog.setMessage("Uploading details to database")

        val stdIDNo = studentIDTIL.editText!!.text.toString()
        val mobileNo = mobileNumberTIL.editText!!.text.toString()

        val students = databaseRef.child(user.uid)
        students.child("student_name").setValue(fullNameTIL.editText!!.text.toString().trim())
        students.child("student_id").setValue(stdIDNo)
        students.child("mobile_no").setValue(mobileNo)
        students.child("gender").setValue("none")
        students.child("reg_id").setValue(getRegID())
        students.child("reg_date").setValue(getRegDate())

        sendEmailVerification(user)
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { it ->
                if(it.isSuccessful) {
                    AlertDialog.Builder(this)
                        .setTitle("Verify e-mail address")
                        .setMessage("Registered Successfully !\nA verification link has been sent to your Email address")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                            startActivity(Intent(this, LoginUserActivity::class.java))
                            finish()
                        })
                        .setCancelable(false)
                        .create()
                        .show()
                }
            }
            .addOnFailureListener { t ->
                showDialog("Verification Link", t.message.toString())
            }
    }

    private fun getRegDate(): String {
        val c = Calendar.getInstance()
        val monthName = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val dayNumber = c.get(Calendar.DAY_OF_MONTH)
        val year = c.get(Calendar.YEAR)
        return "%02d-${monthName.substring(0, 3)}-$year".format(dayNumber)
    }

    private fun getRegID(): String {
        val stdName = fullNameTIL.editText!!.text.toString().split(" ")
        val r1 = if(stdName.size == 2) {
            "${stdName[0][0]}${stdName[1][0]}".toUpperCase(Locale.ROOT) //first character of first name and last name
        } else {
            "${stdName[0][0]}${stdName[0][stdName[0].length - 1]}".toUpperCase(Locale.ROOT) //first and last character of first name
        }
        val r2: Int = (10000..99999).random()
        return "$r1$r2"
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            .setCancelable(false)
            .create()
            .show()
        registerProgressDialog.dismiss()
    }

    fun openLoginActivity(view: View) {
        startActivity(Intent(this, LoginUserActivity::class.java))
        finish()
    }
}