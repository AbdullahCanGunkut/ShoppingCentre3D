package com.example.shoppingcentre3d.Helpers

import com.example.shoppingcentre3d.ErrorHandling.ShoppingCentreErrorHolder
import com.example.shoppingcentre3d.ErrorHandling.ErrorCode
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.lang.Exception
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.bumptech.glide.Glide
import retrofit2.awaitResponse
import java.net.URISyntaxException
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient

import java.net.URL;
import java.net.URLConnection;

//Bu package'de yardımcı fonksiyonlar ve class'lar bulabilirsiniz işinizi hızlandıracak.

//ModelFileMetaData key value'lerini dynamic olarak tutatlım yani amaç json'a çevirirken herhangi bir objeyi serialize edebilelim.
typealias ModelFileMetaData = HashMap<String, Any>
typealias ModelFileMetaDataMutable = MutableMap<String, Any>

/**
 * Eğer ilk olarak bir dosyayı analiz etmek istiyorsanız  json olarak içindeki bilgileri (Tabi sistem tarafından belirtilmiş düzene göre) bu fonksiyonu kullanın.
 * Dosyada şu alt dosya muhakak olmalı : images -> tüm resimler buraya atılmalı.
 */
class ShoppingCentre3DHelpers {

    companion object {
/*        fun createModelFileMetaData(directoryPath: String): ShoppingCentreErrorHolder {
            val map: ModelFileMetaDataMutable = mutableMapOf()

            try {
                val directory = File(directoryPath)

                if (directory.exists() && directory.isDirectory) {
                    val filesAndSubdirectories = directory.listFiles()

                    map["productId"] = directory.name

                    var imgrgx = ".*[png|jpg|jpeg|]".toRegex();
                    var imgLst: MutableList<String> =
                        mutableListOf()//Image'lerin local path'ini tutan stringl list.

                    filesAndSubdirectories?.forEach { file ->
                        if (file.isDirectory)
                            when (file.name.lowercase()) {
                                "images" -> {
                                    file.listFiles()?.forEach { file ->
                                        if (file.name.contains(imgrgx))
                                            imgLst.add("images/${file.name}")
                                    }
                                }
                            }
                    }
                    map["images"] = imgLst

                } else {
                    println("Belirtilen dizin bulunamadı veya bir dizin değil.")
                }
            } catch (e: Exception) {
                return ShoppingCentreErrorHolder(
                    errorCode = ErrorCode.Exception,
                    what = "fun createModelFileJson(): " + e.message ?: ""
                )
            }
            return ShoppingCentreErrorHolder(returnValue = map)
        }
*/

        /**
         *  Dinamik olarak bir Retrofit build edip ve belirtilen InterfaceType'a göre bir class oluşturur.
         * */
        inline fun <reified InterfaceType> downloadFileWithRetrofit(baseUrl: String): InterfaceType {

            val interceptor = HttpLoggingInterceptor()
            interceptor.level =
                HttpLoggingInterceptor.Level.BASIC // Log seviyesini BODY olarak ayarla

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

            val gson = GsonBuilder()
                .create()

            return Retrofit.Builder()   .client(client)
                .baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(InterfaceType::class.java)
        }


        /**
        *  İnternet üzerinden ByteArray olarak dosya indirmek için kullanacağız.
        * */

        fun downloadFile(fileUrl: String): Deferred<ShoppingCentreErrorHolder> =
            CoroutineScope(Dispatchers.IO).async {
                try {
                    URL(fileUrl).toURI()
                } catch (e: URISyntaxException) {
                    return@async ShoppingCentreErrorHolder(
                        errorCode = ErrorCode.Exception,
                        e.message ?: ""
                    )
                }
                var url = URL(fileUrl)
                return@async ShoppingCentreErrorHolder(returnValue = url.readBytes())
            }

        /**
         *  ByteArray olark asenkron bir şekilde dosya kayıt etmek için kullnacağız.
         * */
        fun saveFile(path: String, bytes: ByteArray): Deferred<ShoppingCentreErrorHolder> =
            CoroutineScope(Dispatchers.IO).async {
                try {
                    val file = File(path)
                    file.writeBytes(bytes)
                    return@async ShoppingCentreErrorHolder()
                } catch (e: Exception) {
                    println("Dosya indirme hatası: ${e.message}")
                    return@async ShoppingCentreErrorHolder(
                        ErrorCode.Exception,
                        what = "fun saveFile() : ${e.message}"
                    )
                }
            }

        /**
         * Bir metni daha iyi okunabilir hale getirir ve gereksiz boşlukları siler.
         * */
        inline fun alignTextAsSingleSpace(str: String): String =
            str.trim().replace("\\s+".toRegex(), " ")

        /**
         * Byte arrayı bitmape dönüştürür.
         * */
        inline fun bitmapFromByteArray(byteArray: ByteArray): Bitmap {
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }


    }
}
