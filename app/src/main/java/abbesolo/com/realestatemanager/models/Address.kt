package abbesolo.com.realestatemanager.models

import androidx.room.ColumnInfo

/**
 * Created by Romuald Hounsa on 01/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
data class Address (

    @ColumnInfo(name = "street")
    val street: String? = null,

    @ColumnInfo(name = "city")
    val city: String? = null,

    @ColumnInfo(name = "post_code")
    val postCode: Int? = null,

    @ColumnInfo(name = "country")
    val country: String? = null,

    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,

    @ColumnInfo(name = "longitude")
    val longitude: Double? = null

)