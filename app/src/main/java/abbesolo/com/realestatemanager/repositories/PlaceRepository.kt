package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.models.POI
import io.reactivex.Single

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
interface PlaceRepository {
    // METHODS -------------------------------------------------------------------------------------

    /**
     * Gets stream to Fetch the [List] of [POI]
     * @param location  a [String] that contains the latitude/longitude around which to retrieve place information
     * @param radius    a [Double] that defines the distance (in meters) within which to return place results
     * @param types     a [String] that restricts the results to places matching the specified type
     * @param key       a [String] that contains your application's API key
     * @return a [Single] of [List] of [POI]
     */
    fun getStreamToFetchPointsOfInterest(
        location: String,
        radius: Double,
        types: String,
        key: String
    ): Single<List<POI>>
}
