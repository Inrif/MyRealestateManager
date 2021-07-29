package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.database.dao.RMUserDAO
import abbesolo.com.realestatemanager.models.RMUser
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class RMUserRepositoryImpl (
    private val mUserDAO: RMUserDAO
) : RMUserRepository {

    // METHODS -------------------------------------------------------------------------------------

    // -- Create --

    override suspend fun insertUser(user: RMUser): Long = withContext(Dispatchers.IO) {
        this@RMUserRepositoryImpl.mUserDAO.insertUser(user)
    }

    // -- Read --

    override fun getUserById(userId: Long): LiveData<RMUser> = this.mUserDAO.getUserById(userId)

    // -- Update --

    override suspend fun updateUser(user: RMUser): Int = withContext(Dispatchers.IO) {
        this@RMUserRepositoryImpl.mUserDAO.updateUser(user)
    }

    // -- Delete --

    override suspend fun deleteUser(user: RMUser): Int = withContext(Dispatchers.IO) {
        this@RMUserRepositoryImpl.mUserDAO.deleteUser(user)
    }
}