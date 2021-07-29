package abbesolo.com.realestatemanager.models

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Created by Romuald Hounsa on 01/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 *
 *  * One-to-many relationships: one [RealEstate] and many [Photo]
 */
data class RMAndPhotos (
    @Embedded
    var rm: RM? = null,

    @Relation(parentColumn = "id_real_estate",
        entityColumn = "real_estate_id")
    var photos: List<Photo>? = null
)