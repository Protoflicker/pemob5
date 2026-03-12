package com.example.myfirstkmpapp

import android.os.Build

actual fun getPlatformName(): String {
    return "Android ${Build.VERSION.SDK_INT}"
}