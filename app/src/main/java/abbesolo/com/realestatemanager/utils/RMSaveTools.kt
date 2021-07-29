package abbesolo.com.realestatemanager.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Created by HOUNSA Romuald on 19/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
object RMSaveTools {
    // FIELDS --------------------------------------------------------------------------------------

    private const val SAVE_FILE_NAME = "abbesolo.com.realestatemanager.SAVE_FILE_NAME"

    // METHODS -------------------------------------------------------------------------------------

    /**
     * Gets the [SharedPreferences]
     * @param context a [Context]
     * @return the [SharedPreferences]
     */
    private fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(SAVE_FILE_NAME, Context.MODE_PRIVATE)

    /**
     * Saves a [Boolean] thanks to SharedPreferences
     * @param context   a [Context]
     * @param key       a [String] that contains the key
     * @param value     a [Boolean] that contains the value
     */
    fun saveBooleanIntoSharedPreferences(
        context: Context,
        key: String,
        value: Boolean
    ) {
        getSharedPreferences(context).edit { putBoolean(key, value) }
    }

    /**
     * Fetches a [Boolean] from SharedPreferences
     * @param context   a [Context]
     * @param key       a [String] that contains the key
     * @return a [Boolean] that contains the value
     */
    fun fetchBooleanFromSharedPreferences(
        context: Context,
        key: String
    ): Boolean = getSharedPreferences(context).getBoolean(key, true)
}