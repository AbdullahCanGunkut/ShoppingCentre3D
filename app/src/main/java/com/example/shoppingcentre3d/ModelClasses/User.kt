package com.example.shoppingcentre3d.ModelClasses


/**
 * Kullanıcı bilgilerini tutan model class'ımız.
 * */
data class User(

    var Name: String = "",
    var Surname: String = "",
    var Phone : String = "",
    var Birthday : String = "",
    var Cards: MutableList<BankCard> = mutableListOf(),
    var Addresses: MutableList<Address> = mutableListOf(),
    var Basket: MutableList<BasketItem> = mutableListOf()
) {
}