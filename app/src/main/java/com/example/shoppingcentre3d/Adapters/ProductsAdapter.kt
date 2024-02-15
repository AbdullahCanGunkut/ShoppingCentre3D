package com.example.shoppingcentre3d.Adapters

import  android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import  androidx.recyclerview.widget.RecyclerView
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import com.example.shoppingcentre3d.ShoppingCentre3DContext.FileManagement
import com.example.shoppingcentre3d.ModelClasses.*
import com.example.shoppingcentre3d.ErrorHandling.ErrorCode
import com.example.shoppingcentre3d.ShoppingCentre3DContext.FireBaseModelDataProvider
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ProductList
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ProductListMutable
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ProductMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask


/**
 * Ürünlere buton basıldığı zaman eklemek için kullanacağımız
 * Bunu özellike ürün detaylarına geçerken kullanacağımız interface olarak ayarladım.
 * */
interface ProductItemListener {
    fun OnClick(product: Product)
}

/**
 * Ürünlerin bilgisini RecylceView'a aktarmak için kullanacağımız adataper.
 * */
class ProductsAdapter :
    RecyclerView.Adapter<ProductThumbnailViewHolder> {

    private lateinit var productList: ProductList
    private lateinit var listener: ProductItemListener

    constructor(map: ProductMap, listener: ProductItemListener) {
        this.productList = FileManagement.PMapTopPlist(map)
        this.listener = listener
    }

    constructor(lst: ProductList, listener: ProductItemListener) {
        this.productList = lst
        this.listener = listener
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ProductThumbnailViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(
                com.example.shoppingcentre3d.R.layout.activity_main_product_item,
                viewGroup,
                false
            )
        return ProductThumbnailViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ProductThumbnailViewHolder, position: Int) {
        var prdc = this.productList[position]
        holder.productName.text = prdc.productName
        holder.price.text = prdc.price.toString() + "$"

        if (prdc.modelFile?.thumbnail != null)
            holder.image.setImageBitmap(prdc.modelFile!!.thumbnail!!.second)

        holder.body.setOnClickListener {
            this.listener.OnClick(prdc)
        }
    }

    override fun getItemCount() = this.productList.size

}

/**
* Ürünlerin güncellenip güzelce asenkron şekilde Viewer'a yerleştirmek için kullanacağımız bir model.
 *
* */
class ProductsViewModel :
    ViewModel() {

    /**
     * Eğer ürün değiştiği zaman anında ekrana yansıtmak istiyorsak LivedData güzel bir çözüm.
     * */
    var productsLiveData: MutableLiveData<ProductList?> = MutableLiveData()

    var minPrice: Double = 0.0
    var maxPrice: Double = 0.0
    var productName: String = ""
    var category: String = "Hepsi"
    val timer = Timer()

    init {

        //Bir background task oluşturup asenkron bir şekilde 1 saniye bir dinleyip verileri tekrar adapter'a eşitliyoruz.
        timer.scheduleAtFixedRate(object : TimerTask() {
            @Synchronized
            override fun run() {
                var cor = CoroutineScope(Dispatchers.Main).launch {
                    var instance = ShoppingCentre3DContext.getInstance()


                    if (instance != null && instance.fileManagement != null) {//TODO:Burayı düzeltmemiz lazım özellike sürekli veri tazeliyor gereksiz yere...
                        var lst = instance!!.fileManagement!!.filterProducts(
                            minPrice,
                            maxPrice,
                            category,
                            productName
                        )
                        productsLiveData.value = lst

                    } else {

                        var error =
                            FileManagement.GetFileConfigurationFromFirebaseStorage("products")

                        if (error.errorCode == ErrorCode.Success) {

                            instance?.fileManagement = (error.returnValue as FileManagement)
                            instance?.syncFileManagementModelFiles(
                                FireBaseModelDataProvider(
                                    FileManagement.FirebaseStorageDefaultProductsPath
                                )
                            )


                        }


                    }


                }


            }
        }, 0, 1000)
    }

    fun update() {
        var lst: ProductListMutable = mutableListOf()
        var instance = ShoppingCentre3DContext.getInstance()

        if (instance != null && instance.fileManagement != null)
            productsLiveData.value = instance!!.fileManagement!!.toList()
    }

    override fun onCleared() {//veri temizliği
        super.onCleared()
        timer.cancel()
    }
}


class ProductThumbnailViewHolder : ViewHolder {

    val image: ImageView
    val price: TextView
    val productName: TextView
    val body: View

    constructor(itemView: View) : super(itemView) {
        this.price = itemView.findViewById(com.example.shoppingcentre3d.R.id.productPrice)
        this.image = itemView.findViewById(com.example.shoppingcentre3d.R.id.productImage)
        this.productName = itemView.findViewById(com.example.shoppingcentre3d.R.id.productName)
        this.body = itemView.findViewById(com.example.shoppingcentre3d.R.id.productItem)

    }

}