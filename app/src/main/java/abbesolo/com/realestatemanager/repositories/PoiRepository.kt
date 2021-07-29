package abbesolo.com.realestatemanager.repositories


import abbesolo.com.realestatemanager.models.POI
import androidx.lifecycle.LiveData

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
interface PoiRepository { // METHODS -------------------------------------------------------------------------------------

    // -- Create --

    suspend fun insertPointOfInterest(poi: POI): Long

    // -- Read --

    fun getAllPointsOfInterest(): LiveData<List<POI>>
}