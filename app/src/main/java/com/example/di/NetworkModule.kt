package com.example.di

import com.example.data.remote.NetworkClient
import com.example.data.remote.OpenFdaApi
import com.example.data.remote.RxNavApi

object NetworkModule {

    fun provideRxNavApi(): RxNavApi {
        return NetworkClient.rxNavApi
    }

    fun provideOpenFdaApi(): OpenFdaApi {
        return NetworkClient.openFdaApi
    }
}
