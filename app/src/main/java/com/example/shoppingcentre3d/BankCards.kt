package com.example.shoppingcentre3d

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.shoppingcentre3d.Adapters.AddressesAdapter
import com.example.shoppingcentre3d.Adapters.BankCardsAdapter
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import com.example.shoppingcentre3d.databinding.ActivityUserSettingsAllBankCardsFragmentBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * Kullanıcıya ait banka kartların tutulduğu Fragment.
 */
class BankCards : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var bankCardsAdapter: BankCardsAdapter
    lateinit var binding: ActivityUserSettingsAllBankCardsFragmentBinding

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

        binding = ActivityUserSettingsAllBankCardsFragmentBinding.inflate(layoutInflater)
        var instance = ShoppingCentre3DContext.getInstance()
        if (instance?.currentUserInformation != null) {
            this.bankCardsAdapter = BankCardsAdapter(instance?.currentUserInformation!!)
            binding.bankcards.adapter = this.bankCardsAdapter
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
         * @return A new instance of fragment BankCards.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BankCards().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}