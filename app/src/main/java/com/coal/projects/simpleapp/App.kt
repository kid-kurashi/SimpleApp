package com.coal.projects.simpleapp

import android.app.Application
import com.coal.projects.chat.ChatInstance
import com.coal.projects.chat.ChatNotificationHelper
import com.coal.projects.chat.data.FirebaseRepository
import com.coal.projects.chat.data.SharedPreferecesManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        ChatInstance.firebaseRepository = FirebaseRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance()
        )
        ChatInstance.sharedPreferecesManager = SharedPreferecesManager(this)
        ChatInstance.chatNotificationHelper = ChatNotificationHelper(this)
    }
}