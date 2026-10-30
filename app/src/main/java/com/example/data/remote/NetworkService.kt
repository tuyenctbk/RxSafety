package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RxNavApi {
    @GET("REST/approximateTerm.json")
    suspend fun getApproximateTerm(
        @Query("term") term: String,
        @Query("maxEntries") maxEntries: Int = 10
    ): RxNavApproximateResponse

    @GET("REST/drugs.json")
    suspend fun getDrugsByName(
        @Query("name") name: String
    ): RxNavDrugsResponse

    @GET("REST/interaction/list.json")
    suspend fun getInteractionList(
        @Query("rxcuis") rxcuis: String
    ): RxNavInteractionListResponse

    @GET("REST/interaction/interaction.json")
    suspend fun getInteractionsForDrug(
        @Query("rxcui") rxcui: String
    ): RxNavInteractionListResponse
}

interface OpenFdaApi {
    @GET("drug/enforcement.json")
    suspend fun getEnforcementReports(
        @Query("search") search: String? = null,
        @Query("sort") sort: String = "report_date:desc",
        @Query("limit") limit: Int = 25
    ): OpenFdaEnforcementResponse

    @GET("drug/label.json")
    suspend fun getDrugLabel(
        @Query("search") search: String,
        @Query("limit") limit: Int = 1
    ): OpenFdaLabelResponse
}

object NetworkClient {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val rxNavApi: RxNavApi = Retrofit.Builder()
        .baseUrl("https://rxnav.nlm.nih.gov/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(RxNavApi::class.java)

    val openFdaApi: OpenFdaApi = Retrofit.Builder()
        .baseUrl("https://api.fda.gov/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OpenFdaApi::class.java)
}
