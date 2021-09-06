package abbesolo.com.realestatemanager.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by HOUNSA Romuald on 13/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
@Entity(tableName = "user",
    indices = [Index(value = ["username", "email"],
        unique = true)])

data class RMUser (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_user")
    var id: Long = 0L,

    @ColumnInfo(name = "username")
    var username: String? = null,

    @ColumnInfo(name = "email")
    var email: String? = null,

    @ColumnInfo(name = "url_picture")
    var urlPicture: String? = null,

    @ColumnInfo(name = "password")
    var password: String? = null
)