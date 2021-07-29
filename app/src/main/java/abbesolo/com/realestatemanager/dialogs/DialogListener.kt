package abbesolo.com.realestatemanager.dialogs

import abbesolo.com.realestatemanager.models.Photo



/**
 * Created by HOUNSA Romuald on 19/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 *
 */
interface DialogListener {

    // METHODS -------------------------------------------------------------------------------------

    /**
     * Called when the user has selected a [Photo]
     * @param photo the selected [Photo]
     * @param mode  a [RMPhotoDialog.PhotoDialogMode]
     */
    fun getSelectedPhotoFromDialog(photo: Photo, mode: RMPhotoDialog.PhotoDialogMode)

}