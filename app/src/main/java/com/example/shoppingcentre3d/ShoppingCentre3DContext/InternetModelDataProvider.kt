package com.example.shoppingcentre3d.ShoppingCentre3DContext

import android.provider.ContactsContract.Data
import android.view.Display.Mode
import com.bumptech.glide.Glide
import com.example.shoppingcentre3d.ErrorHandling.ErrorCode
import com.example.shoppingcentre3d.ErrorHandling.ShoppingCentreErrorHolder
import com.example.shoppingcentre3d.Helpers.ShoppingCentre3DHelpers
import com.example.shoppingcentre3d.ModelClasses.ModelFile
import com.example.shoppingcentre3d.ModelClasses.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.shoppingcentre3d.Helpers.ConciseDateObject
import java.time.LocalDateTime

import com.google.android.filament.Engine
import com.google.android.filament.gltfio.AssetLoader
import java.util.EnumSet
import java.io.*

typealias  ImageConciseDateObject = ConciseDateObject<String, Bitmap>


/**
 * Internet üzerinden herhangi bir yerden oluşturduğum düzene göre alabilirsiniz.
* Örnek olarak Githubdan model dosyalarına ulaşmak için eğer reponuzda dosya düzenine uygun bir product klasörü varsa oradan rahatlık ile çekilebilir.
*/
class InternetModelDataProvider(
    val baseUrl: String, val settings: EnumSet<ModelDataProviderSettings> =
        EnumSet.of(
            ModelDataProviderSettings.Images,
            ModelDataProviderSettings.Thumbnail
        )
) : DataProvider {
    //  override fun Get
    override  fun GetData(productMetaData: Product): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Default).async {//Return olarak

            var model = ModelFile(
                this@InternetModelDataProvider.baseUrl,
                productMetaData.productId,
                images = mutableListOf()
            )
            //Eğer resimler var ise belirtilen üründe ozaman onları yükleyecektir.
            if (this@InternetModelDataProvider.settings.contains(ModelDataProviderSettings.Images))
                for (i in productMetaData.imageNames) {

                    var error =
                        ShoppingCentre3DHelpers.downloadFile("${this@InternetModelDataProvider.baseUrl}/${productMetaData.productId}/images/${i.second}")
                            .await()
                    if (error.errorCode == ErrorCode.Success)
                        model.images?.add(
                            ImageConciseDateObject(
                                i.first,
                                ShoppingCentre3DHelpers.bitmapFromByteArray(error.returnValue as ByteArray)
                            )
                        )
                }
            //Eğer thumbnail var ise onuda yükleyecek.
            if (this@InternetModelDataProvider.settings.contains(ModelDataProviderSettings.Thumbnail)) {
                var error =
                    ShoppingCentre3DHelpers.downloadFile("${this@InternetModelDataProvider.baseUrl}/${productMetaData.productId}/thumbnail.png")
                        .await()


                if (error.errorCode == ErrorCode.Success)
                    model.thumbnail = ImageConciseDateObject(
                        productMetaData.thumbnailDate,
                        ShoppingCentre3DHelpers.bitmapFromByteArray(error.returnValue as ByteArray)
                    )
            }

            return@async ShoppingCentreErrorHolder(returnValue = model)
        }
    /**
     * Herhangi özel bir veri almak için kullanacağız ve burada veri tipini "settings" enumu belirleyecek ve ona göre özel veri return edecektir.
     * Şuan sadece özel olarak model dosyası alabiliyoruz.
     */
    override fun GetSpecifiedData(
        localPath: String,
        settings: EnumSet<ModelDataProviderSettings>
    ): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(
            Dispatchers.Default
        ).async {
            try {
                if (settings.contains(ModelDataProviderSettings.Model)) {
                    var file =
                        ShoppingCentre3DHelpers.downloadFile(this@InternetModelDataProvider.baseUrl + "/" + localPath + "/model.glb")
                            .await()
                    if (file.errorCode != ErrorCode.Success) return@async file

                    return@async ShoppingCentreErrorHolder(returnValue = file.returnValue )

                }
            } catch (e: Exception) {
                return@async ShoppingCentreErrorHolder(
                    errorCode = ErrorCode.Exception,
                    what = e.message ?: ""
                )
            }
            return@async ShoppingCentreErrorHolder(
                errorCode = ErrorCode.InvalidMember,
                what = "fun GetSpecifiedData(): invalid arguments  baseUrl : ${this@InternetModelDataProvider.baseUrl} ,local Path : ${localPath}"
            )
        }

}
