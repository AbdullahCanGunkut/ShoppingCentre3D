package com.example.shoppingcentre3d.ErrorHandling



/**
 * ShoppingCentre3D tarafından kullanılan ve varsayılan özel hata kodları.
 * ShoppingCentreErrorHolder tarafından kullanılır.
 * */
enum class ErrorCode {
    Success,
    InvalidRequest,
    InvalidArgument,
    InvalidMember,
    RequestError,
    Null,
    AnyError,
    Exception,
    IO,
    InternetConnectionError,
    UserCreateError
}
/**
 * Basit bir şekilde Exception tarzında hem hata kodu , hem return değeri ve hem hatanın ne olduğunu Unhandled exception olmadan döndürülen bir model class.
 *
 * */
data class ShoppingCentreErrorHolder(val errorCode : ErrorCode = ErrorCode.Success , val what: String = "" , val returnValue : Any? = null) {
    override fun toString(): String {
        return "ErrorCode : " + this.errorCode.toString() + " / What : " + this.what
    }
}