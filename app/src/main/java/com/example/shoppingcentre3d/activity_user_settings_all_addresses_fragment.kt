package com.example.shoppingcentre3d

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.shoppingcentre3d.ErrorHandling.ErrorCode
import com.example.shoppingcentre3d.Helpers.ShoppingCentre3DHelpers
import com.example.shoppingcentre3d.ModelClasses.User
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import com.example.shoppingcentre3d.databinding.ActivityUserRegisterMenuBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import androidx.navigation.*
import com.example.shoppingcentre3d.Adapters.AddressesAdapter
import com.example.shoppingcentre3d.databinding.ActivityUserSettingsAllAddressesFragmentBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * Tüm adresleri tutan fragmentimiz.
 */
class activity_user_settings_all_addresses_fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: ActivityUserSettingsAllAddressesFragmentBinding
    private lateinit var adapter: AddressesAdapter

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

        binding =
            ActivityUserSettingsAllAddressesFragmentBinding.inflate(inflater, container, false)

        var instance = ShoppingCentre3DContext.getInstance()
        if (instance?.currentUserInformation != null) {
            this.adapter = AddressesAdapter(instance?.currentUserInformation!!)
            binding.addresses.adapter = this.adapter
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