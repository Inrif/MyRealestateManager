package abbesolo.com.realestatemanager.models

import androidx.room.*

/**
 * Created by Romuald Hounsa on 01/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 * It is the point of interest at proximity (school, park, business...).
 *
 * Many-to-many relationships: many [RealEstate] and many [POI]
 */

@Entity(tableName = "point_of_interest",
    indices = [Index(value = ["name",
        "loc_latitude", "loc_longitude"],
        unique = true)])
data class POI (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_point_of_interest")
    var id: Long = 0L,

    @ColumnInfo(name = "name")
    var name: String? = null,

    @ColumnInfo(name = "url_picture")
    var urlPicture: String? = null,

    @Embedded(prefix = "loc_")
    var address: Address? = null,

    @Ignore
    var isSelected: Boolean = true
) {
    // NESTED CLASSES
    /**
     * A [Comparator] of [POI] subclass.
     */
    class AZTitleComparator : Comparator<POI> {
        override fun compare(left: POI?, right: POI?): Int {
            // Comparaison on the name
            val titleLeft = left?.name ?: ""
            val titleRight = right?.name ?: ""

            return titleLeft.compareTo(titleRight, ignoreCase = true)
        }
    }
}
