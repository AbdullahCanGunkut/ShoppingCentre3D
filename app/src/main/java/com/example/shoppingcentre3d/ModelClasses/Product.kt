package com.example.shoppingcentre3d.ModelClasses
import  com.example.shoppingcentre3d.Helpers.ConciseDateObject
import java.time.LocalDateTime


/**
 * Ürün bilgilerini tutan model class'ımız.
 * */
data class Product(
    val productId: String = "",
    val productName: String = "",
    val price: Float = 1f,
    val discountPrice: Float = 0f,
    val tags:  List<String> = listOf(),
    val category: String = "", //String kategori adı , Int ise onun indexini temsil ediyor
    val informationDate : String = "", //Ürün bilgileri güncellendiğinide anlık olarak o tarihi alır.
    val imageNames: List<ConciseDateObject<String , String>> = listOf(),
    var thumbnailDate: String = "", //Thumbnail'in güncellenme zamanını gösterir.
    var modelDate: String = "",
    var description : String = "" // Ürün açıklaması
    , //Modelin güncellenme zamanını gösterir.
    @Transient var modelFile: ModelFile? = null //Bunun serialize edilmeye ihtiyacı yok çünkü runtime'da dinamik ve özel olarak oluşturuluyor. FileManagment tarafından kullanılıyor.

    ){

}