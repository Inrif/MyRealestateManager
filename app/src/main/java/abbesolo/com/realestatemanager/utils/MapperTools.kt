package abbesolo.com.realestatemanager.utils

import abbesolo.com.realestatemanager.models.Address
import abbesolo.com.realestatemanager.models.NearbySearch
import abbesolo.com.realestatemanager.models.POI
import com.google.android.gms.maps.model.PointOfInterest

/**
 * Created by HOUNSA Romuald on 13/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
object MapperTools {
    /**
     * A mapper to convert [NearbySearch] to [List] of [PointOfInterest]
     * @param nearbySearch a [NearbySearch]
     * @return a [List] of [PointOfInterest]
     */
    fun nearbySearchToPointsOfInterest(nearbySearch: NearbySearch): List<POI> {
        return mutableListOf<POI>().apply {
            nearbySearch.results?.forEach {
                this.add(
                    POI(
                        name = it.name ?: "Point of interest",
                        urlPicture = it.photos?.get(0)?.photoReference,
                        address = Address(
                            latitude  = it.geometry?.location?.lat ?: 0.0,
                            longitude = it.geometry?.location?.lng ?: 0.0
                        ),
                        isSelected = false
                    )
                )
            }
        }
    }
}