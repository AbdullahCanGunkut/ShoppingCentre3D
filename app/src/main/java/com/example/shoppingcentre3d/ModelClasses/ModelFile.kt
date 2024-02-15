package com.example.shoppingcentre3d.ModelClasses

import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.shoppingcentre3d.Helpers.*
import java.time.LocalDateTime
import android.graphics.Bitmap



/**
 * Ürün dosyalarını tutan model class'ımız.
 * */

class ModelFile(
    val path: String = "",
    val productId: String = "",
    var modelAvailableOnCache : Boolean = false,//Eğer model dosyası cache klasöründe var ise ozaman oradan hızlı bir şekilde internete uğramadan çekeceğiz.
    var images: MutableList<ConciseDateObject<String , Bitmap >>? = null, //Modelin gerçek hayattaki resimlerini tutar.
    var thumbnail: ConciseDateObject<String , Bitmap>? = null //Thumbnail resmi verisini tutar
) {

}