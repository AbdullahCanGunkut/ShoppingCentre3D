package com.example.shoppingcentre3d

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.shoppingcentre3d.ModelClasses.Address
import com.example.shoppingcentre3d.ModelClasses.BankCard
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import com.example.shoppingcentre3d.databinding.FragmentActivityUserSettingsFragmentBinding
import com.google.android.material.textfield.TextInputEditText

import androidx.fragment.app.FragmentActivity
import com.example.shoppingcentre3d.ModelClasses.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * Tüm kullanıcı bilgilerinin  değiştirildiği fragment. (Addressler , Banka kartları vs)
 */
class activity_user_settings_fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentActivityUserSettingsFragmentBinding
    private lateinit var allAddressesFragment: AllAddress
    private lateinit var allBankCardsFragment: BankCards
    private lateinit var userInformationFragment: UserInformation

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
        binding = FragmentActivityUserSettingsFragmentBinding.inflate(layoutInflater)

        /*
        * Bu butona basarsanız tüm bilgileri Firebase'a iletmeyi sağlayacaktır.
        * */
        binding.applyChanges.setOnClickListener {

            var dialogBuilder  = AlertDialog.Builder(this@activity_user_settings_fragment.requireContext())
            dialogBuilder.setTitle("Onay Kutusu")
            dialogBuilder.setMessage("Bilgilerinizin güncellenmesini istedğinize enim misiniz ?")
            dialogBuilder.setPositiveButton("Evet"){ _ , _ ->
                ShoppingCentre3DContext.getInstance()?.syncCurrentUserInformationToDatabase()
            }

            dialogBuilder.setNegativeButton("Hayır"){ _ , _ ->

            }
            dialogBuilder.setCancelable(false)

            dialogBuilder.create().show()

        //Aktif olan kullanıcı verilerini Firebase'e senkronize eder.

        }
        var instance = ShoppingCentre3DContext.getInstance()


        //Bu buton ise var olan hesaptan çıkmak için kullanılır.

        binding.exitUser.setOnClickListener {
            var instance = ShoppingCentre3DContext.getInstance()
            if (instance != null) {
                instance.auth.signOut()
                if (instance.auth.currentUser == null) {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                }

            }

        }

        //Yeni bir banka kartı ekler.
        binding.addBankcardButton.setOnClickListener {//Yeni banka kartı ekleme butonu.
            ShoppingCentre3DContext.getInstance()?.currentUserInformation?.Cards?.add(
                BankCard(
                    binding.addBankcard.findViewById<TextInputEditText>(R.id.bankCardName).text.toString(),
                    binding.addBankcard.findViewById<TextInputEditText>(R.id.bankcardSurname).text.toString(),
                    binding.addBankcard.findViewById<TextInputEditText>(R.id.cardNumber).text.toString(),
                    binding.addBankcard.findViewById<TextInputEditText>(R.id.securityCode).text.toString(),
                    binding.addBankcard.findViewById<TextInputEditText>(R.id.dateMonth).text.toString(),
                    binding.addBankcard.findViewById<TextInputEditText>(R.id.dateYear).text.toString(),

                    )
            )
            allBankCardsFragment.bankCardsAdapter.userInformation =
            instance?.currentUserInformation!!
            allAddressesFragment.addressesAdapter?.notifyDataSetChanged()


        }

        //Yeni bir adres ekler.
        binding.addAddressButton.setOnClickListener { //Yeni adres ekleme butonu.

            var instance = ShoppingCentre3DContext.getInstance()
            if (instance != null) {
                instance?.currentUserInformation?.Addresses?.add(
                    Address(
                        binding.addAddress.findViewById<TextInputEditText>(R.id.addressTitle).text.toString(),
                        binding.addAddress.findViewById<TextInputEditText>(R.id.name).text.toString(),
                        binding.addAddress.findViewById<TextInputEditText>(R.id.surname).text.toString(),
                        binding.addAddress.findViewById<TextInputEditText>(R.id.addressText).text.toString(),
                        binding.addAddress.findViewById<TextInputEditText>(R.id.telephone).text.toString()
                    )
                )
                allAddressesFragment.addressesAdapter.userInformation =
                    instance?.currentUserInformation!!
                allAddressesFragment.addressesAdapter?.notifyDataSetChanged()

            }
        }


        CoroutineScope(Dispatchers.Default).async {
            if (instance != null) {
                var error = instance.setCurrentUserInformation().await()

            }

            allAddressesFragment = AllAddress()
            // İçerik fragment'ını yükle ve container'a ekle
            val fragmentManager = parentFragmentManager
            /*Fragment eklemek için managerimizi alıyoruz ve daha sonra hepsini ekleyip ve
            commit yapıp tüm fragment'lerin activiteye eklenmesini sağlıyoruz.
*/

            var transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.allAddresses, allAddressesFragment)

            allBankCardsFragment = BankCards()
            transaction.add(R.id.allBankCards, allBankCardsFragment)

            userInformationFragment = UserInformation()
            transaction.add(R.id.userInformation, userInformationFragment)
            transaction.commit()
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
         * @return A new instance of fragment activity_user_settings_fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            activity_user_settings_fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}