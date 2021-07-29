package abbesolo.com.realestatemanager.models

import android.location.Location
import com.squareup.moshi.JsonClass

/**
 * Created by Romuald Hounsa on 01/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
@JsonClass(generateAdapter = true)
data class LocationData(
    val mLocation: Location? = null,
    val mException: Exception? = null
)