package abbesolo.com.realestatemanager.database.dao

import abbesolo.com.realestatemanager.models.Photo
import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
@Dao
interface PhotoDAO {
    // METHODS -------------------------------------------------------------------------------------

    // -- Create --

    /**
     * Usage:
     * val id = dao.insertPhoto(photo)
     */
    @Insert
    suspend fun insertPhoto(photo: Photo): Long

    /**
     * Usage:
     * val ids = dao.insertPhotos(photo1, photo2)
     */
    @Insert
    suspend fun insertPhotos(vararg photos: Photo): List<Long>

    // -- Read --

    /**
     * Usage:
     * dao.getPhotoByRealEstateId(realEstateId)
     *    .observe(this, Observer { photos -> ... })
     */
    @Query("""
        SELECT * 
        FROM photo 
        WHERE real_estate_id = :realEstateId
        """)
    fun getPhotoByRealEstateId(realEstateId: Long): LiveData<List<Photo>>

    /**
     * Usage:
     * dao.getAllPhotos()
     *    .observe(this, Observer { photos -> ... })
     */
    @Query("""
        SELECT * 
        FROM photo
        """)
    fun getAllPhotos(): LiveData<List<Photo>>

    // -- Update --

    /**
     * Usage:
     * val numberOfUpdatedRow = dao.updatePhoto(photo)
     */
    @Update
    suspend fun updatePhoto(photo: Photo): Int

    // -- Delete --

    /**
     * Usage:
     * val numberOfDeletedRow = dao.deletePhoto(photo)
     */
    @Delete
    suspend fun deletePhoto(photo: Photo): Int


}