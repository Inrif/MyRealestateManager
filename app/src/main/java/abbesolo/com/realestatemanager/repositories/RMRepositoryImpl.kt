package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.database.dao.RMDAO
import abbesolo.com.realestatemanager.models.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class RMRepositoryImpl (
    private val rmDAO: RMDAO
) : RMRepository {

    // METHODS -------------------------------------------------------------------------------------

    // -- Create --

    override suspend fun insertRealEstate(
        rm: RM
    ): Long = withContext(Dispatchers.IO) {
        this@RMRepositoryImpl.rmDAO.insertRealEstate(rm)
    }


    // -- Read --

    override fun getRealEstatesAndPhotosByUserId(
        userId: Long
    ): LiveData<List<RMAndPhotos>> {
        return this.rmDAO.getRealEstatesWithPhotosByUserId(userId)
    }

    override fun getRealEstateAndPhotosById(realEstateId: Long): LiveData<RMAndPhotos> {
        return this.rmDAO.getRealEstateWithPhotosById(realEstateId)
    }

 
    override fun getRealEstateAndInterestPointById(
            realEstateId: Long
        ): LiveData<RMAndPoi> {
            return this.rmDAO.getRealEstateWithPointsOfInterestById(realEstateId)
        }



    override fun getRealEstatesAndPhotosByMultiSearch(
        minPrice: Double,
        maxPrice: Double,
        minSurface: Double,
        maxSurface: Double,
        minNumberRoom: Int,
        maxNumberRoom: Int
    ): LiveData<List<RMAndPhotos>> {
        return this.rmDAO.getRealEstatesWithPhotosByMultiSearch(
            minPrice,
            maxPrice,
            minSurface,
            maxSurface,
            minNumberRoom,
            maxNumberRoom
        )
    }


    // -- Update --

    override suspend fun updateRealEstate(
        rm: RM
    ): Int = withContext(Dispatchers.IO) {
        this@RMRepositoryImpl.rmDAO.updateRealEstate(rm)
    }
}