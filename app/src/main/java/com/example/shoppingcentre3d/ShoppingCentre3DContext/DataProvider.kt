package com.example.shoppingcentre3d.ShoppingCentre3DContext
import com.example.shoppingcentre3d.ErrorHandling.ShoppingCentreErrorHolder
import com.example.shoppingcentre3d.ModelClasses.Product
import kotlinx.coroutines.*
import java.util.EnumSet

//Bir ürün verisini yüklemek için kullanacağımız ayarlar.
enum class ModelDataProviderSettings{
    Model,
    Thumbnail,
    Images
}

/**
 * Bu interface amacı size herhangi bir işlemden veri döndürmek. (Örnek : local dosyalardan , internet üzerrinden vs.) Genel olarak kullanım amacı model dosyalarını internet veya herhangi bir dosyadan temin etmek.
 * Şu anki amacı ise direkt ürün ModelFile bilgilerini sağlamak olacak.
*/
interface DataProvider {


    /**
     * Direkt ModelFile almak için kullanılır.
     * */
    fun GetData(productMetaData: Product) : Deferred<ShoppingCentreErrorHolder> = CoroutineScope(Dispatchers.Default).async{
        return@async ShoppingCentreErrorHolder()
    }

    /**
    * Direkt belirtilen özel bir veri alır tekil olark.
    * */
    fun GetSpecifiedData(path : String  , settings : EnumSet<ModelDataProviderSettings>) : Deferred<ShoppingCentreErrorHolder> = CoroutineScope(Dispatchers.Default).async{//Verilen path argümanından özel bir model döndürür.
        return@async ShoppingCentreErrorHolder()
    }

}