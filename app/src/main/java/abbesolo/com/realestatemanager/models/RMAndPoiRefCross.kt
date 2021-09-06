package abbesolo.com.realestatemanager.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Created by Romuald Hounsa on 01/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 * It is cross-reference table.
 *
 * Many-to-many relationships: many [RM] and many [POI]
 */

@Entity(tableName = "rm_poi_join",
    primaryKeys = ["id_real_estate", "id_point_of_interest"],
    foreignKeys = [ForeignKey(entity = RM::class,
        parentColumns = ["id_real_estate"],
        childColumns = ["id_real_estate"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = POI::class,
            parentColumns = ["id_point_of_interest"],
            childColumns = ["id_point_of_interest"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE)],
    indices = [Index(value = ["id_real_estate"]),
        Index(value = ["id_point_of_interest"])])

 data class RMAndPoiRefCross (
    @ColumnInfo(name = "id_real_estate")
    var rmId: Long = 0L,

    @ColumnInfo(name = "id_point_of_interest")
    var poiId: Long = 0L

)