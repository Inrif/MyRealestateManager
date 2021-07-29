package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.models.RMUser
import androidx.lifecycle.LiveData

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
interface RMUserRepository { // METHODS -------------------------------------------------------------------------------------

    // -- Create --

    suspend fun insertUser(user: RMUser): Long

    // -- Read --

    fun getUserById(userId: Long): LiveData<RMUser>

    // -- Update --

    suspend fun updateUser(user: RMUser): Int

    // -- Delete --

    suspend fun deleteUser(user: RMUser): Int
}