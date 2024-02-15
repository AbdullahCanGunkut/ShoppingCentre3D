package com.example.shoppingcentre3d.ModelClasses



//Bu class şuan için geçersiz.
data class BasketItem (
    var productId : String,
    var amount : Int
){

    fun calculateItemPrice() : Int{
    return this.amount * 4
    }

}