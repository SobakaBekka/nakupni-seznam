package com.example.shoplist

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class ShopListApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
