package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.database.dao.RMAndPoiRefCrossDAO
import abbesolo.com.realestatemanager.models.RMAndPoiRefCross
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class RMAndPoiRefCrossRespositoryImpl (
    private val mRMAndPoiRefCrossDAO: RMAndPoiRefCrossDAO
) : RMAndPoiRefCrossRespository {

    // METHODS -------------------------------------------------------------------------------------

    // -- Create --

    override suspend fun insertCrossRef(
        crossRef: RMAndPoiRefCross
    ): Long = withContext(Dispatchers.IO) {
        this@RMAndPoiRefCrossRespositoryImpl.mRMAndPoiRefCrossDAO.insertCrossRef(crossRef)
    }
}