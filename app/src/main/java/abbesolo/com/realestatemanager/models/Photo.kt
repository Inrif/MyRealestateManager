package abbesolo.com.realestatemanager.models

import androidx.room.*

/**
 * Created by Romuald Hounsa on 01/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */

@Entity(tableName = "photo",
    foreignKeys = [ForeignKey(entity = RM::class,
        parentColumns = ["id_real_estate"],
        childColumns = ["real_estate_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE)],
    indices = [Index(value = ["url_picture"],
        unique = true),
        Index(value = ["real_estate_id"])])

data class Photo (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_photo")
    var id: Long = 0L,

    @ColumnInfo(name = "url_picture")
    val urlPicture: String? = null,

    @ColumnInfo(name = "description")
    var description: String? = null,

    @ColumnInfo(name = "real_estate_id")
    var reId: Long? = null

)