package abbesolo.com.realestatemanager.utils

import androidx.room.TypeConverter
import java.util.*

/**
 * Created by HOUNSA Romuald on 20/07/21.
 */
class RMConverters {
    /**
     * Converts an [Long] to a [Date]
     * @param value a [Long]
     * @return a [Date]
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    /**
     * Converts a [Date] to an [Long]
     * @param date a [Date]
     * @return a [Long]
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}