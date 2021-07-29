package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.models.RMAndPoiRefCross

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
interface RMAndPoiRefCrossRespository {
// METHODS -------------------------------------------------------------------------------------

    // -- Create --

    suspend fun insertCrossRef(crossRef: RMAndPoiRefCross): Long
}