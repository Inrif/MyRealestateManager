package abbesolo.com.realestatemanager.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Created by Romuald Hounsa on 01/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 *  * Many-to-many relationships: many [RealEstate] and many [poi]
 */
data class RMAndPoi(
    @Embedded
    var rm: RM? = null,

    @Relation(parentColumn = "id_real_estate",
        entityColumn = "id_point_of_interest",
        associateBy = Junction(RMAndPoiRefCross::class)
    )
    var poi: List<POI>? = null
)