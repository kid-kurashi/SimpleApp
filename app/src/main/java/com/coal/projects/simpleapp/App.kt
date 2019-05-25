package com.coal.projects.simpleapp

import android.app.Application
import android.net.Uri
import android.widget.ImageView
import com.coal.projects.chat.creation.ChatInstance
import com.coal.projects.chat.NotificationHelper
import com.coal.projects.chat.ImageLoader
import com.coal.projects.chat.creation.ChatIcons
import com.coal.projects.chat.data.FirebaseRepository
import com.coal.projects.chat.data.SharedPreferecesManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class App : Application() {

    private lateinit var chatInstance: ChatInstance

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        /*Поучему синглтоны? да потому что... Разные конексты у модулей, ресурсы тоже
        * Аппликейшен не заинжектить нормально. так что вот*/
        chatInstance = ChatInstance(
            FirebaseRepository(FirebaseFirestore.getInstance()),
            SharedPreferecesManager(this),
            NotificationHelper(this),
            getImageLoader(),
            getString(R.string.server_key)
        )
        ChatIcons.drawableIcNotification = R.drawable.logo
        ChatIcons.mipmapIcNotification = R.mipmap.ic_launcher
    }

    private fun getImageLoader(): ImageLoader? {
        return object : ImageLoader {
            override fun loadImageInto(url: String?, target: Target?) {
                picasso.load(url).into(target)
            }

            override fun loadImageInto(url: String?, imageView: ImageView?) {
                picasso.load(url).into(imageView)
            }

            override fun invalidateCache(url: String?) {

            }

            override fun loadImageInto(uri: Uri?, imageView: ImageView?) {
                picasso.load(uri).into(imageView)
            }

            override fun loadImageInto(uri: Uri?, target: Target?) {
                picasso.load(uri).into(target)
            }

            override fun getPicasso(): Picasso {
                return Picasso.with(this@App)
            }
        }
    }
}