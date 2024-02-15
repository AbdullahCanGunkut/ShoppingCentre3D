package com.example.shoppingcentre3d

import android.os.Bundle
import android.transition.Visibility
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import com.example.shoppingcentre3d.databinding.ActivityMainProductDetailsBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * Bir ürüne basıldığı zaman işte o tüm detayları gösteren Fragment.
 * ShoppingCenter3DRenderMachine'yi kullanan ÖNEMLİ bir Fragment.
 */

class activity_main_product_details_fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: ActivityMainProductDetailsBinding
    private lateinit var modelViewportFragment: ShoppingCenter3DRenderMachine
    private var currentImageIndex: Int = 0 //Resim değiştirirken kullanacağımız index.

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
        binding = ActivityMainProductDetailsBinding.inflate(inflater, container, false)
        var instance = ShoppingCentre3DContext.getInstance()

        if (instance?.currentProduct != null) { //Eğer seçili ürün varsa ozaman bilgileri güncelleyecek ve

            var prodc = instance?.currentProduct!!
            binding.productName.setText(prodc.productName.toString())
            binding.productPrice.setText(prodc.price.toString() + "$")
            binding.categoryName.setText(prodc.category.toString())
            binding.productDescription.setText(prodc.description.toString())

            if( (prodc?.modelFile?.images?.size ?: 0) != 0)
                binding.modelImage.setImageBitmap(prodc?.modelFile?.images!![0].second)


            //Resim kümesinden bir soldakine iletir.
            binding.leftImage.setOnClickListener {

                var size = prodc?.modelFile?.images?.size ?: 0
                if (size != 0) { //Tabi eğer resim varsa işlem yapar.
                    currentImageIndex--
                    if (currentImageIndex == -1) { //Eğer -1 index olursa tekrar en sondan başlar.
                        currentImageIndex = size - 1
                    }

                    var img = prodc?.modelFile?.images!![currentImageIndex]
                    binding.modelImage.setImageBitmap(img.second)
                }

            }

            //Resim kümesinden bir sağdakine ilerletir.
            binding.rightImage.setOnClickListener {
                var size = prodc?.modelFile?.images?.size ?: 0
                if (size != 0) {
                    currentImageIndex++
                    if (currentImageIndex == size) { //Eğer boyuta eşit olursa ilk baştan başlar.
                        currentImageIndex = 0
                    }
                    var img = prodc?.modelFile?.images!![currentImageIndex]
                    binding.modelImage.setImageBitmap(img.second)
                }

            }
            binding.imageMode.setOnClickListener {
                binding.modelImageBody.visibility = View.VISIBLE
                binding.modelViewport.visibility = View.GONE
                binding.imageMode.setBackgroundColor(resources.getColor(R.color.black))
                binding.modelMode.setBackgroundColor(resources.getColor(R.color.theme_color_1))
            }
            binding.modelMode.setOnClickListener {
                binding.modelImageBody.visibility = View.GONE
                binding.modelViewport.visibility = View.VISIBLE
                binding.imageMode.setBackgroundColor(resources.getColor(R.color.theme_color_1))
                binding.modelMode.setBackgroundColor(resources.getColor(R.color.black))
            }


        }
        modelViewportFragment = ShoppingCenter3DRenderMachine()

        // İçerik fragment'ını yükle ve container'a ekle
        val fragmentManager = childFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.modelViewport, modelViewportFragment)
        transaction.commit()

        return binding.root
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProductDetails.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            activity_main_product_details_fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}