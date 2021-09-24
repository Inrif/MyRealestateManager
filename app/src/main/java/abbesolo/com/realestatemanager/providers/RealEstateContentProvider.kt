package abbesolo.com.realestatemanager.providers

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import abbesolo.com.realestatemanager.database.Database
import abbesolo.com.realestatemanager.models.Address
import abbesolo.com.realestatemanager.models.RM
import abbesolo.com.realestatemanager.models.RMUser
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*


class RealEstateContentProvider : ContentProvider() {

    //https://developer.android.com/guide/topics/providers/content-provider-basics

    // ENUMS ---------------------------------------------------------------------------------------

    enum class Table(val mRealesate: String) {
        RM("rm"),
        RM_ID("rm/#"),
    }

    // FIELDS --------------------------------------------------------------------------------------

    private val mUriMatch = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, Table.RM.mRealesate, Table.RM.ordinal)
        addURI(AUTHORITY, Table.RM_ID.mRealesate, Table.RM_ID.ordinal)
    }

    companion object {
        private const val AUTHORITY = "abbesolo.com.realestatemanager.providers"
        val TABLE_RM = "content://$AUTHORITY/${Table.RM.mRealesate}"
    }

    private val mDatabase: Database by inject()

    // METHODS -------------------------------------------------------------------------------------

    // -- ContentProvider --

    override fun onCreate(): Boolean = true

    override fun insert(uri: Uri, values: ContentValues?) = runBlocking<Uri> {
        when (this@RealEstateContentProvider.mUriMatch.match(uri)) {
            Table.RM.ordinal -> {
                this@RealEstateContentProvider.context?.let {
                    val rm = this@RealEstateContentProvider.getRmFromContentValues(values)

                    val rmId: Long = this@RealEstateContentProvider.mDatabase
                                                                     .rmDAO()
                                                                     .insertRealEstate(rm)

                    if (rmId != 0L) {
                        it.contentResolver.notifyChange(uri, null)
                        return@runBlocking ContentUris.withAppendedId(uri, rmId)
                    }
                }
            }

            Table.RM_ID.ordinal -> { /* Do nothing */ }

            else -> { /* Ignore all other Uri */ }
        }
        throw IllegalArgumentException("Failed to insert row for uri $uri")
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        when (this.mUriMatch.match(uri)) {
            Table.RM.ordinal -> {
                this.context?.let {
                    val cursor: Cursor = this.mDatabase
                                             .rmDAO()
                                             .getAllRmWithCursor()
                    cursor.setNotificationUri(it.contentResolver, uri)
                    return cursor
                }
            }
            Table.RM_ID.ordinal -> {
                this.context?.let {
                    val rmId = ContentUris.parseId(uri)
                    val cursor: Cursor = this.mDatabase
                                             .rmDAO()
                                             .getRmByIdWithCursor(rmId)
                    cursor.setNotificationUri(it.contentResolver, uri)

                    return cursor
                }
            }
            else -> { /* Ignore all other Uri */ }
        }
        throw IllegalArgumentException("Failed to query row for uri $uri")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = runBlocking {
        when (this@RealEstateContentProvider.mUriMatch.match(uri)) {
            Table.RM.ordinal,
            Table.RM_ID.ordinal -> {
                this@RealEstateContentProvider.context?.let {
                    val rm = this@RealEstateContentProvider.getRmFromContentValues(values)

                    val count: Int = this@RealEstateContentProvider.mDatabase
                                                                   .rmDAO()
                                                                   .updateRealEstate(rm)

                    it.contentResolver.notifyChange(uri, null)

                    return@runBlocking count
                }
            }

            else -> { /* Ignore all other Uri */ }
        }

        throw IllegalArgumentException("Failed to update row for uri $uri")
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = runBlocking {
        when (this@RealEstateContentProvider.mUriMatch.match(uri)) {
            Table.RM.ordinal -> { /* Do nothing */ }

            Table.RM_ID.ordinal -> {
                this@RealEstateContentProvider.context?.let {
                    val count: Int = this@RealEstateContentProvider.mDatabase
                                                                   .rmDAO()
                                                                   .deleteUserById(ContentUris.parseId(uri))

                    it.contentResolver.notifyChange(uri, null)

                    return@runBlocking count
                }
            }

            else -> { /* Ignore all other Uri */ }
        }

        throw IllegalArgumentException("Failed to delete row for uri $uri")
    }

    override fun getType(uri: Uri): String? {
        return when (this.mUriMatch.match(uri)) {
            Table.RM.ordinal -> "vnd.android.cursor.dir/${Table.RM.mRealesate}"
            Table.RM_ID.ordinal -> "vnd.android.cursor.item/${Table.RM.mRealesate}"

            else -> null
        }
    }

    // -- RM --



    /**
     * Gets [RM] from [ContentValues]
     * @param values a [ContentValues]
     * @return a [RM]
     */
    private fun getRmFromContentValues(values: ContentValues?): RM {
        return RM().apply {
            values?.let {
                mId = it.getAsLong("id_real_estate") ?: 0L
                type = it.getAsString("type")
                price = it.getAsDouble("price_dollar")
                surface = it.getAsDouble("surface_m2")
                roomNumber = it.getAsInteger("number_of_room")
                description = it.getAsString("description maison")
                isEnable = it.getAsBoolean("is_enable")
                it.getAsString("effective_date").also { effectiveDate =  Date() }
                it.getAsString("sale_date").also { saleDate =  Date() }
                rmAgentId = it.getAsLong("estate_agent_id")

            }
        }
    }
}


