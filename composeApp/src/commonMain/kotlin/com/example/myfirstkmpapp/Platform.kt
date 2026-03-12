package com.example.myfirstkmpapp

expect fun getPlatformName(): String

fun greet(): String {
    return "Hello from ${getPlatformName()}!"
}