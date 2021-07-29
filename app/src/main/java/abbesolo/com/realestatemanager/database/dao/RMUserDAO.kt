package abbesolo.com.realestatemanager.database.dao

import abbesolo.com.realestatemanager.models.RMUser
import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
@Dao
interface RMUserDAO { // METHODS -------------------------------------------------------------------------------------

    // -- Create --

    /**
     * Usage:
     * val id = dao.insertUser(user)
     */
    @Insert
    suspend fun insertUser(user: RMUser): Long

    /**
     * Usage:
     * val ids = dao.insertUsers(user1, user2)
     */
    @Insert
    suspend fun insertUsers(vararg users: RMUser): List<Long>

    // -- Read --

    /**
     * Usage:
     * dao.getUserById(userId)
     *    .observe(this, Observer { user -> ... })
     */
    @Query("""
        SELECT * 
        FROM user 
        WHERE id_user = :userId
        """)
    fun getUserById(userId: Long): LiveData<RMUser>

    /**
     * Usage:
     * dao.getAllUsers()
     *    .observe(this, Observer { users -> ... })
     */
    @Query("""
        SELECT * 
        FROM user
        """)
    fun getAllUsers(): LiveData<List<RMUser>>

    /**
     * Usage:
     * dao.getUserByIdWithCursor(userId)
     */
    @Query("""
        SELECT * 
        FROM user 
        WHERE id_user = :userId
        """)
    fun getUserByIdWithCursor(userId: Long): Cursor

    /**
     * Usage:
     * dao.getAllUsersWithCursor()
     */
    @Query("""
        SELECT * 
        FROM user 
        """)
    fun getAllUsersWithCursor(): Cursor

    // -- Update --

    /**
     * Usage:
     * val numberOfUpdatedRow = dao.updateUser(user)
     */
    @Update
    suspend fun updateUser(user: RMUser): Int

    // -- Delete --

    /**
     * Usage:
     * val numberOfDeletedRow = dao.deleteUser(user)
     */
    @Delete
    suspend fun deleteUser(user: RMUser): Int

    /**
     * Usage:
     * val numberOfDeletedRow = dao.deleteUserById(userId)
     */
    @Query("""
        DELETE 
        FROM user 
        WHERE id_user = :userId
        """)
    suspend fun deleteUserById(userId: Long): Int
}