package com.intershala.myapplication.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.intershala.myapplication.R
import com.intershala.myapplication.activity.GenderSelectionActivity
import com.intershala.myapplication.activity.MainActivity
import com.intershala.myapplication.activity.RegisterActivity
import com.intershala.myapplication.datamodels.MenuItem
import com.intershala.myapplication.fragment.ForgotPassword
import org.w3c.dom.Text


class PotentialCustomer: AppCompatActivity() {
     private lateinit var auth: FirebaseAuth
     private lateinit var databaseRef: DatabaseReference

     private lateinit var emailTIL: TextInputLayout
     private lateinit var passwordTIL: TextInputLayout

     lateinit var buyer: TextView
     lateinit var studentName:TextView
     lateinit var studentPhone: TextView
     lateinit var studentGender: TextView
     lateinit var studentRegistrationId: TextView

     private var doubleBackToExit =false
     override fun onBackPressed() {
         if(doubleBackToExit){
             super.onBackPressed()
             return
         }
         doubleBackToExit= true
         Toast.makeText(this,"Press Again To Exit", Toast.LENGTH_SHORT).show()
         Handler().postDelayed({doubleBackToExit=false},2000)
     }
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_login_user)

         auth = FirebaseAuth.getInstance()
         databaseRef= FirebaseDatabase.getInstance().reference

         emailTIL=findViewById(R.id.login_email_til)
         passwordTIL=findViewById(R.id.login_password_til)

         if(buyer!=null){
             studentName=findViewById(R.id.item_name)
             studentPhone=findViewById(R.id.PhoneNumber)
             studentGender=findViewById(R.id.gender_name_tv)
             studentRegistrationId=findViewById(R.id.register_student_id_til)
         intent.getStringExtra("studentName")
         intent.getStringExtra("studentPhone")
         intent.getStringExtra("studentGender")
         intent.getStringExtra("studentRegistrationId")
         }
         else{
             return
         }
     }
     override fun onStart(){
         super.onStart()
         val user = auth.currentUser
         if(user!= null && user.isEmailVerified){
             startActivity(Intent(this, MainActivity::class.java))
             finish()
         }
     }
 }