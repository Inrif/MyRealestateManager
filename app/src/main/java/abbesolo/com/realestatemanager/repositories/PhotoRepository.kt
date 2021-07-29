package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.models.Photo
import androidx.lifecycle.LiveData


/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
interface PhotoRepository {

// METHODS -------------------------------------------------------------------------------------

    // -- Create --

    suspend fun insertPhotos(vararg photos: Photo): List<Long>

    // -- Read --

    fun getAllPhotos(): LiveData<List<Photo>>

    // -- Update --

    suspend fun updatePhoto(photo: Photo): Int

    // -- Delete --

    suspend fun deletePhoto(photo: Photo): Int
}