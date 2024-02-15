package com.example.shoppingcentre3d.ModelClasses
import android.graphics.Bitmap
import com.google.android.filament.gltfio.AssetLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import  com.example.shoppingcentre3d.Helpers.ConciseDateObject
import java.lang.reflect.Constructor
import java.time.LocalDateTime
import java.util.Date

/**
 * Adres bilgilerini tutan model class'ımız.
 * */
data class Address(
    val title : String = "",
    val name : String = "",
    val surname : String = "",
    val address : String = "",
    val telephone : String = ""
    ){
}