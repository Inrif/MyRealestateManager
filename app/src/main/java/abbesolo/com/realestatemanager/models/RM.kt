package abbesolo.com.realestatemanager.models

import androidx.room.*
import java.util.*

/**
 * Created by HOUNSA Romuald on 05/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */

@Entity(tableName = "rm",
    foreignKeys = [ForeignKey(entity = RMUser::class,
        parentColumns = ["id_user"],
        childColumns = ["estate_agent_id"],
        onDelete = ForeignKey.NO_ACTION,
        onUpdate = ForeignKey.CASCADE)],
    indices = [Index(value = ["type", "surface_m2", "number_of_room",
        "loc_latitude", "loc_longitude"],
        unique = true),
        Index(value = ["estate_agent_id"])])

data class RM(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_real_estate")
    var mId: Long = 0L,

    @ColumnInfo(name = "type")
    var type: String? = null,

    @ColumnInfo(name = "price_dollar")
    var price: Double? = null,


    @ColumnInfo(name = "surface_m2")
    var surface: Double? = null,

    @ColumnInfo(name = "number_of_room")
    var roomNumber: Int? = null,

    @ColumnInfo(name = "description")
    var description: String? = null,

    @ColumnInfo(name = "is_enable")
    var isEnable: Boolean? = null,

    @ColumnInfo(name = "effective_date")
    var effectiveDate: Date? = null,

    @ColumnInfo(name = "sale_date")
    var saleDate: Date? = null,

    @ColumnInfo(name = "estate_agent_id")
    var rmAgentId: Long? = null,

    @Embedded(prefix = "loc_")
    var address: Address? = null,

    @Ignore
    var isSelected: Boolean?= false

)