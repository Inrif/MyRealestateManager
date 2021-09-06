package abbesolo.com.realestatemanager.databases

import abbesolo.com.realestatemanager.database.Database
import abbesolo.com.realestatemanager.database.dao.RMDAO
import abbesolo.com.realestatemanager.models.*
import abbesolo.com.realestatemanager.utils.LiveDataTestUtil
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

/**
 * Created by HOUNSA Romuald on 02/08/2021.
 * Name of the project: RealEstateManager
 * An android test on [RMDAO].
 */
@RunWith(AndroidJUnit4::class)
class RMDAOTest {

    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var mDatabase: Database
    private lateinit var mRealEstateDAO: RMDAO

    // The fields that correspond to an unique index or an unique indices couple must not be null.
    private val mAddress = Address(latitude = 0.0, longitude = 0.0)
    private val mRealEstate1 = RM(type = "Flat", price = 120_000.0, surface = 0.0, roomNumber = 2, rmAgentId = 1L, address = this.mAddress)
    private val mRealEstate2 = RM(type = "House", price = 300_000.0, surface = 100.0, roomNumber = 10, rmAgentId = 1L, address = this.mAddress)

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

        // Add user to avoid the SQLiteConstraintException (FOREIGN KEY constraint)
        mDatabase.userDAO().insertUser(RMUser(username = "Romuald"))

        mRealEstateDAO = mDatabase.rmDAO()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        this.mDatabase.close()
    }

    // -- TypeConverters (Date) --

    @Test
    fun typeConverters_date_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estate with current date
        val realEstate = RM(effectiveDate = Date(), rmAgentId = 1L)
        mRealEstateDAO.insertRealEstate(realEstate)

        // THEN: Retrieve the real estate
        val realEstateFromRoom = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstateById(1L))

        // TEST: Same date
        assertEquals(realEstate.effectiveDate, realEstateFromRoom.effectiveDate)
    }

    // -- Create --

    @Test
    fun insertRealEstate_shouldBeSuccess() = runBlocking {
        val id = mRealEstateDAO.insertRealEstate(mRealEstate1)

        // TEST: Good Id
        assertEquals(1L, id)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertRealEstate_shouldBeFail() = runBlocking {
        // BEFORE: Add real estate
        mRealEstateDAO.insertRealEstate(mRealEstate1)

        // THEN: Add a new real estate with the same indices (Error)
        val id = mRealEstateDAO.insertRealEstate(mRealEstate1)

        // TEST: No insert because the indices must be unique
        assertEquals(0L, id)
    }

    @Test
    fun insertRealEstates_shouldBeSuccess() = runBlocking {
        val ids = mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate2)

        // TEST: Good Ids
        assertEquals(1L, ids[0])
        assertEquals(2L, ids[1])
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertRealEstates_shouldBeFail() = runBlocking {
        // THEN: Add 2 real estates with the same indices (Error)
        val ids = mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate1)

        // TEST: No insert because the indices must be unique
        assertEquals(0, ids.size)
    }

    // -- Read --

    @Test
    @Throws(InterruptedException::class)
    fun getRealEstateById_shouldBeNull() {
        val realEstate = LiveDataTestUtil.getValue(this.mRealEstateDAO.getRealEstateById(1L))

        // TEST: No user
        Assert.assertNull(realEstate)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getRealEstateById_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estates
        mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate2)

        // THEN: Retrieve real estate by Id
        val realEstate = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstateById(1L))

        // TEST: Same real estate
        assertEquals(mRealEstate1.type, realEstate.type)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getAllRealEstates_shouldBeEmpty() {
        val realEstates = LiveDataTestUtil.getValue(this.mRealEstateDAO.getAllRealEstates())

        // TEST: Empty list
        assertEquals(0, realEstates.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getAllRealEstates_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estates
        mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate2)

        // THEN: Retrieve real estates
        val realEstates = LiveDataTestUtil.getValue(mRealEstateDAO.getAllRealEstates())

        // TEST: All real estates
        assertEquals(2, realEstates.size)
        assertEquals(mRealEstate1.type, realEstates[0].type)
        assertEquals(mRealEstate2.type, realEstates[1].type)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getCountOfRealEstatesByUserId_shouldBeZero() {
        // Retrieve the count of row with the same user Id
        val count = LiveDataTestUtil.getValue(mRealEstateDAO.getCountOfRealEstatesByUserId(1L))

        // TEST: Must be zero
        assertEquals(0, count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getCountOfRealEstatesByUserId_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estates
        mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate2)

        // THEN: Retrieve the count of row with the same user Id
        val count = LiveDataTestUtil.getValue(mRealEstateDAO.getCountOfRealEstatesByUserId(1L))

        // TEST: Must not be zero
        assertEquals(2, count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getIdTypeAddressPriceTupleOfRealEstate_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estates
        mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate2)

        // THEN: Retrieve tuples of real estates
        val realEstatesTuples = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstateIdTypePriceAdressByUserId(1L))

        // TEST: All real estates
        assertEquals(2, realEstatesTuples.size)
        assertEquals(mRealEstate1.type, realEstatesTuples[0].type)
        assertEquals(mRealEstate2.type, realEstatesTuples[1].type)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getRealEstatesWithPhotos_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estates
        mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate2)

        // THEN: Add photos
        mDatabase.photoDAO().insertPhoto(Photo(urlPicture = "URL1", reId = 1L))
        mDatabase.photoDAO().insertPhoto(Photo(urlPicture = "URL2", reId = 2L))
        mDatabase.photoDAO().insertPhoto(Photo(urlPicture = "URL3", reId = 2L))

        // THEN: Retrieve real estates with their photos
        val realEstatesWithPhotos = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstatesWithPhotosByUserId(1L))

        // TEST: All real estates with their photos
        assertEquals(2, realEstatesWithPhotos.size)
        assertEquals(mRealEstate1.type, realEstatesWithPhotos[0].rm?.type)
        assertEquals(mRealEstate2.type, realEstatesWithPhotos[1].rm?.type)
        assertEquals(1, realEstatesWithPhotos[0].photos?.size)
        assertEquals(2, realEstatesWithPhotos[1].photos?.size)
        assertEquals("URL1", realEstatesWithPhotos[0].photos?.get(0)?.urlPicture)
        assertEquals("URL2", realEstatesWithPhotos[1].photos?.get(0)?.urlPicture)
        assertEquals("URL3", realEstatesWithPhotos[1].photos?.get(1)?.urlPicture)
    }

    @Test
    fun getRealEstatesWithPointsOfInterest_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estates
        mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate2)

        // THEN: Add points of interest to avoid the SQLiteConstraintException (FOREIGN KEY constraint)
        mDatabase.poiDAO().insertInterestPoint(POI(name = "school"),
                                                              POI(name = "business"))

        // THEN: Add cross-reference between real estates and points of interest
        mDatabase.rmAndPoiRefCrossDAO().insertSeveralCrossRef(
            RMAndPoiRefCross(rmId = 1L, poiId = 1L),
            RMAndPoiRefCross(rmId = 1L, poiId = 2L),
            RMAndPoiRefCross(rmId = 2L, poiId = 1L)
        )

        // THEN: Retrieve real estates with their points of interest
        val realEstatesWithPointsOfInterest = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstatesWithPointsOfInterestByUserId(1L))

        // TEST: All real estates with their points of interest
        assertEquals(2, realEstatesWithPointsOfInterest.size)
        assertEquals(mRealEstate1.type, realEstatesWithPointsOfInterest[0].rm?.type)
        assertEquals(mRealEstate2.type, realEstatesWithPointsOfInterest[1].rm?.type)
        assertEquals(2, realEstatesWithPointsOfInterest[0].poi?.size)
        assertEquals(1, realEstatesWithPointsOfInterest[1].poi?.size)
        assertEquals("school", realEstatesWithPointsOfInterest[0].poi?.get(0)?.name)
        assertEquals("business", realEstatesWithPointsOfInterest[0].poi?.get(1)?.name)
        assertEquals("school", realEstatesWithPointsOfInterest[1].poi?.get(0)?.name)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getRealEstateWithPhotosById_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estate
        mRealEstateDAO.insertRealEstate(mRealEstate1)

        // THEN: Add photos
        mDatabase.photoDAO().insertPhoto(Photo(urlPicture = "URL1", reId = 1L))

        // THEN: Retrieve real estate with its photos
        val realEstateWithPhotos = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstateWithPhotosById(1L))

        // TEST: The real estate with its photos
        assertEquals(mRealEstate1.type, realEstateWithPhotos.rm?.type)
        assertEquals(1, realEstateWithPhotos.photos?.size)
        assertEquals("URL1", realEstateWithPhotos.photos?.get(0)?.urlPicture)
    }

    @Test
    fun getRealEstateWithPointsOfInterestById_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estates
        mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate2)

        // THEN: Add points of interest to avoid the SQLiteConstraintException (FOREIGN KEY constraint)
        mDatabase.poiDAO().insertInterestPoint(
            POI(name = "school"),
            POI(name = "business")
        )

        // THEN: Add cross-reference between real estates and points of interest
        mDatabase.rmAndPoiRefCrossDAO().insertSeveralCrossRef(
            RMAndPoiRefCross(rmId = 1L, poiId = 1L),
            RMAndPoiRefCross(rmId = 1L, poiId = 2L),
            RMAndPoiRefCross(rmId = 2L, poiId = 1L)
        )

        // THEN: Retrieve real estate with its points of interest
        val realEstateWithPointsOfInterest = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstateWithPointsOfInterestById(1L))

        // TEST: real estate with its points of interest
        assertEquals(mRealEstate1.type, realEstateWithPointsOfInterest.rm?.type)
        assertEquals(2, realEstateWithPointsOfInterest.poi?.size)
        assertEquals("school", realEstateWithPointsOfInterest.poi?.get(0)?.name)
        assertEquals("business", realEstateWithPointsOfInterest.poi?.get(1)?.name)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getRealEstatesWithPhotosByMultiSearch_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estates
        mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate2)

        // THEN: Add photos
        mDatabase.photoDAO().insertPhoto(Photo(urlPicture = "URL1", reId  = 1L))
        mDatabase.photoDAO().insertPhoto(Photo(urlPicture = "URL2", reId = 2L))
        mDatabase.photoDAO().insertPhoto(Photo(urlPicture = "URL3", reId = 2L))

        // THEN: Retrieve real estates with their photos
        val realEstatesWithPhotos = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstatesWithPhotosByMultiSearch(
            minPrice= 0.0,
            maxPrice= 150_000.0
        ))

        // TEST: Just mRealEstate1 with their photos
        assertEquals(1, realEstatesWithPhotos.size)
        assertEquals(mRealEstate1.type, realEstatesWithPhotos[0].rm?.type)
        assertEquals(1, realEstatesWithPhotos[0].photos?.size)
        assertEquals("URL1", realEstatesWithPhotos[0].photos?.get(0)?.urlPicture)
    }

    // -- Update --

    @Test
    @Throws(InterruptedException::class)
    fun updateRealEstate_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estates
        mRealEstateDAO.insertRealEstate(mRealEstate1)

        // THEN: Retrieve the real estate
        val realEstateBeforeUpdate = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstateById(1L))

        // THEN: Update the real estate
        val realEstateUpdated = realEstateBeforeUpdate.copy(type = "Random")
        val numberOfUpdatedRow = mRealEstateDAO.updateRealEstate(realEstateUpdated)

        // AFTER: Retrieve the real estate
        val realEstateAfterUpdate = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstateById(1L))

        // TEST: Number of updated row
        assertEquals(1, numberOfUpdatedRow)

        // TEST: Same real estate
        assertEquals(realEstateUpdated.type, realEstateAfterUpdate.type)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun updateRealEstate_shouldBeFail() = runBlocking {
        // BEFORE: Add real estates
        mRealEstateDAO.insertRealEstates(mRealEstate1, mRealEstate2)

        // THEN: Retrieve the real estate
        val realEstateBeforeUpdate = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstateById(1L))

        // THEN: Update the real estate 1 with the several fields of the real estate 2 (Error)
        val realEstateUpdated = realEstateBeforeUpdate.copy(type = mRealEstate2.type,
                                                            surface = mRealEstate2.surface,
                                                            roomNumber = mRealEstate2.roomNumber,
                                                            address = mRealEstate2.address)

        val numberOfUpdatedRow = mRealEstateDAO.updateRealEstate(realEstateUpdated)

        // TEST: No update because the indices must be unique
        assertEquals(0, numberOfUpdatedRow)
    }

    // -- Delete --

    @Test
    @Throws(InterruptedException::class)
    fun deleteRealEstate_shouldBeSuccess() = runBlocking {
        // BEFORE: Add real estate
        mRealEstateDAO.insertRealEstate(mRealEstate1)

        // THEN: Retrieve the real estate
        val realEstate = LiveDataTestUtil.getValue(mRealEstateDAO.getRealEstateById(1L))

        // THEN: Delete real estate
        val numberOfDeletedRow = mRealEstateDAO.deleteRealEstate(realEstate)

        // TEST: Number of deleted row
        assertEquals(1, numberOfDeletedRow)
    }

    @Test
    @Throws(InterruptedException::class)
    fun deleteRealEstate_shouldBeFail() = runBlocking {
        // THEN: Delete real estate (Error)
        val numberOfDeletedRow = mRealEstateDAO.deleteRealEstate(mRealEstate1)

        // TEST: No delete
        assertEquals(0, numberOfDeletedRow)
    }
}