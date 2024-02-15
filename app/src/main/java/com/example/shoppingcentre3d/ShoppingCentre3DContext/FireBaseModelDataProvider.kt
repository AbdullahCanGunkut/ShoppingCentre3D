package com.example.shoppingcentre3d.ShoppingCentre3DContext

import android.util.Log
import com.example.shoppingcentre3d.ErrorHandling.ErrorCode
import com.example.shoppingcentre3d.ErrorHandling.ShoppingCentreErrorHolder
import com.example.shoppingcentre3d.Helpers.ShoppingCentre3DHelpers
import com.example.shoppingcentre3d.ModelClasses.ModelFile
import com.example.shoppingcentre3d.ModelClasses.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.EnumSet


/**
*  Bunun InternetModelDataProvider'dan farkı sadece Firebase storage'dan ürün verileri çekmek için kullanılacak olması.
*  Member olarak baseUrl yani storage'ın başlangıç konumu local olarak örn : (deneme/dene/) gibi ve sonra baseUrl belirlendikten sonra yüklenecek veriler relative olarak yapılacak.
 *
 * */

class FireBaseModelDataProvider(
    val baseUrl: String, val settings: EnumSet<ModelDataProviderSettings> =
        EnumSet.of(
            ModelDataProviderSettings.Images,
            ModelDataProviderSettings.Thumbnail
        )
) : DataProvider {


    /**
     * InternetModelDataProvider ile aynı işleve sahip.
     * */
    override fun GetData(productMetaData: Product): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Default).async {//Return olarak

            var instance = ShoppingCentre3DContext.getInstance()

            if (instance == null || !instance.isInternetConnected()) {
                Log.i("ShoppingCenter3DError " ,  "ShoppingCentre3DContext instance is null or internet hasnt connected  !")
                return@async ShoppingCentreErrorHolder(
                    errorCode = ErrorCode.InternetConnectionError,
                    what = "ShoppingCentre3DContext instance is null or internet hasnt connected  !"
                )
            }

            var model = ModelFile(
                this@FireBaseModelDataProvider.baseUrl,
                productMetaData.productId,
                images = mutableListOf()
            )
            if (this@FireBaseModelDataProvider.settings.contains(ModelDataProviderSettings.Images))
                for (i in productMetaData.imageNames) {

                    var error =
                        instance!!.loadStorageFile("${this@FireBaseModelDataProvider.baseUrl}/${productMetaData.productId}/images/${i.second}")
                            .await()

                    if (error.errorCode == ErrorCode.Success)
                        model.images?.add(
                            ImageConciseDateObject(
                                i.first,
                                ShoppingCentre3DHelpers.bitmapFromByteArray(error.returnValue as ByteArray)
                            )
                        )
                    else {

                    }
                }
            if (this@FireBaseModelDataProvider.settings.contains(ModelDataProviderSettings.Thumbnail)) {
                var error =
                    instance!!.loadStorageFile("${this@FireBaseModelDataProvider.baseUrl}/${productMetaData.productId}/thumbnail.png")
                        .await()

                if (error.errorCode == ErrorCode.Success)
                    model.thumbnail = ImageConciseDateObject(
                        productMetaData.thumbnailDate,
                        ShoppingCentre3DHelpers.bitmapFromByteArray(error.returnValue as ByteArray)
                    )
                else{
                    Log.i("ShoppingCentre3DError" , "Thumnbail hasnt loaded ! ${this@FireBaseModelDataProvider.baseUrl}/${productMetaData.productId}/thumbnail.png ")
                }
            }

            /*     var instance = ShoppingCentre3DContext.getInstance()
                 if (instance != null && instance.activityInstance != null && this@InternetModelDataProvider.settings.contains(
                         ModelDataProviderSettings.Model
                     )
                 ) {//hallet burayı
                     // var loader: AssetLoader = AssetLoader(ShoppingCentre3DContext.getInstance().engine , )
                     //loader.createAsset()
                 }
     */
            return@async ShoppingCentreErrorHolder(returnValue = model)
        }

    /**
    * InternetModelDataProvider ile aynı işleve sahip.
    * */
    override fun GetSpecifiedData(
        localPath: String,
        settings: EnumSet<ModelDataProviderSettings>
    ): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(
            Dispatchers.Default
        ).async {//Verilen path argümanından özel bir model döndürür.
            try {
                var instance = ShoppingCentre3DContext.getInstance()

                if (instance == null || !instance.isInternetConnected()) {
                    Log.i("ShoppingCenter3DError " ,  "ShoppingCentre3DContext instance is null or internet hasnt connected  !")
                    return@async ShoppingCentreErrorHolder(
                        errorCode = ErrorCode.InternetConnectionError,
                        what = "ShoppingCentre3DContext instance is null or internet hasnt connected  !"
                    )
                }

                if (settings.contains(ModelDataProviderSettings.Model)) {
                    var file = instance.loadStorageFile("${baseUrl}/${localPath}/model.glb").await()

                    if (file.errorCode != ErrorCode.Success) return@async file
                    return@async ShoppingCentreErrorHolder(returnValue = file.returnValue)

                }
            } catch (e: Exception) {
                Log.i("ShoppingCenter3DError " ,  "Exception : ${e.message}")
                return@async ShoppingCentreErrorHolder(
                    errorCode = ErrorCode.Exception,
                    what = e.message ?: ""
                )
            }
            return@async ShoppingCentreErrorHolder(
                errorCode = ErrorCode.InvalidMember,
                what = "fun GetSpecifiedData(): invalid arguments  baseUrl : ${this@FireBaseModelDataProvider.baseUrl} ,local Path : ${localPath}"
            )
        }

}