package abbesolo.com.realestatemanager.databases

import abbesolo.com.realestatemanager.database.Database
import abbesolo.com.realestatemanager.database.dao.RMAndPoiRefCrossDAO
import abbesolo.com.realestatemanager.models.POI
import abbesolo.com.realestatemanager.models.RM
import abbesolo.com.realestatemanager.models.RMAndPoiRefCross
import abbesolo.com.realestatemanager.models.RMUser
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
 * An android test on [RMUserDAO].
 */
@RunWith(AndroidJUnit4::class)
class RMAndPoiRefCrossDAOTest {

    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var mDatabase: Database
    private lateinit var mRealEstatePointOfInterestCrossRefDAO: RMAndPoiRefCrossDAO

    private val mCrossRef1 = RMAndPoiRefCross(rmId = 1L, poiId = 1L)
    private val mCrossRef2 = RMAndPoiRefCross(rmId = 1L, poiId = 2L)

    // RULES (Synchronized Tests) ------------------------------------------------------------------

    @get:Rule
    val rule = InstantTaskExecutorRule()

    // METHODS -------------------------------------------------------------------------------------

    @Before
    @Throws(Exception::class)
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        mDatabase = Room.inMemoryDatabaseBuilder(context,
                                                 Database::class.java)
                        .allowMainThreadQueries()
                        .build()

        // Add user, real estates and points of interest
        // to avoid the SQLiteConstraintException (FOREIGN KEY constraint)
        mDatabase.userDAO().insertUser(RMUser(username = "Romuald"))
        mDatabase.rmDAO().insertRealEstates(RM(type = "Flat", rmAgentId = 1L),
                                                    RM(type = "House", rmAgentId = 1L))
        mDatabase.poiDAO().insertInterestPoint(POI(name = "school"),
                                                              POI(name = "business"))

        mRealEstatePointOfInterestCrossRefDAO = mDatabase.rmAndPoiRefCrossDAO()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        this.mDatabase.close()
    }

    // -- Create --

    @Test
    fun insertCrossRef_shouldBeSuccess() = runBlocking {
        val id = mRealEstatePointOfInterestCrossRefDAO.insertCrossRef(mCrossRef1)

        // TEST: Good Id
        assertEquals(1L, id)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertCrossRef_shouldBeFail() = runBlocking {
        // BEFORE: Add cross-ref
        mRealEstatePointOfInterestCrossRefDAO.insertCrossRef(mCrossRef1)

        // THEN: Add a new cross-ref with the same primary keys (Error)
        val id = mRealEstatePointOfInterestCrossRefDAO.insertCrossRef(mCrossRef1)

        // TEST: No insert because the primary keys must be unique
        assertEquals(0L, id)
    }

    @Test
    fun insertSeveralCrossRef_shouldBeSuccess() = runBlocking {
        val ids = mRealEstatePointOfInterestCrossRefDAO.insertSeveralCrossRef(mCrossRef1,
                                                                              mCrossRef2)

        // TEST: Good Ids
        assertEquals(1L, ids[0])
        assertEquals(2L, ids[1])
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertSeveralCrossRef_shouldBeFail() = runBlocking {
        // THEN: Add 2 cross-ref with the same primary keys (Error)
        val ids = mRealEstatePointOfInterestCrossRefDAO.insertSeveralCrossRef(mCrossRef1,
                                                                              mCrossRef1)

        // TEST: No insert because the primary keys must be unique
        assertEquals(0, ids.size)
    }
}