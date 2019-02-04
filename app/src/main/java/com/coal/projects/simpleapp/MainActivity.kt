package com.coal.projects.simpleapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.coal.projects.chat.presentation.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.client_id)
        setContentView(R.layout.activity_main)

        openChat.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
