package com.example.shoppingcentre3d

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.shoppingcentre3d.databinding.ActivityUserBinding
import androidx.appcompat.widget.Toolbar;
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.navigation.NavOptions


/** Kullanıcı bilgilerinin hepsini kontrol eden aktivite. (Giriş , kayıt , ayarlar vs)
 *
 */
class UserActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserBinding
    val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_right)
        .setExitAnim(R.anim.slide_left)
        .setPopEnterAnim(R.anim.slide_right)
        .setPopExitAnim(R.anim.slide_left)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar as Toolbar)

    }


    ///Herhangi bir card'ın üstüne basıldığı zaman (içinde birden fazla kart tutan card'lar) içindeki verileri açıp kapamk için yapılır.
    fun toggleCard(view: View) {
        val parentView = view as? ViewGroup
        if (parentView != null && parentView!!.childCount > 0) {

            var child = (parentView?.getChildAt(0) as ViewGroup).getChildAt(1)

            if (child?.visibility == View.VISIBLE)
                child?.visibility = View.GONE
            else
                child?.visibility = View.VISIBLE
        }

    }


/*Main activitye dönderir*/
    fun goMainPage(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun goUserSettings(view: View) {
        /* val intent = Intent(this, UserActivity::class.java)
         startActivity(intent)
    */
    }
/*
    fun goBasket(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
*/}