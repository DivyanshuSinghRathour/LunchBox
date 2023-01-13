package com.intershala.myapplication.services

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

class StorageServices {
    private val firebaseStorage = FirebaseStorage.getInstance()

    private val menuItems = "foodMenuItems"

    fun getItemUrl(itemId: String) : String {
        val imgPath = "$menuItems/$itemId"
        val encoded = Uri.encode(imgPath)
        return "https://firebasestorage.googleapis.com/v0/b/${firebaseStorage.reference.bucket}/o/$encoded?alt=media"
    }

    fun uploadImage() {
        // Upload to Storage
    }
}