package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.googleMapsApi.MapApi
import abbesolo.com.realestatemanager.models.POI
import abbesolo.com.realestatemanager.utils.MapperTools
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class MapApiRepository : PlaceRepository {


    private val mMapApi = MapApi.retrofit
        .create(MapApi::class.java)

    override fun getStreamToFetchPointsOfInterest(
        location: String,
        radius: Double,
        types: String,
        key: String
    ): Single<List<POI>> {
        return this.mMapApi
            .getNearbySearch(location, radius, types, key)
           .map { MapperTools.nearbySearchToPointsOfInterest(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(10L, TimeUnit.SECONDS)
    }
}