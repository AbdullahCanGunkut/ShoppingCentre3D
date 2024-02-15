package com.example.shoppingcentre3d

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import com.example.shoppingcentre3d.databinding.ActivityUserSettingsUserInformationsBinding

// TODO: Rename parameter arguments, choose names that match
// Kullanıcı bilgilerinin yansıtıldığı fragment.

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 *Kullanıcıya ait bilgilerin değiştirilecek ve tutulacak olan bir Fragment.
 */
class UserInformation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: ActivityUserSettingsUserInformationsBinding

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
        binding = ActivityUserSettingsUserInformationsBinding.inflate(layoutInflater)

        var instance = ShoppingCentre3DContext.getInstance()

        if (instance?.currentUserInformation != null) {
            var info = instance?.currentUserInformation!!
            binding.name.setText(info.Name)
            binding.surname.setText(info.Surname)
            binding.phoneNumber.setText(info.Phone)
            binding.birthdayDate.setText(info.Birthday)
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
         * @return A new instance of fragment UserInformation.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserInformation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}