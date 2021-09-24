package abbesolo.com.realestatemanager.providers


import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Created by HOUNSA Romuald on 02/08/2021.
 * Name of the project: RealEstateManager
 * An android test on [ContentResolver].
 */
@RunWith(AndroidJUnit4::class)
class RealEstateContentProviderTest {

    // FIELDS --------------------------------------------------------------------------------------

    private val mContentResolver = ApplicationProvider.getApplicationContext<Context>()
                                                      .contentResolver

    private val mContentValues = ContentValues().apply {
        put("id_real_estate", 0L)
        put("type", "House")
        put("price_dollar", "1000")
        put("surface_m2", "400")
        put("number_of_room", "5")
        put("description", "description")
        put("isEnable", "isEnable")
        put("effective_date", "effective_date")
        put("sale_date", "sale_date")
        put("estate_agent_id", "2")

    }

    companion object {
        const val RM_ID = 1L
    }

    // METHODS -------------------------------------------------------------------------------------

    @Test
    fun query_shouldBeSuccess() {
        // BEFORE: Retrieve the RM
        this.mContentResolver.query(
            ContentUris.withAppendedId(
                Uri.parse(RealEstateContentProvider.TABLE_RM),
                RM_ID
            ),
            null,
            null,
            null,
            null
        )?.use {
            // TEST: No rm
            assertThat(it, notNullValue())

        }
    }


    @Test
    fun insert_shouldBeSuccess(){
    val rmUri = this.mContentResolver.insert(
        Uri.parse(RealEstateContentProvider.TABLE_RM),
        this.mContentValues
    )
    assertNotNull(rmUri)
        val id = ContentUris.parseId(rmUri!!)
        assertTrue(id > 0)
    }


    @Test
    fun update_shouldBeSuccess(){

        //insert

        val rmUri = this.mContentResolver.insert(
            Uri.parse(RealEstateContentProvider.TABLE_RM),
            this.mContentValues
        )
        val id = ContentUris.parseId(rmUri!!)
        assertTrue(id > 0)
        assertNotNull(rmUri)
        //then update

        val updateContentValue = ContentValues(this.mContentValues).apply {
            put("id_real_estate", ContentUris.parseId(rmUri))
            put("type", "Flat")
        }

            this.mContentResolver.update(
                rmUri,
                updateContentValue,
                null,
                null
            ).let {

                    assertEquals(1, it)

            }


        }


    @Test
    fun delete_shouldBeSuccess(){

        //insert

        val rmUri = this.mContentResolver.insert(
            Uri.parse(RealEstateContentProvider.TABLE_RM),
            this.mContentValues
        )
        val id = ContentUris.parseId(rmUri!!)
        assertTrue(id > 0)
        assertNotNull(rmUri)

        // THEN: Delete the Rm
        this.mContentResolver.delete(
            rmUri,
            null,
            null
        ).let {
            // TEST: Only one row deleted
            assertEquals(0, it)
        }


    }



    @Test
    fun query_all_Rm_shouldBeSuccess() {
        var rows = 0

        //  Retrieve all rms
        this.mContentResolver.query(
            Uri.parse(RealEstateContentProvider.TABLE_RM),
            null,
            null,
            null,
            null
        )?.use {
            // TEST: All rms
            assertThat(it, notNullValue())
            rows = it.count
        }

    }


}