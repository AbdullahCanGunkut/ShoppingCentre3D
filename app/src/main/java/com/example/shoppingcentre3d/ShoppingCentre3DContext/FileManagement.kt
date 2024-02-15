package com.example.shoppingcentre3d.ShoppingCentre3DContext

import android.util.Log
import com.example.shoppingcentre3d.ErrorHandling.ErrorCode
import com.example.shoppingcentre3d.Helpers.ShoppingCentre3DHelpers
import com.example.shoppingcentre3d.ErrorHandling.ShoppingCentreErrorHolder
import com.example.shoppingcentre3d.ModelClasses.ModelFile
import com.example.shoppingcentre3d.ModelClasses.Product
import kotlinx.coroutines.runBlocking
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import retrofit2.Response
import retrofit2.http.GET

typealias  ProductMap = HashMap<String, Product>
typealias  ProductList = List<Product>
typealias  ProductListMutable = MutableList<Product>

/**
 * Retrofit tarafından kullanılacak olan bir interface.
 * */
interface FileManagementApi {
    @GET("FileManager.json")
    suspend fun getFileManagement(): Response<FileManagement>
}

/**
 * Tüm ürün bilgilerinin saklandığı ve sistematik olarak işlendiği yer burasıdır.
 * Ürünlere göre ModelFile oluşturabilir.
 * Ürün filtrelemesi yapılabilir (Min Fiyat , Maks Fiyat ve Kategorisi gibi)
**/
class FileManagement {
    @Volatile
    var products: ProductMap =
        ProductMap() //String -> Product Id temsil ediyor , LocalDateTime ise o dosyanın ne zamana oluşturulduğu veya değiştirildği.

    constructor(list: ProductList = listOf()) {
        createProductsFromList(list)
    }


  /*  @Synchronized //Daha sistematik cache sistemi olduğu zaman kullanılabilir.
    fun update(
        other: ProductList,
        updateItem: ((product: Any) -> Any)?
    ): ShoppingCentreErrorHolder {

        for (k in other) {
            var bool: Boolean = true
            if (this.products.contains(k.productId)) {
                if (updateItem != null)
                    updateItem(k)
                this.products[k.productId] = k
            }
        }
        return ShoppingCentreErrorHolder()
    }

*/

    /**
     * Listeyi Hashmap'a çevirmek için kullanacağız.
     * */
    @Synchronized
    fun createProductsFromList(lst: ProductList) {
        for (i in lst)
            this.products[i.productId] = i

    }

    /**
    * products HashMap'ını listeye çevirmek için kullanacağız.
    * */
    @Synchronized
    fun toList(): ProductListMutable = PMapTopPlist(this.products)

    /**
     * ModelFile dosyasını belirtilen ürüne(productId) göre yüklemek için kullanacağız.
    * */
    @Synchronized
    fun loadModelFile(
        productId: String,
        provider: DataProvider
    ): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Main).async {
            if (this@FileManagement.products.contains(productId)) {
                var data = provider.GetData(this@FileManagement.products[productId]!!).await()
                if (data.errorCode == ErrorCode.Success) {
                    synchronized(this) {
                        this@FileManagement.products[productId]?.modelFile =
                            data.returnValue as ModelFile
                    }
                    return@async ShoppingCentreErrorHolder()
                }

                return@async data
            }
            return@async ShoppingCentreErrorHolder(
                errorCode = ErrorCode.InvalidArgument,
                what = "fun loadModel() : productId is invalid !"
            )

        }


    /**
     * Ürünleri filtreler
    * */
    @Synchronized
    fun filterProducts(
        minPrice: Double = 0.0, maxPrice: Double = 0.0,
        productCategory: String = "", productName: String = ""
    ): ProductList {
        var lst : ProductListMutable = mutableListOf()
        val minVal = minOf(minPrice, maxPrice)
        val maxVal = maxOf(minPrice, maxPrice)

        var toLst = toList()
        var regex = Regex(productName , RegexOption.IGNORE_CASE)

        for (i in toLst) {
            var ok = true
            if (minPrice != 0.0 || maxPrice != 0.0)
                ok = ok and (i.price >= minVal && i.price <= maxVal)
            if (productCategory != "All" && productCategory != "Hepsi" || productCategory == "")
                ok = ok and (productCategory == i.category)

            if (productName != "")
                ok = ok and (regex.find(i.productName) != null)


            if (ok) {
                lst.add(i)
            }
        }

        return lst

    }


    //Singleton nesnemiz FileManagement class'ımız için.
    companion object {
        //Burası varsayılan url ürünlerin dosyaları ve FileManager.json için
        const val DefaultProductsPath: String =
            "https://raw.githubusercontent.com/AbdullahCanGunkut/ShoppingCentre3D/main/products/"



        const val FirebaseStorageDefaultProductsPath: String = "products"


        /**
         * Bir product map'i liste dönüştürür.
         * */
        @Synchronized
        fun PMapTopPlist(map: ProductMap): ProductListMutable {
            var lst: ProductListMutable = mutableListOf()
            for (entry in map.entries)
                lst.add(entry.value)
            return lst
        }

        /**
         * Verilen url'dan alınacak olan FileManagement.json'u FileManagement class olarak dönderir.
         */
        fun GetFileConfigurationFromUrl(
            url: String?
        ): ShoppingCentreErrorHolder {
            try {
                var response = runBlocking {

                    return@runBlocking ShoppingCentre3DHelpers.downloadFileWithRetrofit<FileManagementApi>(
                        DefaultProductsPath
                    ).getFileManagement()
                }
                if (response.isSuccessful) {
                    return ShoppingCentreErrorHolder(
                        returnValue = response.body()
                    )
                }

            } catch (e: Exception) {
                return ShoppingCentreErrorHolder(ErrorCode.Exception, what = e.message ?: "")
            }
            return ShoppingCentreErrorHolder(
                ErrorCode.Null,
                what = "fun GetFileConfigurationFromUrl(): local value config is null !"
            )


        }


        /**
         * Verilen local path'i FireBase storage'den alınacak olan FileManagement.json'u FileManagement class olarak dönderir.
         */
        fun GetFileConfigurationFromFirebaseStorage(
            baseUrl: String
        ): ShoppingCentreErrorHolder {
            try {
                var response = runBlocking {
                    var instance = ShoppingCentre3DContext.getInstance()


                    if (instance == null || !instance.isInternetConnected()) {
                        Log.i(
                            "ShoppingCenter3DError ",
                            "ShoppingCentre3DContext instance is null or internet hasnt connected  !"
                        )
                        return@runBlocking ShoppingCentreErrorHolder(
                            errorCode = ErrorCode.InternetConnectionError,
                            what = "ShoppingCentre3DContext instance is null or internet hasnt connected  !"
                        )
                    }

                    return@runBlocking instance!!.loadStorageFile(baseUrl + "/FileManager.json")
                        .await()

                }
                if (response.errorCode == ErrorCode.Success) {
                    return ShoppingCentreErrorHolder(
                        returnValue = Gson().fromJson(
                            (response.returnValue as ByteArray).toString(
                                Charsets.UTF_8
                            ), FileManagement::class.java
                        )

                    )
                }

            } catch (e: Exception) {
                return ShoppingCentreErrorHolder(ErrorCode.Exception, what = e.message ?: "")
            }
            return ShoppingCentreErrorHolder(
                ErrorCode.Null,
                what = "fun GetFileConfigurationFromUrlFireBase(): local value config is null !"
            )


        }


    }
}


/*
open class FileConfiguration<ProductType :CacheableMap<String , LocalDateTime> > {
    //Bu sınıf ürün listelerini bir json'dan alır ve CacheableMap'e dönüştürür , amacı ise sitede var olan ürünlerin bilgisinin hepsini tutmak.
    @Volatile
    var products: ProductType = CacheableMap<String , LocalDateTime>() //String -> Product Id temsil ediyor , LocalDateTime ise o dosyanın ne zamana oluşturulduğu veya değiştirildği.

    constructor(list: List<Product> = listOf()) {
        this.createFromListProducts(list)
    }
    fun update(
        config: FileConfiguration<ProductType>,
        updateItem: ((ProductType, value : Any) -> Any)?
    ): ShoppingCentreErrorHolder {
        // this.products.update(config.products, updateItem)
        return ShoppingCentreErrorHolder()
    }
    open fun createFromListProducts(products: List<Product>): ShoppingCentreErrorHolder {

        this.products = CacheableMap<String ,LocalDateTime>()
        for (prdc in products)
            this.products[CacheableMapKey<String , LocalDateTime>(prdc.productId, prdc.date)] = prdc

        return ShoppingCentreErrorHolder()
    }
    companion object {


        //Verilen url'dan alınacak olan jsonu belirtilen FileConfigurationType çevirir ve dönderir.
        fun<FileConfigurationType : FileConfiguration> GetFileConfigurationFromUrl(url: String , cls : Class<FileConfigurationType>): ShoppingCentreErrorHolder {

            var config: FileConfiguration? = null

            try {
                runBlocking {
                    var error = ShoppingCentre3DHelpers.downloadFile(url).await()
                    if (error.errorCode == ErrorCode.Success) {
                        val gson = Gson()
                        config = gson.fromJson(
                            (error.returnValue as ByteArray).toString(Charsets.UTF_8),
                            cls
                        )
                    }

                }
            } catch (e: Exception) {
                return ShoppingCentreErrorHolder(ErrorCode.Exception, what = e.message ?: "")
            }
            return if (config != null) return ShoppingCentreErrorHolder(returnValue = config) else return ShoppingCentreErrorHolder(
                ErrorCode.Null,
                what = "fun GetFileConfigurationFromUrl(): local value config is null !"
            )


        }

    }
}*/