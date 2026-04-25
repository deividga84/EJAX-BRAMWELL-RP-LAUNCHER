package com.ejax.bramwell.launcher.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ejax.bramwell.launcher.R

class SplashActivity : AppCompatActivity() {
    private val splashTimeOut: Long = 3000 // 3 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, splashTimeOut)
    }

    override fun onBackPressed() {
        // Impede que o usuário volte da splash screen
    }
}
