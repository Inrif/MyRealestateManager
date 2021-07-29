package abbesolo.com.realestatemanager.database.dao

import abbesolo.com.realestatemanager.models.POI
import abbesolo.com.realestatemanager.models.RMAndPoiRefCross
import androidx.room.Dao
import androidx.room.Insert

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
@Dao
interface RMAndPoiRefCrossDAO {// METHODS -------------------------------------------------------------------------------------

    // -- Create --

    /**
     * Usage:
     * val id = dao.insertCrossRef(crossRef)
     */
    @Insert
    suspend fun insertCrossRef(mAssoDAO: RMAndPoiRefCross): Long

    /**
     * Usage:
     * val id = dao.insertSeveralCrossRef(crossRef1, crossRef2)
     */
    @Insert
    suspend fun insertSeveralCrossRef(vararg severalCrossRef: RMAndPoiRefCross): List<Long>
}