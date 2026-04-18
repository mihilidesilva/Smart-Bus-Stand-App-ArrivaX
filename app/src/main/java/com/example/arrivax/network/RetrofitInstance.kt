package com.example.arrivax.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // TODO: Replace this with your actual base URL in a constants file.
    private const val BASE_URL = "https://your-api-base-url.com/"

    /**
     * A lazy-initialized Retrofit instance.
     * This ensures the Retrofit object is only created when it's first needed.
     */
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * A public accessor for the ApiService interface.
     * This will be used by the rest of the app to make network calls.
     */
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}