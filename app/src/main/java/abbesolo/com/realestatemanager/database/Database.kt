package abbesolo.com.realestatemanager.database

import abbesolo.com.realestatemanager.database.dao.*
import abbesolo.com.realestatemanager.models.*
import abbesolo.com.realestatemanager.utils.RMConverters
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Romuald Hounsa on 03/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */

@androidx.room.Database(entities = [RMUser::class,
    RM::class,
    Photo::class,
    POI::class,
    RMAndPoiRefCross::class],
    version = 1,
    exportSchema = false)
@TypeConverters(RMConverters::class)
abstract class Database: RoomDatabase (){

    // DAOs ----------------------------------------------------------------------------------------

    abstract fun userDAO(): RMUserDAO
    abstract fun rmDAO(): RMDAO
    abstract fun photoDAO(): PhotoDAO
    abstract fun poiDAO(): PoiDAO
    abstract fun rmAndPoiRefCrossDAO(): RMAndPoiRefCrossDAO

    // METHODS -------------------------------------------------------------------------------------

    companion object {

        private const val DATABASE_NAME = "RM_Database"

        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: Database? = null

        /**
         * Gets the [Database]
         * @param context a [Context]
         * @return the [Database]
         */
        fun getDatabase(context: Context): Database {
            val tempInstance = INSTANCE

            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                    Database::class.java,
                    DATABASE_NAME)
                    .addCallback(UserDatabaseCallback())
                    .build()

                INSTANCE = instance

                return instance
            }
        }
    }

    // PRIVATE CLASSES -----------------------------------------------------------------------------

    /**
     * A [RoomDatabase.Callback] subclass.
     */
    private class UserDatabaseCallback : RoomDatabase.Callback() {

        // METHODS ---------------------------------------------------------------------------------

        // -- RoomDatabase.Callback --

        // If you only want to populate the database the first time the app is launched.
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    this@UserDatabaseCallback.populateDatabase(database.userDAO())
                }
            }
        }

        // -- User --

        /**
         * Populates the [Database] with an [User]
         * @param RMUserDAO a DAO for the [User] table
         */
        private suspend fun populateDatabase(RMUserDAO: RMUserDAO) {
            // Add a User to the database
            val user = RMUser(username = "User",
                email = "user@gmail.com",
                urlPicture = "")

            RMUserDAO.insertUser(user)
        }
    }
}