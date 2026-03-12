package com.example.myfirstkmpapp

actual fun getPlatformName(): String {
    return "Java ${System.getProperty("java.version")}"
}