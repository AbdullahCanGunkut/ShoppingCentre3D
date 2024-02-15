package com.example.shoppingcentre3d

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.shoppingcentre3d.ErrorHandling.*
import com.example.shoppingcentre3d.databinding.ActivityUserLoginMenuBinding
import  com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import kotlinx.coroutines.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * Kullanıcı girişi yapmak için kullanacağımız Fragment.
 */
class activity_user_login_fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: ActivityUserLoginMenuBinding


    /*
    * Navigasyon ayarları ve özellike animasyon için kullanacağız.
    * */
    private val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_right)
        .setExitAnim(R.anim.slide_left)
        .setPopEnterAnim(R.anim.slide_right)
        .setPopExitAnim(R.anim.slide_left)
        .build()

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
        binding = ActivityUserLoginMenuBinding.inflate(inflater, container, false)

        if (ShoppingCentre3DContext.getInstance() != null && ShoppingCentre3DContext.getInstance()
                ?.checkIsConnectionAvaiable()!!
        ) {//Eğer bağlantı varsa direkt yönlendirme yapılacak.
            findNavController().navigate(R.id.activity_user_settings_fragment, null, navOptions)
        }

        binding.signupButton.setOnClickListener {
            findNavController().navigate(
                R.id.activity_user_register_fragment,
                null,
                navOptions
            )

        }

        binding.signButton.setOnClickListener {

            CoroutineScope(Dispatchers.Main).launch {

                if (ShoppingCentre3DContext.getInstance() != null) {
                    binding.signButton.isEnabled = false
                    binding.signupButton.isEnabled = false

                    var email = this@activity_user_login_fragment.binding.email.text.toString()
                    var password =
                        this@activity_user_login_fragment.binding.password.text.toString()


                    if (email == "" || password == "") {
                        var dialogBuilder =
                            AlertDialog.Builder(this@activity_user_login_fragment.requireContext())
                        dialogBuilder.setTitle("Hata !")
                        dialogBuilder.setMessage("Tüm bilgileri eksiksiz giriniz !")
                        dialogBuilder.setCancelable(true)
                        binding.signupButton.isEnabled = true
                        dialogBuilder.create().show()
                        binding.signButton.isEnabled = true
                        binding.signupButton.isEnabled = true
                        return@launch
                    }


                    var error = ShoppingCentre3DContext.getInstance()?.setUser(
                        this@activity_user_login_fragment.binding.email.text.toString(),
                        this@activity_user_login_fragment.binding.password.text.toString()
                    )?.await()

                    if (error!!.errorCode == ErrorCode.Success) {
                        findNavController().navigate(
                            R.id.activity_user_settings_fragment,
                            null,
                            navOptions
                        )
                    } else {
                        binding.signButton.isEnabled = true
                        binding.signupButton.isEnabled = true
                    }


                }

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
         * @return A new instance of fragment activity_user_login_fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            activity_user_login_fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}