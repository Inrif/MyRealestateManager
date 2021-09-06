package abbesolo.com.realestatemanager.databases

import abbesolo.com.realestatemanager.database.Database
import abbesolo.com.realestatemanager.database.dao.PoiDAO
import abbesolo.com.realestatemanager.models.Address
import abbesolo.com.realestatemanager.models.POI
import abbesolo.com.realestatemanager.utils.LiveDataTestUtil
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Created by HOUNSA Romuald on 02/08/2021.
 * Name of the project: RealEstateManager
 * An android test on [PoiDAO].
 */
@RunWith(AndroidJUnit4::class)
class PoiDAOTest {

    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var mDatabase: Database
    private lateinit var poiDAO: PoiDAO

    // The fields that correspond to an unique index or an unique indices couple must not be null.
    private val mAddress = Address(latitude = 0.0, longitude = 0.0)
    private val poi1 = POI(name = "school", address = this.mAddress)
    private val poi2 = POI(name = "business", address = this.mAddress)

    // RULES (Synchronized Tests) ------------------------------------------------------------------

    @get:Rule
    val rule = InstantTaskExecutorRule()

    // METHODS -------------------------------------------------------------------------------------

    @Before
    @Throws(Exception::class)
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        this.mDatabase = Room.inMemoryDatabaseBuilder(context,
                                                      Database::class.java)
                             .allowMainThreadQueries()
                             .build()

        this.mDatabase.poiDAO().also { this.poiDAO = it }
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        this.mDatabase.close()
    }

    // -- Create --

    @Test
    fun insertPointOfInterest_shouldBeSuccess() = runBlocking {
        val id = poiDAO.insertInterestPoint(poi1)

        // TEST: Good Id
        assertEquals(1L, id)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertPointOfInterest_shouldBeFail() = runBlocking {
        // BEFORE: Add point of interest
        poiDAO.insertInterestPoint(poi1)

        // THEN: Add a new point of interest with the same indices (Error)
        val id = poiDAO.insertInterestPoint(poi1)

        // TEST: No insert because the indices must be unique
        assertEquals(0L, id)
    }

    @Test
    fun insertPointsOfInterest_shouldBeSuccess() = runBlocking {
        val ids = poiDAO.insertInterestPoint(poi1, poi2)

        // TEST: Good Ids
        assertEquals(1L, ids[0])
        assertEquals(2L, ids[1])
    }

//    @Test(expected = SQLiteConstraintException::class)
//    fun insertPointsOfInterest_shouldBeFail() = runBlocking {
//        // THEN: Add 2 points of interest with the same indices (Error)
//        val ids = poiDAO.insertInterestPoint(poi1, poi2)
//
//        // TEST: No insert because the indices must be unique
//        assertEquals(0, ids.size)
//    }

    // -- Read --

    @Test
    @Throws(InterruptedException::class)
    fun getAllPointsOfInterest_shouldBeEmpty() {
        val pointsOfInterest = LiveDataTestUtil.getValue(this.poiDAO.getAllInterestPoint())

        // TEST: Empty list
        assertEquals(0, pointsOfInterest.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getAllPointsOfInterest_shouldBeSuccess() = runBlocking {
        // BEFORE: Add points of interest
        poiDAO.insertInterestPoint(poi1, poi2)

        // THEN: Retrieve points of interest
        val pointOfInterests = LiveDataTestUtil.getValue(poiDAO.getAllInterestPoint())

        // TEST: Same points of interest
        assertEquals(poi1.name, pointOfInterests[0].name)
        assertEquals(poi2.name, pointOfInterests[1].name)
    }
}