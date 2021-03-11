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
import kotlinx.android.synthetic.main.activity_register.*
import java.lang.Exception

class RegisterActivity : AppCompatActivity(),View.OnClickListener {

    lateinit var auth :FirebaseAuth
    lateinit var user : FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        init()
    }

    private fun init() {
        button_register_register.setOnClickListener(this)
        text_view_register_haveacc.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view){

            button_register_register -> {
                var email = edit_text_register_email.text.toString()
                var password = edit_text_register_password.text.toString()

                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(object:OnCompleteListener<AuthResult>{
                    override fun onComplete(task: Task<AuthResult>) {
                        if(task.isSuccessful){
                            startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
                            finish()
                        }
                    }

                }).addOnFailureListener(object:OnFailureListener{
                    override fun onFailure(exception: Exception) {
                        Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
                    }

                })
            }

            text_view_register_haveacc ->{
                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }

        }
    }
}