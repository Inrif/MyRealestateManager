package abbesolo.com.realestatemanager.providers

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import abbesolo.com.realestatemanager.database.Database
import abbesolo.com.realestatemanager.models.RMUser
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import java.lang.IllegalArgumentException


class RealEstateContentProvider : ContentProvider() {

    // ENUMS ---------------------------------------------------------------------------------------

    enum class Table(val mName: String) {
        USER("user"),
        USER_ID("user/#"),
    }

    // FIELDS --------------------------------------------------------------------------------------

    private val mUriMatch = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, Table.USER.mName, Table.USER.ordinal)
        addURI(AUTHORITY, Table.USER_ID.mName, Table.USER_ID.ordinal)
    }

    companion object {
        private const val AUTHORITY = "abbesolo.com.realestatemanager.providers"
        val TABLE_USER = "content://$AUTHORITY/${Table.USER.mName}"
    }

    private val mDatabase: Database by inject()

    // METHODS -------------------------------------------------------------------------------------

    // -- ContentProvider --

    override fun onCreate(): Boolean = true

    override fun insert(uri: Uri, values: ContentValues?) = runBlocking<Uri> {
        when (this@RealEstateContentProvider.mUriMatch.match(uri)) {
            Table.USER.ordinal -> {
                this@RealEstateContentProvider.context?.let {
                    val user = this@RealEstateContentProvider.getUserFromContentValues(values)

                    val userId: Long = this@RealEstateContentProvider.mDatabase
                                                                     .userDAO()
                                                                     .insertUser(user)

                    if (userId != 0L) {
                        it.contentResolver.notifyChange(uri, null)
                        return@runBlocking ContentUris.withAppendedId(uri, userId)
                    }
                }
            }

            Table.USER_ID.ordinal -> { /* Do nothing */ }

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
            Table.USER.ordinal -> {
                this.context?.let {
                    val cursor: Cursor = this.mDatabase
                                             .userDAO()
                                             .getAllUsersWithCursor()

                    cursor.setNotificationUri(it.contentResolver, uri)

                    return cursor
                }
            }

            Table.USER_ID.ordinal -> {
                this.context?.let {
                    val userId = ContentUris.parseId(uri)

                    val cursor: Cursor = this.mDatabase
                                             .userDAO()
                                             .getUserByIdWithCursor(userId)

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
            Table.USER.ordinal,
            Table.USER_ID.ordinal -> {
                this@RealEstateContentProvider.context?.let {
                    val user = this@RealEstateContentProvider.getUserFromContentValues(values)

                    val count: Int = this@RealEstateContentProvider.mDatabase
                                                                   .userDAO()
                                                                   .updateUser(user)

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
            Table.USER.ordinal -> { /* Do nothing */ }

            Table.USER_ID.ordinal -> {
                this@RealEstateContentProvider.context?.let {
                    val count: Int = this@RealEstateContentProvider.mDatabase
                                                                   .userDAO()
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
            Table.USER.ordinal -> "vnd.android.cursor.dir/${Table.USER.mName}"
            Table.USER_ID.ordinal -> "vnd.android.cursor.item/${Table.USER.mName}"

            else -> null
        }
    }

    // -- User --

    /**
     * Gets [RMUser] from [ContentValues]
     * @param values a [ContentValues]
     * @return a [RMUser]
     */
    private fun getUserFromContentValues(values: ContentValues?): RMUser {
        return RMUser().apply {
            values?.let {
                id = it.getAsLong("id_user") ?: 0L
                username = it.getAsString("username")
                email = it.getAsString("email")
                urlPicture = it.getAsString("url_picture")
            }
        }
    }
}