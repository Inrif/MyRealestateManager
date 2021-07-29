package abbesolo.com.realestatemanager.database.dao

import abbesolo.com.realestatemanager.models.POI
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020  abbesolo.com.realestatemanager. All rights reserved.
 *  * DAO of [POI].
 */
@Dao
interface PoiDAO {// METHODS -------------------------------------------------------------------------------------

    // -- Create --

    /**
     * Usage:
     * val id = dao.insertPointOfInterest(pointOfInterest)
     */
    @Insert
    suspend fun insertInterestPoint(poi: POI): Long

    /**
     * Usage:
     * val ids = dao.insertPointsOfInterest(pointOfInterest1, pointOfInterest2)
     */
    @Insert
    suspend fun insertInterestPoint(vararg poi: POI): List<Long>

    // -- Read --

    /**
     * Usage:
     * dao.getAllPointsOfInterest()
     *    .observe(this, Observer { pointsOfInterest -> ... })
     */
    @Query("""
        SELECT * 
        FROM point_of_interest
        """)
    fun getAllInterestPoint(): LiveData<List<POI>>
}