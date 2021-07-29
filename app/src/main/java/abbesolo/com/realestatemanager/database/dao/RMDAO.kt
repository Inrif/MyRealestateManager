package abbesolo.com.realestatemanager.database.dao

import abbesolo.com.realestatemanager.models.*
import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
@Dao
interface RMDAO {
    // METHODS -------------------------------------------------------------------------------------

    // -- Create --

    /**
     * Usage:
     * val id = dao.insertRealEstate(realEstate)
     */
    @Insert
    suspend fun insertRealEstate(realEstate: RM): Long

    /**
     * Usage:
     * val ids = dao.insertRealEstates(realEstate1, realEstate2)
     */
    @Insert
    suspend fun insertRealEstates(vararg realEstates: RM): List<Long>

    // -- Read --

    /**
     * Usage:
     * dao.getRealEstateById(realEstateId)
     *    .observe(this, Observer { realEstate -> ... })
     */
    @Query("""
        SELECT * 
        FROM rm 
        WHERE id_real_estate = :realEstateId
        """)
    fun getRealEstateById(realEstateId: Long): LiveData<RM>

    /**
     * Usage:
     * dao.getAllRealEstates()
     *    .observe(this, Observer { realEstates -> ... })
     */
    @Query("""
        SELECT * 
        FROM rm
        """)
    fun getAllRealEstates(): LiveData<List<RM>>

    /**
     * Usage:
     * dao.getCountOfRealEstatesByUserId(userId)
     *    .observe(this, Observer { count -> ... })
     */
    @Query("""
        SELECT count(*) 
        FROM rm 
        WHERE estate_agent_id = :userId
        """)
    fun getCountOfRealEstatesByUserId(userId: Long): LiveData<Int>

    /**
     * Usage:
     * dao.getIdTypeAddressPriceTupleOfRealEstateByUserId(userId)
     *    .observe(this, Observer { tuples -> ... })
     */
    @Query("""
        SELECT id_real_estate,
               type,
               price_dollar,
               loc_street, loc_city, loc_post_code, loc_country, loc_latitude, loc_longitude
        FROM rm
        WHERE estate_agent_id = :userId
        """)
    fun getRealEstateIdTypePriceAdressByUserId(
        userId: Long
    ): LiveData<List<RMIdTypePriceAdress>>

    /**
     * Usage:
     * dao.getRealEstatesWithPhotosByUserId(userId)
     *    .observe(this, Observer { realEstatesWithPhotos -> ... })
     */
    @Transaction
    @Query("""
        SELECT * 
        FROM rm 
        WHERE estate_agent_id = :userId
        """)
    fun getRealEstatesWithPhotosByUserId(
        userId: Long
    ): LiveData<List<RMAndPhotos>>

    /**
     * Usage:
     * dao.getRealEstatesWithPointsOfInterestByUserId(userId)
     *    .observe(this, Observer { realEstatesWithPointsOfInterest -> ... })
     */
    @Transaction
    @Query("""
        SELECT * 
        FROM rm 
        WHERE estate_agent_id = :userId
        """)
    fun getRealEstatesWithPointsOfInterestByUserId(
        userId: Long
    ): LiveData<List<RMAndPoi>>

    /**
     * Usage:
     * dao.getRealEstateWithPhotosById(realEstateId)
     *    .observe(this, Observer { RealEstateWithPhotos -> ... })
     */
    @Transaction
    @Query("""
        SELECT * 
        FROM rm 
        WHERE id_real_estate = :realEstateId
        """)
    fun getRealEstateWithPhotosById(
        realEstateId: Long
    ): LiveData<RMAndPhotos>

    /**
     * Usage:
     * dao.getRealEstateWithPointsOfInterestById(realEstateId)
     *    .observe(this, Observer { realEstateWithPointsOfInterest -> ... })
     */
    @Transaction
    @Query("""
        SELECT * 
        FROM rm 
        WHERE id_real_estate = :realEstateId
        """)
    fun getRealEstateWithPointsOfInterestById(
        realEstateId: Long
    ): LiveData<RMAndPoi>

    /**
     * Usage:
     * dao.getRealEstatesWithPhotosByMultiSearch(
     *      minPrice,
     *      maxPrice,
     *      minSurface,
     *      maxSurface,
     *      minNumberRoom,
     *      maxNumberRoom)
     *    .observe(this, Observer { RealEstateWithPhotos -> ... })
     */
    @Transaction
    @Query("""
        SELECT * 
        FROM rm 
        WHERE 
            price_dollar BETWEEN :minPrice AND :maxPrice
            AND 
            surface_m2 BETWEEN :minSurface AND :maxSurface
            AND 
            number_of_room BETWEEN :minNumberRoom AND :maxNumberRoom
        """)
    fun getRealEstatesWithPhotosByMultiSearch(
        minPrice: Double = 0.0,
        maxPrice: Double = Double.MAX_VALUE,
        minSurface: Double = 0.0,
        maxSurface: Double = Double.MAX_VALUE,
        minNumberRoom: Int = 0,
        maxNumberRoom: Int = Int.MAX_VALUE
    ): LiveData<List<RMAndPhotos>>

    // -- Update --

    /**
     * Usage:
     * val numberOfUpdatedRow = dao.updateRealEstate(realEstate)
     */
    @Update
    suspend fun updateRealEstate(realEstate: RM): Int

    // -- Delete --

    /**
     * Usage:
     * val numberOfDeletedRow = dao.deleteRealEstate(realEstate)
     */
    @Delete
    suspend fun deleteRealEstate(realEstate: RM): Int

}