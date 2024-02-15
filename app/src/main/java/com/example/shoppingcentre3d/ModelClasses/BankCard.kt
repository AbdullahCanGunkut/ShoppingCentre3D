package com.example.shoppingcentre3d.ModelClasses


/**
 * Banka kartı bilgilerini tutan model class'ımız.
 * */
data class BankCard(
    val name: String = "",
    val surname: String = "",
    val cardNumber: String = "",
    val cvv: String = "",
    val validityDateMonth: String = "",
    val validityDateYear: String = ""
) {
   /* fun getValidityPiece(isMonth: Boolean): String {
        val split = this.validityDate.split("/".toRegex())
        return if (split.size == 0) split[0] else if (isMonth) split[0] else split[1]
    }
*/
}
