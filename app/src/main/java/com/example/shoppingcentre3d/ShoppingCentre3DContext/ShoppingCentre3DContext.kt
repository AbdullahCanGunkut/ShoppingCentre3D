package com.example.shoppingcentre3d.ShoppingCentre3DContext


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shoppingcentre3d.ErrorHandling.ErrorCode
import com.example.shoppingcentre3d.ErrorHandling.ShoppingCentreErrorHolder
import com.example.shoppingcentre3d.Helpers.ShoppingCentre3DHelpers
import com.example.shoppingcentre3d.ModelClasses.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.EnumSet

/**
 * İşin en temel yerini bu Class oluşturuyor ve tüm işlemlerin düzenli şekilde sağlayan alt yapı.
 * Firebase yönetimi , Dosya yönetimi (FileManagement) gibi veriler burada işleniyor.
 * Genel kullanımı singleton olarak kullanıyor.
 * */

class ShoppingCentre3DContext {
    // Singleton instance
    companion object {

        @Volatile
        private var instance: ShoppingCentre3DContext? = null

        // Singleton instance'ı almak için bu metod kullanılır
        fun createInstance(
            activity: AppCompatActivity,
            cachePath: String
        ): ShoppingCentre3DContext? {

            /*synchronized kullanmamızdaki amaç başka thread'ler aynı anda oluşturmasın diyedir veya aynı anda biri okuyup biri okumasın (Race Condition önlemek için)*/
            if (instance == null)
                synchronized(this) {
                    instance = ShoppingCentre3DContext(activity, cachePath)
                }

            return instance
        }

        fun getInstance(): ShoppingCentre3DContext? {
            return instance ?: null
        }

    }


    var dataProvider: DataProvider? = null
        @Synchronized get() = field        // getter
        @Synchronized set(value) {         // setter
            field = value
        }

    var fileManagement: FileManagement? = null
        @Synchronized get() = field        // getter
        @Synchronized set(value) {         // setter
            field = value
        }

    @Volatile
    var activityInstance: AppCompatActivity

    @Volatile
    var auth: FirebaseAuth

    @Volatile
    var cachePath: String

    @Volatile
    var currentUserInformation: User? = null
        @Synchronized get() = field        // getter
        @Synchronized set(value) {         // setter
            field = value
        }

    @Volatile
    var currentProduct: Product? =
        null //Bir ürün seçtiğimiz zaman burası set olacak ve product details'e aktarılacak.
        @Synchronized get() = field        // getter
        @Synchronized set(value) {         // setter
            field = value
        }


    constructor(activity: AppCompatActivity, cachePath: String) {
        auth = FirebaseAuth.getInstance()
        this.activityInstance = activity
        this.cachePath = cachePath
    }

    private var statusToast: Toast? = null


    //Ekranda bir Toast oluşturmak için kullanacağız eğer hata veya başarılı bir işlem olursa.
    fun createStatusText(message: String, context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            if (this@ShoppingCentre3DContext.statusToast != null) {
                this@ShoppingCentre3DContext.statusToast?.cancel()
                this@ShoppingCentre3DContext.statusToast =
                    Toast.makeText(context, message, Toast.LENGTH_SHORT)
                this@ShoppingCentre3DContext.statusToast?.show()
            } else {
                this@ShoppingCentre3DContext.statusToast =
                    Toast.makeText(context, message, Toast.LENGTH_SHORT)
                this@ShoppingCentre3DContext.statusToast?.show()
            }
        }
    }

    /**
     * Firebase'a yeni bir kullanıcı oluşturur.
     * */
    @Synchronized
    fun createUser(
        email: String,
        password: String,
        name: String,
        surname: String,
        phone: String,
        date: String
    ): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Default).async {//Bir kullanıcı oluşturur.
            //  this@ShoppingCentre3DContext.firebaseMutex.lock()
            var ok =
                CompletableDeferred<Boolean>() //Başka thread'en belirlenecek değerlerin thread safe bir şekilde tutacağımız değişken, bunu aşağıdaki task içinde belirlemek için kullanacağız.

            try {
                var task =
                    this@ShoppingCentre3DContext.auth.createUserWithEmailAndPassword(
                        ShoppingCentre3DHelpers.alignTextAsSingleSpace(email),
                        ShoppingCentre3DHelpers.alignTextAsSingleSpace(password)
                    )
                        .addOnCompleteListener(this@ShoppingCentre3DContext.activityInstance) { task ->
                            if (task.isSuccessful) {
                                ok.complete(true)
                            } else {

                                // Başarısız ise
                                ok.complete(false)
                            }
                        }.addOnFailureListener {
                            ok.cancel(CancellationException())
                        }
                //  this@ShoppingCentre3DContext.firebaseMutex.unlock()
                ok.await() //Eğer işlem başarılı veya başarısız bir şekilde set edilene kadar bekleyecektir.

                if (ok.getCompletionExceptionOrNull() != null)
                    throw ok.getCompletionExceptionOrNull()!!

                if (ok.getCompleted()) { //Eğer kullanıcı oluşursa diğer işlemlere devam edeceğiz.
                    var error = setUser(
                        ShoppingCentre3DHelpers.alignTextAsSingleSpace(email),
                        ShoppingCentre3DHelpers.alignTextAsSingleSpace(password)
                    ).await() //Eğer kullanıcı oluşturuldu ise otomatik giriş yapacak.

                    if (error.errorCode == ErrorCode.Success) { //Eğer  şimdi kullanıcı belirlenmeyi başardı ise diğer işlemler yoksa aksi takdirde kullanıcı silecek.

                        var error2 = sendDataToDatabase(
                            User(
                                ShoppingCentre3DHelpers.alignTextAsSingleSpace(name),
                                ShoppingCentre3DHelpers.alignTextAsSingleSpace(surname),
                                ShoppingCentre3DHelpers.alignTextAsSingleSpace(phone),
                                ShoppingCentre3DHelpers.alignTextAsSingleSpace(date)
                            )
                        ).await() //Yeni kullanıcının verilerini (İsim , Telefon Numarası) gibi şeyleri göndereceğiz eğer işlem olmazsa kayıt silinir çünkü veriler önemli o yüzden ileride veri kaydı yaşamak istemeyiz.

                        if (error2.errorCode != ErrorCode.Success) {
                            deleteUser().await()
                            this@ShoppingCentre3DContext.createStatusText(
                                "Hata : " + error2.toString(),
                                this@ShoppingCentre3DContext.activityInstance.applicationContext
                            )
                            return@async error2
                        }

                    } else {

                        deleteUser().await()
                        this@ShoppingCentre3DContext.createStatusText(
                            "Hata : " + error.toString(),
                            this@ShoppingCentre3DContext.activityInstance.applicationContext

                        )
                        return@async error
                    }

                } else {
                    deleteUser().await()
                    this@ShoppingCentre3DContext.createStatusText(
                        this@ShoppingCentre3DContext.activityInstance.getString(com.example.shoppingcentre3d.R.string.userFailedToCreate),
                        this@ShoppingCentre3DContext.activityInstance.applicationContext

                    )
                    return@async ShoppingCentreErrorHolder(
                        errorCode = ErrorCode.UserCreateError,
                        what = "fun createUser(): User has not created !"
                    )

                }

                this@ShoppingCentre3DContext.createStatusText(
                    this@ShoppingCentre3DContext.activityInstance.getString(com.example.shoppingcentre3d.R.string.userSuccessfulyCreated),
                    this@ShoppingCentre3DContext.activityInstance.applicationContext

                )


                return@async ShoppingCentreErrorHolder()

            } catch (e: Exception) {
                this@ShoppingCentre3DContext.createStatusText(
                    e.message ?: "",
                    this@ShoppingCentre3DContext.activityInstance.applicationContext
                )

                return@async ShoppingCentreErrorHolder(
                    errorCode = ErrorCode.Exception,
                    what = e.message ?: ""
                )

            }

        }

    /**
     * Fire database'dan veri alıp ve belirtilen class'a veri çeken fonksiyon.
     * reifed ObjectType bize runtime olarak class type tutulmasını sağlayacak ve böylece ObjectType::class.java erişebilieceğiz
     * Fonksiyon biraz dosya şişirebilir çünkü inline.
     */
    @Synchronized
    inline fun <reified ObjectType : Any> fetchDataFromDatabase(): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Default).async {
            try {

                if (this@ShoppingCentre3DContext.auth.currentUser == null) {
                    return@async ShoppingCentreErrorHolder(
                        errorCode = ErrorCode.Null,
                        what = "fun fetchDataFromDatabase() : auth.currentUser is null "
                    )
                }

                val database = FirebaseDatabase.getInstance().reference
                val userReference: DatabaseReference =
                    database.child("users")
                        .child(this@ShoppingCentre3DContext.auth.currentUser!!.uid) //kullanıcı id'sine göre ayrılmış olan bölge için veri alır.

                val objDeferred = CompletableDeferred<ObjectType?>()
                try {
                    userReference.get().addOnSuccessListener { dataSnapshot ->
                        if (dataSnapshot.exists()) {
                            val data = dataSnapshot.getValue(ObjectType::class.java)
                            // User nesnesine erişebilirsiniz
                            if (data != null) {

                                objDeferred.complete(data)
                            }
                        } else {
                            objDeferred.complete(null)
                            // Kullanıcı bulunamadı veya veri yok
                        }
                    }.addOnFailureListener {
                        objDeferred.complete(null)
                    }

                } catch (e: DatabaseException) {
                    return@async ShoppingCentreErrorHolder(
                        errorCode = ErrorCode.Exception,
                        what = e.message ?: ""
                    )
                }

                var obj = objDeferred.await()
                return@async if (obj != null) ShoppingCentreErrorHolder(returnValue = obj) else
                    ShoppingCentreErrorHolder(
                        errorCode = ErrorCode.AnyError,
                        what = "fun fetchDataFromDatabase(): invalid data !"
                    )


            } catch (e: Exception) {
                return@async ShoppingCentreErrorHolder(
                    errorCode = ErrorCode.Exception,
                    what = "fun fetchDataFromDatabase() : " + (e.message ?: "")
                )
            }


        }

    /**
     *
     * */
    @Synchronized
    fun sendDataToDatabase(obj: Any): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Default).async {//Bir kullanıcı verisi gönderir.
            try {
                if (this@ShoppingCentre3DContext.auth.currentUser == null) {
                    return@async ShoppingCentreErrorHolder(
                        ErrorCode.Null,
                        what = "fun sendUserDataToDatabase(): current user is null !"
                    )
                }

                val database: DatabaseReference = FirebaseDatabase.getInstance().reference

                var ok = CompletableDeferred<Boolean>()

                //users/uid/veri olacak şekilde veri gönderir database.
                database.child("users")
                    .child(this@ShoppingCentre3DContext.auth.currentUser!!.uid).setValue(obj)
                    .addOnSuccessListener {
                        ok.complete(true)

                    }.addOnFailureListener {
                        ok.complete(false)
                    }

                ok.await()
                return@async if (ok.getCompleted()) ShoppingCentreErrorHolder()
                else ShoppingCentreErrorHolder(
                    errorCode = ErrorCode.AnyError,
                    what = "Veri gönderilemedi !"
                )
            } catch (e: Exception) {
                return@async ShoppingCentreErrorHolder(
                    ErrorCode.Exception,
                    what = e.message ?: ""
                )
            }


        }


    /**
     * Var olan kullanıcıyı silmek için kullanacağız.
     * */
    @Synchronized
    fun deleteUser(): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Default).async {//Bir kullanıcı verisi gönderir.

            try {
                if (this@ShoppingCentre3DContext.auth.currentUser == null) {
                    return@async ShoppingCentreErrorHolder(
                        ErrorCode.Null,
                        what = "fun sendUserDataToDatabase(): current user is null !"
                    )
                }
                var userUid = this@ShoppingCentre3DContext.auth.currentUser!!.uid

                val database = FirebaseDatabase.getInstance().reference
                database.child("users").child(userUid).removeValue().await()
                this@ShoppingCentre3DContext.auth.currentUser!!.delete().addOnSuccessListener {
                    // Veri gönderme başarılı
                }
                    .addOnFailureListener { exception ->
                        // Veri gönderme başarısız oldu
                        val errorMessage = exception.message
                        // Hata mesajını kullanıcıya gösterebilir veya gerekli işlemleri yapabilirsiniz
                    }.await()
                return@async ShoppingCentreErrorHolder()
            } catch (e: Exception) {
                return@async ShoppingCentreErrorHolder(
                    ErrorCode.Exception,
                    what = e.message ?: ""
                )
            }

        }

    /**
     * Var olan kullanıcıyı değiştirmek için kullanacağız.
     * */

    @Synchronized
    fun setUser(email: String, password: String): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Default).async {
            try {
                this@ShoppingCentre3DContext.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Kullanıcı başarıyla oturum açtı

                        } else {
                            // Oturum açma başarısız oldu
                            val exception = task.exception
                            val errorMessage = exception?.message
                            // Hata mesajını kullanıcıya gösterebilir veya gerekli işlemleri yapabilirsiniz
                        }
                    }

                return@async if (this@ShoppingCentre3DContext.auth.currentUser != null) ShoppingCentreErrorHolder(
                    returnValue = this@ShoppingCentre3DContext.auth.currentUser
                ) else ShoppingCentreErrorHolder(
                    ErrorCode.Null,
                    what = "fun setUser() : Null variable auth.currentUser ! "
                )

            } catch (e: Exception) {
                return@async ShoppingCentreErrorHolder(
                    ErrorCode.Exception,
                    what = e.message ?: ""
                )
            }


        }


    /**
     * Internet bağlantısını kontrol eder.
     */
    fun isInternetConnected(): Boolean {
        val connectivityManager =
            this@ShoppingCentre3DContext.activityInstance.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    @Synchronized
    fun loadStorageFile(path: String): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Default).async {
            try {
                if (!isInternetConnected()) {
                    return@async ShoppingCentreErrorHolder(
                        ErrorCode.InternetConnectionError
                    )
                }
                // Firebase Storage referansını alın
                val storage = FirebaseStorage.getInstance()

// Dosya referansını oluşturun
// Dosyayı indirin
                val localFile = withContext(Dispatchers.IO) {
                    File.createTempFile(
                        "6549889465668",
                        "data"
                    )//Rastgele bir tempFile oluşturalım Firebase storage'daki verileri direkt o dosyaya indirip oradan çekeceğiz.
                }

                //Thread safe bir şekilde işlemin başarılı olup olmadığını anlayalım.
                storage.reference.child(path).getFile(localFile)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { exception ->
                        // Dosya indirme başarısız olduğunda yapılacak işlemler
                        Log.e("TAG", "Dosya indirme başarısız oldu: $exception $path")
                    }.await()

                return@async ShoppingCentreErrorHolder(
                    returnValue = localFile.readBytes()
                )


            } catch (e: Exception) {
                return@async ShoppingCentreErrorHolder(
                    ErrorCode.Exception,
                    what = e.message ?: ""
                )
            }


        }


    /* fun syncCache() {//

     }
 */


    /** Model dosyasını yükleme ve eğer cache olarak saklandı ise cacheDir'de ozaman oradan çekecektir hem daha avantajlı ve hızlı çalışır yoksa eğer internetten indirecektir.
     *Bu fonksiyon genel olarak "Product Details" sayfasına geçerken çağrılırır bunun anlamı modeli ram'de tutmak yerine hepsini sadece ziyaret edilen modellerin bilgisine ulaşır.
     *
     */
    @Synchronized
    fun loadModel(
        productId: String,
        dtProvider: DataProvider? = null
    ): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Main).async {
            var provider = dtProvider ?: this@ShoppingCentre3DContext.dataProvider
            var instance = ShoppingCentre3DContext.getInstance()

            if (provider == null) return@async ShoppingCentreErrorHolder(
                ErrorCode.Null,
                what = "fun loadModel() : data provider is null "
            )

            try {

                if (instance != null && instance.fileManagement != null && instance.fileManagement!!.products.contains(
                        productId
                    )
                ) {
                    val availableOnCache =
                        instance.fileManagement!!.products[productId]?.modelFile?.modelAvailableOnCache
                            ?: false

                    if (availableOnCache) {//Eğer model geçici bellekte saklanıyorsa direkt olark internetten almadan diskten alr.
                        // Geçici dosyanın oluşturulması
                        val tempFile = File(
                            this@ShoppingCentre3DContext.activityInstance.applicationContext.cacheDir,
                            productId
                        )
                        if (tempFile.canRead()) {
                            return@async ShoppingCentreErrorHolder(returnValue = tempFile.readBytes())
                        } else { //Eğer model okunamıyorsa ozaman mecbur tekrardan eski yoluna gitmesi lazım yani internetten veri almalı.
                            instance.fileManagement!!.products[productId]?.modelFile?.modelAvailableOnCache =
                                false
                        }
                    } else {
                        //Direkt model alır dosya olarak.
                        var file = provider!!.GetSpecifiedData(
                            productId,
                            EnumSet.of(ModelDataProviderSettings.Model)
                        ).await()
                        if (file.errorCode != ErrorCode.Success)
                            return@async file
                        val tempFile = File(
                            this@ShoppingCentre3DContext.activityInstance.applicationContext.cacheDir,
                            productId
                        )
                        tempFile.writeBytes(file.returnValue as ByteArray)

                        this@ShoppingCentre3DContext.fileManagement!!.products[productId]?.modelFile?.modelAvailableOnCache =
                            true
                        return@async file
                    }


                }

                return@async ShoppingCentreErrorHolder(
                    errorCode = ErrorCode.InvalidArgument,
                    what = "fun loadModel() : invalid product id ${productId}"
                )
            } catch (e: Exception) {

                return@async ShoppingCentreErrorHolder(
                    errorCode = ErrorCode.Exception,
                    what = e.message ?: ""
                )
            }
            return@async ShoppingCentreErrorHolder(
                errorCode = ErrorCode.InvalidArgument,
                what = "fun loadModel() : invalid product name ${productId}"
            )
        }

    //Seçilmiş olan ürünün bilgisini tutar. Özellike bu olay product details için ürün belirlemekte kullanılır.
    @Synchronized
    fun setCurrentProduct(productId: String): ShoppingCentreErrorHolder {

        if (this.fileManagement != null && this.fileManagement?.products!!.contains(productId)) {
            this.currentProduct = this.fileManagement!!.products[productId]
            return ShoppingCentreErrorHolder()
        }
        return ShoppingCentreErrorHolder(
            errorCode = ErrorCode.AnyError,
            what = "fun setCurrentProduct() : any error !"
        )
    }


    //Şuanki kullanıcının User classını database'dan alarak belirler.
    @Synchronized
    fun setCurrentUserInformation(): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Main).async {
            var error = fetchDataFromDatabase<User>().await()
            if (error.errorCode == ErrorCode.Success) {
                this@ShoppingCentre3DContext.currentUserInformation = error.returnValue as User
                return@async ShoppingCentreErrorHolder()
            }
            return@async error
        }


    // Kullanıcı bilgilerini değiştirir (Bankkartları , İsim , Telefon numarası vb...)
    @Synchronized
    fun syncCurrentUserInformationToDatabase(): Deferred<ShoppingCentreErrorHolder> =
        CoroutineScope(Dispatchers.Main).async {
            return@async if (this@ShoppingCentre3DContext.currentUserInformation != null) sendDataToDatabase(
                this@ShoppingCentre3DContext.currentUserInformation!!
            ).await() else ShoppingCentreErrorHolder()
        }


    //Tüm ürünlerin ModelFile class'larını oluşturur ve eğer oluşamayan var ise onları es geçer ve hepsini yüklemek zorunda şart koşmaz.
    suspend fun syncFileManagementModelFiles(provider: DataProvider) {
        this.dataProvider = provider
        val deferredList = mutableListOf<Deferred<Unit>>()

        if (this@ShoppingCentre3DContext.fileManagement != null)
            for (i in this@ShoppingCentre3DContext.fileManagement!!.products) {

                if (i.value.modelFile == null) {
                    deferredList.add(CoroutineScope(Dispatchers.IO).async { //her dosya okuma işlemini asenkron olarak yapalım ki aynı andan birden fazla dosya yükleyelim ve bunu ana sayfada ki ürünler yüklenirken gözlemleyebilirsiniz.
                        var error = provider.GetData(i.value).await()
                        if (error.errorCode == ErrorCode.Success)
                            i.value.modelFile = error.returnValue as ModelFile
                    })

                }

            }
        deferredList.awaitAll() //Tüm asenkron işlemlerin bitmesini burada bekleyeceğiz.
    }


    /**
     * Eğer şimdi kullanıcı belirlendi ise onu kontrol eder.

     * */
    fun checkIsConnectionAvaiable(): Boolean {
        return this.auth.currentUser != null
    }


}