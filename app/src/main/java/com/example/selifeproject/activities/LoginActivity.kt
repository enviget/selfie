package com.example.selifeproject.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.selifeproject.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        var isLoggedIn = FirebaseAuth.getInstance().currentUser
        if(isLoggedIn != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()}
        init()
    }

    private fun init() {
        button_login_login.setOnClickListener(this)
        text_view_login_noacc.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view){

            //login button
            button_login_login -> {
                var email = edit_text_login_email.text.toString()
                var password = edit_text_login_password.text.toString()

                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(object:OnCompleteListener<AuthResult>{
                    override fun onComplete(task: Task<AuthResult>) {
                        if(task.isSuccessful){
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    }

                }).addOnFailureListener(object:OnFailureListener{
                    override fun onFailure(exception: Exception) {
                        Toast.makeText(applicationContext,exception.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }

            //no account textbutton
            text_view_login_noacc -> {startActivity(Intent(this, RegisterActivity::class.java))
            finish()}
        }
    }
}