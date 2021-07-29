package abbesolo.com.realestatemanager.liveDatas

import abbesolo.com.realestatemanager.models.Photo
import androidx.lifecycle.LiveData


/**
 * Created by HOUNSA Romuald on 19/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 *
 * A [LiveData] of [List] of [Photo] subclass.
 */
class PhotoCreatorLiveData: LiveData<List<Photo>>() {

    // FIELDS --------------------------------------------------------------------------------------

    private val mPhotos = mutableListOf<Photo>()
    private val mAlreadyPresentPhotos = mutableListOf<Photo>()

    // METHODS -------------------------------------------------------------------------------------

    // -- Photo --

    /**
     * Adds all current [Photo]
     * @param currentPhotos a [List] of [Photo]
     */
    fun addCurrentPhotos(currentPhotos: List<Photo>) {
        // MODE EDIT
        with(this.mAlreadyPresentPhotos) {
            clear()
            addAll(currentPhotos)
        }

        // Add photos if possible
        this.mAlreadyPresentPhotos.forEach { photoFromDB ->
            // Search if already present
            val index = this.mPhotos.indexOfFirst {
                it.urlPicture == photoFromDB.urlPicture
            }

            if (index == -1) {
                // New photo
                this.mPhotos.add(photoFromDB)
            }
            else {
                // Update photo
                this.mPhotos[index] = photoFromDB
            }
        }

        // Notify
        this.value = this.mPhotos
    }

    /**
     * Adds a [Photo]
     * @param photo a [Photo]
     */
    fun addPhoto(photo: Photo) {
        // Search presence in photos from database
        val newPhoto = this.mAlreadyPresentPhotos.find {
            it.urlPicture == photo.urlPicture
        } ?: photo

        // Particular case: Photo from database -> removed then added again
        if (newPhoto.id != 0L) {
            newPhoto.description = photo.description
        }

        this.mPhotos.add(newPhoto)

        // Notify
        this.value = this.mPhotos
    }

    /**
     * Updates a [Photo]
     * @param photo a [Photo]
     */
    fun updatePhoto(photo: Photo) {
        // The list does not contain more than one item with the same Url
        val index = this.mPhotos.indexOfFirst { it.urlPicture == photo.urlPicture }
        this.mPhotos[index] = photo

        // Notify
        this.value = this.mPhotos
    }

    /**
     * Deletes a [Photo]
     * @param photo a [Photo]
     */
    fun deletePhoto(photo: Photo) {
        this.mPhotos.remove(photo)

        // Notify
        this.value = this.mPhotos
    }
}