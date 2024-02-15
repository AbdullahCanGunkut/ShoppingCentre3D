package com.example.shoppingcentre3d

import android.os.Bundle

import com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent
import com.example.shoppingcentre3d.databinding.ActivityMainPageBinding

class MainActivity : AppCompatActivity() {


    private lateinit var shoppingCentreContextInst: ShoppingCentre3DContext
    private lateinit var binding: ActivityMainPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPageBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar as Toolbar);
        shoppingCentreContextInst = ShoppingCentre3DContext.createInstance(this, "")!!
        /*     val lst: ProductList = listOf(
                 Product(
                     "0001", "tahta sandalye", 15f, 8f,
                     listOf("sandalye", "tahta", "rahat"), "Koltuk",
                     "",
                     listOf(
                         ConciseDateObject<String, String>("", "back"),
                         ConciseDateObject<String, String>("", "front"),
                     )
                 ),
                 Product(
                     "0002", "dekoratif masa", 32f, 16f,
                     listOf("masa", "tahta", "demir"), "Masa",
                     "",
                     listOf(
                         ConciseDateObject<String, String>("", "front"),
                     )
                 ),
                 Product(
                     "0003", "dolap ", 120f, 90f,
                     listOf("sandalye", "tahta", "rahat"), "Koltuk",
                     "",
                     listOf(
                         ConciseDateObject<String, String>("", "front_opened"),
                         ConciseDateObject<String, String>("", "front_closed"),
                     )
                 ),
             )

             var fil = FileManagement(lst)
             var gson = Gson()
             Log.i("My Gson : ", gson.toJson(fil))*/



        setContentView(binding.root)

        //     hoppingCentreContextInst.createUser("senigidifinduk@hotmail.com" , "455756şişi")
        // RenderContext oluştur

    }



    fun goMainPage(view: android.view.View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun goUserSettings(view: android.view.View) {
        val intent = Intent(this, UserActivity::class.java)
        startActivity(intent)
    }

    fun goBasket(view: android.view.View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


}
