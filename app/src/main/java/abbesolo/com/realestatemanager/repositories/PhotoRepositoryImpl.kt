package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.database.dao.PhotoDAO
import abbesolo.com.realestatemanager.models.Photo
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class PhotoRepositoryImpl  (
    private val mPhotoDAO: PhotoDAO
) : PhotoRepository {

    // METHODS -------------------------------------------------------------------------------------

    // -- Create --

    override suspend fun insertPhotos(
        vararg photos: Photo
    ): List<Long> = withContext(Dispatchers.IO) {
        this@PhotoRepositoryImpl.mPhotoDAO.insertPhotos(*photos)
    }

    // -- Read --

    override fun getAllPhotos(): LiveData<List<Photo>> = this.mPhotoDAO.getAllPhotos()

    // -- Update --

    override suspend fun updatePhoto(photo: Photo): Int = withContext(Dispatchers.IO) {
        this@PhotoRepositoryImpl.mPhotoDAO.updatePhoto(photo)
    }

    // -- Delete --

    override suspend fun deletePhoto(photo: Photo): Int = withContext(Dispatchers.IO) {
        this@PhotoRepositoryImpl.mPhotoDAO.deletePhoto(photo)
    }
}