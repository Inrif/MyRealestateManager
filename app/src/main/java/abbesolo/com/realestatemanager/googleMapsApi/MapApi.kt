package abbesolo.com.realestatemanager.googleMapsApi

import abbesolo.com.realestatemanager.models.NearbySearch
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by HOUNSA Romuald on 08/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
interface MapApi {
    // FIELDS --------------------------------------------------------------------------------------

    companion object {

        private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()


        fun getPhoto(
            photoReference: String,
            maxWidth: Int,
            key: String
        ) = "${BASE_URL}place/photo?photoreference=${photoReference}&maxwidth=${maxWidth}&key=${key}"
    }

    // METHODS -------------------------------------------------------------------------------------


    @GET("place/nearbysearch/json?")
    fun getNearbySearch(
        @Query("location") location: String,
        @Query("radius") radius: Double,
        @Query("types") types: String,
        @Query("key") key: String
    ): Single<NearbySearch>

}