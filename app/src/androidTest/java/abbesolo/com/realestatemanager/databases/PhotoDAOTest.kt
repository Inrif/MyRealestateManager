package abbesolo.com.realestatemanager.databases

import abbesolo.com.realestatemanager.database.Database
import abbesolo.com.realestatemanager.database.dao.PhotoDAO
import abbesolo.com.realestatemanager.models.Photo
import abbesolo.com.realestatemanager.models.RM
import abbesolo.com.realestatemanager.models.RMUser
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
 An android test on [PhotoDAO].
 */
@RunWith(AndroidJUnit4::class)
class PhotoDAOTest {

    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var mDatabase: Database
    private lateinit var mPhotoDAO: PhotoDAO

    // The fields that correspond to an unique index or an unique indices couple must not be null.
    private val mPhoto1 = Photo(urlPicture = "URL1", reId = 1L)
    private val mPhoto2 = Photo(urlPicture = "URL2", reId = 1L)
    private val mPhoto3 = Photo(urlPicture  = "URL3", reId = 2L)

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

        // Add user and real estates to avoid the SQLiteConstraintException (FOREIGN KEY constraint)
        mDatabase.userDAO().insertUser(RMUser(username = "Romuald"))
        mDatabase.rmDAO().insertRealEstates(RM(type = "Flat", rmAgentId = 1L),
                                                    RM(type = "House", rmAgentId = 1L))

        mPhotoDAO = mDatabase.photoDAO()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        this.mDatabase.close()
    }

    // -- Create --

    @Test
    fun insertPhoto_shouldBeSuccess() = runBlocking {
        val id = mPhotoDAO.insertPhoto(mPhoto1)

        // TEST: Good Id
        assertEquals(1L, id)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertPhoto_shouldBeFail() = runBlocking {
        // BEFORE: Add photo
        mPhotoDAO.insertPhoto(mPhoto1)

        // THEN: Add a new photo with the same indices (Error)
        val id = mPhotoDAO.insertPhoto(mPhoto1)

        // TEST: No insert because the indices must be unique
        assertEquals(0L, id)
    }

    @Test
    fun insertPhotos_shouldBeSuccess() = runBlocking {
        val ids = mPhotoDAO.insertPhotos(mPhoto1, mPhoto2, mPhoto3)

        // TEST: Good Ids
        assertEquals(1L, ids[0])
        assertEquals(2L, ids[1])
        assertEquals(3L, ids[2])
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertPhotos_shouldBeFail() = runBlocking {
        // THEN: Add 2 photos with the same indices (Error)
        val ids = mPhotoDAO.insertPhotos(mPhoto1, mPhoto1)

        // TEST: No insert because the indices must be unique
        assertEquals(0, ids.size)
    }

    // -- Read --

    @Test
    @Throws(InterruptedException::class)
    fun getPhotoByRealEstateId_shouldBeEmpty() {
        val photos = LiveDataTestUtil.getValue(this.mPhotoDAO.getPhotoByRealEstateId(1L))

        // TEST: Empty list
        assertEquals(0, photos.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getPhotoByRealEstateId_shouldBeSuccess() = runBlocking {
        // BEFORE: Add photos
        mPhotoDAO.insertPhotos(mPhoto1, mPhoto2, mPhoto3)

        // THEN: Retrieve photos by real estate Id
        val photos = LiveDataTestUtil.getValue(mPhotoDAO.getPhotoByRealEstateId(1L))

        // TEST: Just 2 photos
        assertEquals(2, photos.size)
        assertEquals(mPhoto1.urlPicture, photos[0].urlPicture)
        assertEquals(mPhoto2.urlPicture, photos[1].urlPicture)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getAllPhotos_shouldBeEmpty() {
        val photos = LiveDataTestUtil.getValue(this.mPhotoDAO.getAllPhotos())

        // TEST: Empty list
        assertEquals(0, photos.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getAllPhotos_shouldBeSuccess() = runBlocking {
        // BEFORE: Add photos
        mPhotoDAO.insertPhotos(mPhoto1, mPhoto2, mPhoto3)

        // THEN: Retrieve photos
        val photos = LiveDataTestUtil.getValue(mPhotoDAO.getAllPhotos())

        // TEST: All photos
        assertEquals(3, photos.size)
        assertEquals(mPhoto1.urlPicture, photos[0].urlPicture)
        assertEquals(mPhoto2.urlPicture, photos[1].urlPicture)
        assertEquals(mPhoto3.urlPicture, photos[2].urlPicture)
    }

    // -- Update --

    @Test
    @Throws(InterruptedException::class)
    fun updatePhoto_shouldBeSuccess() = runBlocking {
        // BEFORE: Add photo
        mPhotoDAO.insertPhoto(mPhoto1)

        // THEN: Retrieve the photo -> photos[0]
        val photosBeforeUpdate = LiveDataTestUtil.getValue(mPhotoDAO.getAllPhotos())

        // THEN: Update the photo
        val photoUpdated = photosBeforeUpdate[0].copy(urlPicture = "Random")
        val numberOfUpdatedRow = mPhotoDAO.updatePhoto(photoUpdated)

        // AFTER: Retrieve the photo -> photos[0]
        val photosAfterUpdate = LiveDataTestUtil.getValue(mPhotoDAO.getAllPhotos())

        // TEST: Number of updated row
        assertEquals(1, numberOfUpdatedRow)

        // TEST: Same photo
        assertEquals(photoUpdated.urlPicture, photosAfterUpdate[0].urlPicture)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun updatePhoto_shouldBeFail() = runBlocking {
        // BEFORE: Add photos
        mPhotoDAO.insertPhotos(mPhoto1, mPhoto2, mPhoto3)

        // THEN: Retrieve the photo -> photos[0]
        val photosBeforeUpdate = LiveDataTestUtil.getValue(mPhotoDAO.getAllPhotos())

        // THEN: Update the photo 1 with the url picture of the photo 2 (Error)
        val photoUpdated = photosBeforeUpdate[0].copy(urlPicture = mPhoto2.urlPicture)

        val numberOfUpdatedRow = mPhotoDAO.updatePhoto(photoUpdated)

        // TEST: No update because the indices must be unique
        assertEquals(0, numberOfUpdatedRow)
    }

    // -- Delete --

    @Test
    @Throws(InterruptedException::class)
    fun deletePhoto_shouldBeSuccess() = runBlocking {
        // BEFORE: Add photo
        mPhotoDAO.insertPhoto(mPhoto1)

        // THEN: Retrieve the photo -> photos[0]
        val photos = LiveDataTestUtil.getValue(mPhotoDAO.getAllPhotos())

        // THEN: Delete photo
        val numberOfDeletedRow = mPhotoDAO.deletePhoto(photos[0])

        // TEST: Number of deleted row
        assertEquals(1, numberOfDeletedRow)
    }

    @Test
    @Throws(InterruptedException::class)
    fun deletePhoto_shouldBeFail() = runBlocking {
        // THEN: Delete photo (Error)
        val numberOfDeletedRow = mPhotoDAO.deletePhoto(mPhoto1)

        // TEST: No delete
        assertEquals(0, numberOfDeletedRow)
    }
}