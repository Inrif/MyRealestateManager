package abbesolo.com.realestatemanager.databases

import abbesolo.com.realestatemanager.database.Database
import abbesolo.com.realestatemanager.database.dao.RMUserDAO
import abbesolo.com.realestatemanager.models.RMUser
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import abbesolo.com.realestatemanager.utils.LiveDataTestUtil
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Created by HOUNSA Romuald on 02/08/2021.
 * Name of the project: RealEstateManager
 * An android test on [RMUserDAO].
 */
@RunWith(AndroidJUnit4::class)
class UserDAOTest {

    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var mDatabase: Database
    private lateinit var mUserDAO: RMUserDAO

    // The fields that correspond to an unique index or an unique indices couple must not be null.
    private val mUser1 = RMUser(username = "Romuald", email = "rom@com")
    private val mUser2 = RMUser(username = "Josiane", email = "josiane@com")

    // RULES ---------------------------------------------------------------------------------------

    // A JUnit rule that configures LiveData to execute each task synchronously
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

        this.mUserDAO = this.mDatabase.userDAO()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        this.mDatabase.close()
    }

    // -- Create --

    @Test
    fun insertUser_shouldBeSuccess() = runBlocking {
        // BEFORE: Add user
        val id = mUserDAO.insertUser(mUser1)

        // TEST: Good Id
        assertEquals(1L, id)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertUser_shouldBeFail() = runBlocking {
        // BEFORE: Add user
        mUserDAO.insertUser(mUser1)

        // THEN: Add a new user with the same indices (Error)
        val id = mUserDAO.insertUser(mUser1)

        // TEST: No insert because the indices must be unique
        assertEquals(0L, id)
    }

    @Test
    fun insertUsers_shouldBeSuccess() = runBlocking {
        // Add 2 users
        val ids = mUserDAO.insertUsers(mUser1, mUser2)

        // TEST: Good Ids
        assertEquals(1L, ids[0])
        assertEquals(2L, ids[1])
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertUsers_shouldBeFail() = runBlocking {
        // Add 2 users with the same indices (Error)
        val ids = mUserDAO.insertUsers(mUser1, mUser1)

        // TEST: No insert because the indices must be unique
        assertEquals(0, ids.size)
    }

    // -- Read --

    @Test
    @Throws(InterruptedException::class)
    fun getUserById_shouldBeNull() {
        val user = LiveDataTestUtil.getValue(this.mUserDAO.getUserById(1L))

        // TEST: No user
        assertNull(user)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getUserById_shouldBeSuccess() = runBlocking {
        // BEFORE: Add user
        mUserDAO.insertUser(mUser1)

        // THEN: Retrieve user by its Id
        val user = LiveDataTestUtil.getValue(mUserDAO.getUserById(1L))

        // TEST: Same user except the id because it is 0 for user1 and 1 for user2
        assertEquals(mUser1.username, user.username)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getAllUsers_shouldBeEmpty() {
        val users = LiveDataTestUtil.getValue(this.mUserDAO.getAllUsers())

        // TEST: Empty list
        assertEquals(0, users.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getAllUsers_shouldBeSuccess() = runBlocking {
        // BEFORE: Add users
        mUserDAO.insertUsers(mUser1, mUser2)

        // THEN: Retrieve users
        val users = LiveDataTestUtil.getValue(mUserDAO.getAllUsers())

        // TEST: Same users
        assertEquals(mUser1.username, users[0].username)
        assertEquals(mUser2.username, users[1].username)
    }

    @Test
    @Throws(InterruptedException::class)
    fun getUserByIdWithCursor_shouldBeEmpty() {
        this.mUserDAO.getUserByIdWithCursor(1L).use {
            // TEST: No user
            assertThat(it, notNullValue())
            assertEquals(0, it.count)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun getUserByIdWithCursor_shouldBeSuccess() = runBlocking {
        // BEFORE: Add user
        val id = mUserDAO.insertUser(mUser1)

        // THEN: Retrieve the cursor
        mUserDAO.getUserByIdWithCursor(id).use {
            // TEST: nly one user
            assertThat(it, notNullValue())
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertEquals("Romuald", it.getString(it.getColumnIndexOrThrow("username")))
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun getAllUsersWithCursor_shouldBeEmpty() {
        this.mUserDAO.getAllUsersWithCursor().use {
            // TEST: No user
            assertThat(it, notNullValue())
            assertEquals(it.count, 0)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun getAllUsersWithCursor_shouldBeSuccess() = runBlocking {
        // BEFORE: Add user
        mUserDAO.insertUsers(mUser1, mUser2)

        // THEN: Retrieve the cursor
        mUserDAO.getAllUsersWithCursor().use {
            // TEST: 2 users
            assertThat(it, notNullValue())
            assertEquals(2, it.count)
            assertTrue(it.moveToFirst())
            assertEquals("Romuald", it.getString(it.getColumnIndexOrThrow("username")))
            assertTrue(it.moveToNext())
            assertEquals("Josiane", it.getString(it.getColumnIndexOrThrow("username")))
        }
    }

    // -- Update --

    @Test
    @Throws(InterruptedException::class)
    fun updateUser_shouldBeSuccess() = runBlocking {
        // BEFORE: Add user
        mUserDAO.insertUser(mUser1)

        // THEN: Retrieve the user
        val userBeforeUpdate = LiveDataTestUtil.getValue(mUserDAO.getUserById(1L))

        // THEN: Update the user
        val userUpdated = userBeforeUpdate.copy(username = "Josiane")
        val numberOfUpdatedRow = mUserDAO.updateUser(userUpdated)

        // AFTER: Retrieve the user
        val userAfterUpdate = LiveDataTestUtil.getValue(mUserDAO.getUserById(userBeforeUpdate.id))

        // TEST: Number of updated row
        assertEquals(1, numberOfUpdatedRow)

        // TEST: Same user
        assertEquals(userUpdated.username, userAfterUpdate.username)
    }

    @Test(expected = SQLiteConstraintException::class)
    @Throws(InterruptedException::class)
    fun updateUser_shouldBeFail() = runBlocking {
        // BEFORE: Add users
        mUserDAO.insertUsers(mUser1, mUser2)

        // THEN: Retrieve the user
        val userBeforeUpdate = LiveDataTestUtil.getValue(mUserDAO.getUserById(1L))

        // THEN: Update the user 1 with the username and the email of the user 2 (Error)
        val userUpdated = userBeforeUpdate.copy(username = mUser2.username,
                                                email = mUser2.email)

        val numberOfUpdatedRow = mUserDAO.updateUser(userUpdated)

        // TEST: No update because the indices must be unique
        assertEquals(0, numberOfUpdatedRow)
    }

    // -- Delete --

    @Test
    @Throws(InterruptedException::class)
    fun deleteUser_shouldBeSuccess() = runBlocking {
        // BEFORE: Add user
        mUserDAO.insertUser(mUser1)

        // THEN: Retrieve the user
        val user = LiveDataTestUtil.getValue(mUserDAO.getUserById(1L))

        // THEN: Delete user
        val numberOfDeletedRow = mUserDAO.deleteUser(user)

        // TEST: Number of deleted row
        assertEquals(1, numberOfDeletedRow)
    }

    @Test
    @Throws(InterruptedException::class)
    fun deleteUser_shouldBeFail() = runBlocking {
        // THEN: Delete user (Error)
        val numberOfDeletedRow = mUserDAO.deleteUser(mUser1)

        // TEST: No delete
        assertEquals(0, numberOfDeletedRow)
    }

    @Test
    @Throws(InterruptedException::class)
    fun deleteUserById_shouldBeSuccess() = runBlocking {
        // BEFORE: Add user
        mUserDAO.insertUser(mUser1)

        // THEN: Retrieve user by its Id
        val user = LiveDataTestUtil.getValue(mUserDAO.getUserById(1L))

        // THEN: Delete user thanks to its Id
        val numberOfDeletedRow = mUserDAO.deleteUserById(user.id)

        // TEST: Number of deleted row
        assertEquals(1, numberOfDeletedRow)
    }

    @Test
    @Throws(InterruptedException::class)
    fun deleteUserById_shouldBeFail() = runBlocking {
        // THEN: Delete user thanks to its Id (Error)
        val numberOfDeletedRow = mUserDAO.deleteUserById(0L)

        // TEST: No delete
        assertEquals(0, numberOfDeletedRow)
    }
}