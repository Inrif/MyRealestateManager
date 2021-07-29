package abbesolo.com.realestatemanager.viewModel

import abbesolo.com.realestatemanager.repositories.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by HOUNSA Romuald on 19/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */

class RMViewModelFactory(
private val placeRepository: PlaceRepository,
private val userRepository: RMUserRepository,
private val rmRepository: RMRepository,
private val photoRepository: PhotoRepository,
private val poiRepository: PoiRepository,
private val rmAndPoiRefCrossRepository: RMAndPoiRefCrossRespository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RMViewModel::class.java)) {
            return RMViewModel(
                this.placeRepository,
                this.userRepository,
                this.rmRepository,
                this.photoRepository,
                this.poiRepository,
                this.rmAndPoiRefCrossRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }

}