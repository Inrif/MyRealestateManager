package abbesolo.com.realestatemanager.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by HOUNSA Romuald on 19/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
object RMUtils {
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





    fun convertDollarToEuro(dollars: Int): Int = (dollars.toFloat() * 0.812F).roundToInt()

    fun convertEuroToDollar(euros: Int): Int = (euros.toFloat() / 0.812F).roundToInt()



// -- Date --

    /**
     * Converts the current date in yyyy/MM/dd format
     * @return a [String] that contains the format
     */
    @VisibleForTesting
    fun getTodayDateYYYYMMDD(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * Converts the current date in dd/MM/yyyy format
     * @return a [String] that contains the format
     */
    @VisibleForTesting
    fun getTodayDateDDMMYYYY(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

// -- Network --

    /**
     * Checks the network connection
     * @param context a [Context]
     * @return a [Boolean] that returns true is there is a network connection
     */
    @VisibleForTesting

    fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                return@run connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }



}