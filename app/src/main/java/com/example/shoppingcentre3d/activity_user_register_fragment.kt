package com.example.shoppingcentre3d

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.*
import androidx.navigation.fragment.findNavController
import com.example.shoppingcentre3d.ErrorHandling.ErrorCode
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import com.example.shoppingcentre3d.databinding.ActivityUserRegisterMenuBinding
import com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * Kullanıcıyı kaydetmek için kullanılan Fragment.
 */

class activity_user_register_fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: ActivityUserRegisterMenuBinding
    private var birthdayDate : String = "1/1/2000" //gün/ay/yıl
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



        if (ShoppingCentre3DContext.getInstance() != null && ShoppingCentre3DContext.getInstance()
                ?.checkIsConnectionAvaiable()!!
        ) {//Eğer bağlantı varsa direkt yönlendirme yapılacak.
            findNavController().navigate(R.id.activity_user_settings_fragment, null, navOptions)
        }


        binding = ActivityUserRegisterMenuBinding.inflate(inflater, container, false)

        binding.birthdayDate.init(2000 , 1, 1 , { datePicker, year, monthOfYear,dayOfMonth  ->
            birthdayDate =    (dayOfMonth.toString() + "/" + (monthOfYear + 1).toString() ) + "/" + year.toString()
        })

        //Kayıt butonu.
        binding.signupButton.setOnClickListener {

            CoroutineScope(Dispatchers.Main).launch {

                if (ShoppingCentre3DContext.getInstance() != null) {
                    binding.signupButton.isEnabled = false

                    var email = this@activity_user_register_fragment.binding.email.text.toString()
                    var password =
                        this@activity_user_register_fragment.binding.password.text.toString()
                    var name = this@activity_user_register_fragment.binding.name.text.toString()
                    var surname =
                        this@activity_user_register_fragment.binding.surname.text.toString()
                    var telephone =
                        this@activity_user_register_fragment.binding.telephone.text.toString()


                    if (email == "" || password == "" || name == "" || surname == "" || telephone == "" || birthdayDate == "") {

                     var dialogBuilder  = AlertDialog.Builder(this@activity_user_register_fragment.requireContext())
                        dialogBuilder.setTitle("Hata !")
                        dialogBuilder.setMessage("Tüm bilgileri eksiksiz giriniz !")
                        dialogBuilder.setCancelable(true)
                        binding.signupButton.isEnabled = true
                        dialogBuilder.create().show()

                        return@launch
                    }


                    var error = ShoppingCentre3DContext.getInstance()?.createUser(
                        email, password, name, surname, telephone, birthdayDate

                    )?.await()

                    if (error == null || error?.errorCode == ErrorCode.Success) {
                        findNavController().navigate(
                            R.id.activity_user_settings_fragment,
                            null,
                            navOptions
                        )
                    } else {
                        binding.signupButton.isEnabled = true
                    }


                }

            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment activity_user_register_fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            activity_user_register_fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}