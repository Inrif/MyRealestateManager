package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.models.*
import androidx.lifecycle.LiveData

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
interface RMRepository {

// METHODS -------------------------------------------------------------------------------------

    // -- Create --

    suspend fun insertRealEstate(rm: RM): Long

    // -- Read --

    fun getRealEstatesAndPhotosByUserId(userId: Long): LiveData<List<RMAndPhotos>>

    fun getRealEstateAndPhotosById(realEstateId: Long): LiveData<RMAndPhotos>

    fun getRealEstateAndInterestPointById(
        realEstateId: Long
    ): LiveData<RMAndPoi>

    fun getRealEstatesAndPhotosByMultiSearch(
        minPrice: Double = 0.0,
        maxPrice: Double = Double.MAX_VALUE,
        minSurface: Double = 0.0,
        maxSurface: Double = Double.MAX_VALUE,
        minNumberRoom: Int = 0,
        maxNumberRoom: Int = Int.MAX_VALUE
    ): LiveData<List<RMAndPhotos>>

    // -- Update --

    suspend fun updateRealEstate(rm: RM): Int
}