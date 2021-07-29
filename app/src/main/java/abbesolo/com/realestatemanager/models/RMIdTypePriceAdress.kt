package abbesolo.com.realestatemanager.models

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Created by Romuald Hounsa on 02/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class RMIdTypePriceAdress (

    @ColumnInfo(name = "id_real_estate")
    var mId: Long = 0L,

    @ColumnInfo(name = "type")
    var type: String? = null,

    @ColumnInfo(name = "price_dollar")
    var price: Double? = null,

    @Embedded(prefix = "loc_")
    var address: Address? = null
)