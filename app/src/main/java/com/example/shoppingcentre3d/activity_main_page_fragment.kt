package com.example.shoppingcentre3d

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.shoppingcentre3d.Adapters.ProductItemListener
import com.example.shoppingcentre3d.Adapters.ProductsAdapter
import com.example.shoppingcentre3d.Adapters.ProductsViewModel
import com.example.shoppingcentre3d.ModelClasses.Product
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ProductList
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import com.example.shoppingcentre3d.databinding.FragmentActivityMainPageFragmentBinding
import com.google.android.material.textfield.TextInputEditText

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class activity_main_page_fragment : Fragment(), LifecycleOwner {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var model: ProductsViewModel? = null
    private var adapter: ProductsAdapter? = null

    /**
    * Eğer ürünler yenilenirse direkt olarak adapter içindeki verileri yenileyen observer.
    * */
    private var productListObserver: Observer<ProductList?> = Observer<ProductList?>({ lst ->
        var instance = ShoppingCentre3DContext.getInstance()
        if (instance?.fileManagement != null && lst != null) {
            binding.mainPageFrag.products.adapter =
                ProductsAdapter(lst, object : ProductItemListener {
                    override fun OnClick(product: Product) {
                        instance.setCurrentProduct(product.productId)
                        findNavController().navigate(
                            R.id.activity_main_product_details_fragment,
                            null,
                            navOptions
                        )
                    }
                })
        }

    })
    //Aynı şekilde geçiş animasyonu oluşturmak için.
    private val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_right)
        .setExitAnim(R.anim.slide_left)
        .setPopEnterAnim(R.anim.slide_right)
        .setPopExitAnim(R.anim.slide_left)
        .build()

    private lateinit var binding: com.example.shoppingcentre3d.databinding.FragmentActivityMainPageFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentActivityMainPageFragmentBinding.inflate(layoutInflater)
        model = ViewModelProvider(this).get(ProductsViewModel::class.java)
        model?.productsLiveData?.observe(viewLifecycleOwner, productListObserver)



        val spinner: Spinner =
            binding.mainPageFrag.searchProduct.findViewById<Spinner>(R.id.category)

        //Kategori spinner'ımıza yeni değerler eklemek için kullanacağımız adapter ve bunu R.array.productCategory'den sağlayacağız yani varsayılan kategori String arrayından.
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            resources.getStringArray(
                R.array.productCategory
            )
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


        //Ürün arama butonu.
        binding.mainPageFrag.searchProduct.findViewById<AppCompatButton>(R.id.searchButton)
            ?.setOnClickListener {

                var instance = ShoppingCentre3DContext.getInstance()

                if (instance?.fileManagement != null) {

                    var minPrice =
                        binding.mainPageFrag.searchProduct.findViewById<TextInputEditText>(R.id.minPrice).text.toString()
                    var maxPrice =
                        binding.mainPageFrag.searchProduct.findViewById<TextInputEditText>(R.id.maxPrice).text.toString()

                    model?.minPrice =  if (minPrice == "") 0.0 else minPrice.toDouble()
                    model?.maxPrice =  if (maxPrice == "") 0.0 else maxPrice.toDouble()
                    model?.productName = binding.mainPageFrag.searchProduct.findViewById<TextInputEditText>(R.id.productName).text.toString()
                    model?.category =  binding.mainPageFrag.searchProduct.findViewById<Spinner>(R.id.category).selectedItem.toString()

                }

            }

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment activity_main_page_fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            activity_main_page_fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}