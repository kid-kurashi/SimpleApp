package com.coal.projects.simpleapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.coal.projects.chat.NotificationHelper.CHAT_INNER_ACTION
import com.coal.projects.chat.creation.ChatInstance
import com.coal.projects.chat.data.ChatUser
import com.coal.projects.chat.presentation.chats.ChatsActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notifyScreen()
        ChatInstance.getInstance().firebaseRepository.initChatUser(
            ChatUser(
                FirebaseAuth.getInstance().currentUser!!.email,
                FirebaseAuth.getInstance().currentUser!!.displayName,
                FirebaseAuth.getInstance().currentUser!!.photoUrl!!.toString()
            )
        )

        setTitle(R.string.app_name)
        setContentView(R.layout.activity_main)

        openChat.setOnClickListener {
            startActivity(Intent(this, ChatsActivity::class.java))
        }
    }

    private fun notifyScreen() {
        val intent = intent
        intent.action = CHAT_INNER_ACTION
        sendBroadcast(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.coal.projects.chat.R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == com.coal.projects.chat.R.id.menu_signout) {
            FirebaseAuth.getInstance().signOut()
            finish()
        }
        return true
    }
}
