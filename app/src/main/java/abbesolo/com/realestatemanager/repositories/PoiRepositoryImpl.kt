package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.database.dao.PoiDAO
import abbesolo.com.realestatemanager.models.POI
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class PoiRepositoryImpl (
    private val poiDAO: PoiDAO
) : PoiRepository {

    // METHODS -------------------------------------------------------------------------------------

    // -- Create --

    override suspend fun insertPointOfInterest(
        poi: POI
    ): Long = withContext(Dispatchers.IO) {
        this@PoiRepositoryImpl.poiDAO.insertInterestPoint(poi)
    }

    // -- Read --

    override fun getAllPointsOfInterest(): LiveData<List<POI>> =
        this.poiDAO.getAllInterestPoint()
}