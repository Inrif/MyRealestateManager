package abbesolo.com.realestatemanager.viewModel

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.dialogs.RMSettingsDialog
import abbesolo.com.realestatemanager.liveDatas.LocationLiveData
import abbesolo.com.realestatemanager.liveDatas.POIsSearchLiveData
import abbesolo.com.realestatemanager.liveDatas.PhotoCreatorLiveData
import abbesolo.com.realestatemanager.models.*
import abbesolo.com.realestatemanager.notifications.RMNotification
import abbesolo.com.realestatemanager.repositories.*
import abbesolo.com.realestatemanager.utils.RMSaveTools
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import androidx.lifecycle.viewModelScope
import timber.log.Timber

/**
 * Created by HOUNSA Romuald on 19/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class RMViewModel(
    private val placeRepository: PlaceRepository,
    private val userRepository: RMUserRepository,
    private val rmRepository: RMRepository,
    private val photoRepository: PhotoRepository,
    private val poiRepository: PoiRepository,
    private val rmAndPoiRefCrossRepository: RMAndPoiRefCrossRespository
) : ViewModel() {

    // ATTRIBUTES 

    private var mLocation: LocationLiveData? = null

    private var mUser: LiveData<RMUser>? = null

    private var rmAndPhotosList: LiveData<List<RMAndPhotos>>? = null
    private var rmAndPhotos: LiveData<RMAndPhotos>? = null
    private var rmAndPoi: LiveData<RMAndPoi>? = null
    private var multiSearch: LiveData<List<RMAndPhotos>>? = null

    private var photos: LiveData<List<Photo>>? = null
    private var photoCreator: PhotoCreatorLiveData? = null

    private var pois: LiveData<List<POI>>? = null
    private var poisSearch: POIsSearchLiveData? = null


// FUNCTIONS


// -- Location --

    /**
     * Gets the [LiveData] of [LocationData]
     * @param context a [Context]
     * @return a [LiveData] of [LocationData]
     */
    fun getLocation(context: Context): LiveData<LocationData> {
        if (this.mLocation == null) {
            this.mLocation = LocationLiveData(context)
        }
        return this.mLocation!!
    }

    /**
     * Starts the location update from [LocationLiveData]
     */
    fun startLocationUpdate() = this.mLocation?.requestUpdateLocation()!!

// -- User --

    /**
     * Inserts the new [User] in argument
     * @param user a [User]
     */
    fun insertUser(user: RMUser) = viewModelScope.launch(Dispatchers.IO) {
        try {
            // Fetch the new rowId for the inserted item
            val userId: Long = this@RMViewModel.userRepository.insertUser(user)
            Timber.d("insertUser: Id = $userId")
        } catch (e: SQLiteConstraintException) {
            // UNIQUE constraint failed
            Timber.e("insertUser: ${e.message}")
        }
    }

    /**
     * Gets the [User] with the id in argument
     * @param userId a [Long] that contains the user Id
     * @return a [LiveData] of [User]
     */
    fun getUserById(userId: Long): LiveData<RMUser> {
        if (this.mUser == null) {
            this.mUser = this.userRepository.getUserById(userId)
        }
        return this.mUser!!
    }


    // -- RM --

    /**
     * Gets all [RealEstateWithPhotos] for an [User]
     * @param userId a [Long] that contains the user Id
     * @return a [LiveData] of [List] of [RealEstateWithPhotos]
     */
    fun getRealEstatesWithPhotosByUserId(userId: Long): LiveData<List<RMAndPhotos>> {
        if (this.rmAndPhotosList == null) {
            this.rmAndPhotosList = this.rmRepository.getRealEstatesAndPhotosByUserId(userId = userId)
        }
        return this.rmAndPhotosList!!
    }

    /**
     * Gets a [RealEstateWithPhotos] by its Id
     * @param realEstateId a [Long] that contains the real estate Id
     * @return a [LiveData] of [RealEstateWithPhotos]
     */
    fun getRealEstateWithPhotosById(realEstateId: Long): LiveData<RMAndPhotos> {
        if (this.rmAndPhotos == null) {
            this.rmAndPhotos =
                this.rmRepository.getRealEstateAndPhotosById(realEstateId)
        }
        return this.rmAndPhotos!!
    }

    /**
     * Gets a [RealEstateWithPointsOfInterest] by its Id
     * @param realEstateId a [Long] that contains the real estate Id
     * @return a [LiveData] of [RealEstateWithPointsOfInterest]
     */
    fun getRealEstateAndInterestPointById(
        realEstateId: Long
    ): LiveData<RMAndPoi> {
        if (this.rmAndPoi == null) {
            this.rmAndPoi =
                this.rmRepository.getRealEstateAndInterestPointById(realEstateId)
        }
        return this.rmAndPoi!!
    }

    /**
     * Gets all [RealEstateWithPhotos] by multi search
     * @param minPrice      a [Double] that contains the min price
     * @param maxPrice      a [Double] that contains the max price
     * @param minSurface    a [Double] that contains the min surface
     * @param maxSurface    a [Double] that contains the max surface
     * @param minNumberRoom a [Double] that contains the min room
     * @param maxNumberRoom a [Double] that contains the max room
     * @return a [LiveData] of [RealEstateWithPhotos]
     */
    fun getRealEstatesAndPhotosByMultiSearch(
        minPrice: Double = 0.0,
        maxPrice: Double = Double.MAX_VALUE,
        minSurface: Double = 0.0,
        maxSurface: Double = Double.MAX_VALUE,
        minNumberRoom: Int = 0,
        maxNumberRoom: Int = Int.MAX_VALUE
    ): LiveData<List<RMAndPhotos>> {
        if (this.multiSearch == null) {
            this.multiSearch = this.rmRepository.getRealEstatesAndPhotosByMultiSearch(
                minPrice,
                maxPrice,
                minSurface,
                maxSurface,
                minNumberRoom,
                maxNumberRoom
            )
        }
        return this.multiSearch!!
    }

    /**
     * Removes all observers of this [LiveData]
     * @param owner a [LifecycleOwner]
     */
    fun removeObserversOfMultiSearch(owner: LifecycleOwner) {
        this.multiSearch?.removeObservers(owner)
    }

    /**
     * Reset of this [LiveData]
     */
    fun resetMultiSearch() {
        this.multiSearch = null
    }

    /**
     * Gets this [LiveData] even if it is null
     */
    fun getMultiSearchEvenIfNull(): LiveData<List<RMAndPhotos>>? = this.multiSearch

    /**
     * Inserts the new [RealEstate] in argument
     * @param context           a [Context]
     * @param realEstate        a [RealEstate]
     * @param photos            a [List] of [Photo]
     * @param pointsOfInterest  a [List] of [PointOfInterest]
     */
    fun insertRealEstate(
        context: Context,
        realEstate: RM,
        photos: List<Photo>? = null,
        pointsOfInterest: List<POI>? = null
    ) = viewModelScope.launch(Dispatchers.IO) {
        val realEstateId: Long

        // INSERT: Real Estate
        try {
            // Fetch the new rowId for the inserted item
            realEstateId = this@RMViewModel.rmRepository
                .insertRealEstate(realEstate)

            // From SharedPreferences
            val isEnableNotification = RMSaveTools.fetchBooleanFromSharedPreferences(
                context,
                RMSettingsDialog.BUNDLE_SWITCH_NOTIFICATION
            )

            // NOTIFICATION
            if (isEnableNotification) {
                RMNotification.sendVisualNotification(
                    context,
                    context.getString(
                        R.string.notification_message,
                        realEstate.type, realEstate.price
                    )
                )
            }
        }
        catch (e: SQLiteConstraintException) {
            // UNIQUE constraint failed
            Timber.e("insertRealEstate: ${e.message}")
            return@launch
        }

        // INSERT: Photos
        this@RMViewModel.insertPhotosWithRealEstateId(
            photos,
            realEstateId
        )

        // INSERT: Points Of Interest
        this@RMViewModel.insertPOIsWithRealEstateId(
            pointsOfInterest,
            realEstateId
        )
    }

    /**
     * Updates a [RealEstate] in argument
     * @param realEstate        a [RealEstate]
     * @param oldPhotos         a [List] of [Photo]
     * @param newPhotos         a [List] of [Photo]
     * @param pointsOfInterest  a [List] of [PointOfInterest]
     */
    fun updateRealEstate(
        realEstate: RM,
        oldPhotos: List<Photo>? = null,
        newPhotos: List<Photo>? = null,
        pointsOfInterest: List<POI>? = null
    ) = viewModelScope.launch(Dispatchers.IO) {
        // UPDATE: Real Estate
        try {
            // Fetch the number of updated row
            val  numberOfUpdatedRow = this@RMViewModel.rmRepository.updateRealEstate(realEstate)

            // Update impossible
            if (numberOfUpdatedRow == 0) {
                Timber.e("updateRealEstate: Update impossible, Number of update row = $numberOfUpdatedRow")
                return@launch
            }
        }
        catch (e: SQLiteConstraintException) {
            // UNIQUE constraint failed
            Timber.e("updateRealEstate: ${e.message}")
            return@launch
        }


        // UPDATE: Photos
        this@RMViewModel.updatePhotosWithRealEstateId(
            oldPhotos,
            newPhotos,
            realEstate.mId
        )


        // NO UPDATE JUST INSERT: Points Of Interest

        this@RMViewModel.insertPOIsWithRealEstateId(
            pointsOfInterest,
            realEstate.mId
        )
    }

    // -- Photo --

    /**
     * Gets all [Photo]
     * @return a [LiveData] of [List] of [Photo]
     */
    fun getPhotos(): LiveData<List<Photo>> {
        if (this.photos == null) {
            this.photos = this.photoRepository.getAllPhotos()
        }
        return this.photos!!
    }

    /**
     * Gets a [PhotoCreatorLiveData]
     * @return a [PhotoCreatorLiveData]
     */
    fun getPhotoCreator(): PhotoCreatorLiveData {
        if (this.photoCreator == null) {
            this.photoCreator = PhotoCreatorLiveData()
        }
        return this.photoCreator!!
    }

    /**
     * Add a [List] of [Photo] into [PhotoCreatorLiveData]
     * @param photos a [List] of [Photo]
     */
    fun addCurrentPhotos(photos: List<Photo>) = this.photoCreator?.addCurrentPhotos(photos)

    /**
     * Add a [Photo] into [PhotoCreatorLiveData]
     * @param photo a [Photo]
     */
    fun addPhotoToPhotoCreator(photo: Photo) = this.photoCreator?.addPhoto(photo)

    /**
     * Updates a [Photo] into [PhotoCreatorLiveData]
     * @param photo a [Photo]
     */
    fun updatePhotoToPhotoCreator(photo: Photo) = this.photoCreator?.updatePhoto(photo)

    /**
     * Deletes a [Photo] into [PhotoCreatorLiveData]
     * @param photo a [Photo]
     */
    fun deletePhotoToPhotoCreator(photo: Photo) = this.photoCreator?.deletePhoto(photo)

    /**
     * Inserts several [Photo] into database
     * @param photos        a [List] of [Photo]
     * @param realEstateId  a [Long] that contains the real estate Id
     */
    private suspend fun insertPhotosWithRealEstateId(
        photos: List<Photo>?,
        realEstateId: Long
    ) = withContext(Dispatchers.IO) {
        photos?.let { photos ->
            // Change the [real_estate_id] of each photo
            photos.forEach { photo ->
                photo.reId = realEstateId
            }

            // INSERT: Photos
            val deferred: Deferred<List<Long>> = async(start = CoroutineStart.LAZY) {
                try {
                    this@RMViewModel.photoRepository.insertPhotos(*photos.toTypedArray())
                }
                catch (e: SQLiteConstraintException) {
                    // UNIQUE constraint failed
                    Timber.e("insertPhotos: ${e.message}")
                    emptyList<Long>()
                }
            }

            // Lazily started async
            deferred.start()
        }
    }

    /**
     * Updates several [Photo] into database
     * @param oldPhotos     a [List] of [Photo]
     * @param newPhotos     a [List] of [Photo]
     * @param realEstateId  a [Long] that contains the real estate Id
     */
    private suspend fun updatePhotosWithRealEstateId(
        oldPhotos: List<Photo>? = null,
        newPhotos: List<Photo>? = null,
        realEstateId: Long
    ) = withContext(Dispatchers.IO) {
        newPhotos?.let { newPhotos ->

            // DELETE: Photos
            oldPhotos?.let { oldPhotos ->
                val photosToDelete = oldPhotos.filterNot { oldPhoto ->
                    newPhotos.any { newPhoto ->
                        oldPhoto.urlPicture == newPhoto.urlPicture
                    }
                }

                if (!photosToDelete.isNullOrEmpty()) {
                    photosToDelete.forEach {
                        // DELETE: Photos
                        val deferred: Deferred<Int> = async(start = CoroutineStart.LAZY) {
                            try {
                                this@RMViewModel.photoRepository.deletePhoto(it)
                            }
                            catch (e: SQLiteConstraintException) {
                                // UNIQUE constraint failed
                                Timber.e("deletePhoto: ${e.message}")
                                0
                            }
                        }

                        // Lazily started async
                        deferred.start()
                    }
                }
            }

            // UPDATE: Photos
            val photosToUpdate = newPhotos.filter { it.id != 0L }

            if (!photosToUpdate.isNullOrEmpty()) {
                photosToUpdate.forEach {
                    // UPDATE: Photos
                    val deferred: Deferred<Int> = async(start = CoroutineStart.LAZY) {
                        try {
                            this@RMViewModel.photoRepository.updatePhoto(it)
                        }
                        catch (e: SQLiteConstraintException) {
                            // UNIQUE constraint failed
                            Timber.e("updatePhotos: ${e.message}")
                            0
                        }
                    }

                    // Lazily started async
                    deferred.start()
                }
            }

            // INSERT: Photos
            val photosToInsert = newPhotos.filter { it.id == 0L }

            if (!photosToInsert.isNullOrEmpty()) {
                this@RMViewModel.insertPhotosWithRealEstateId(
                    photosToInsert,
                    realEstateId
                )
            }
        }
    }

    // -- Points of interest --

    /**
     * Gets all [PointOfInterest]
     * @return a [LiveData] of [List] of [PointOfInterest]
     */
    fun getPOIs(): LiveData<List<POI>> {
        if (this.pois == null) {
            this.pois = this.poiRepository.getAllPointsOfInterest()
        }
        return this.pois!!
    }

    /**
     * Gets the [LiveData] of [List] of [PointOfInterest]
     * @param context   a [Context]
     * @param latitude  a [Double] that contains the latitude value
     * @param longitude a [Double] that contains the longitude value
     * @param radius    a [Double] that contains the radius value
     * @param types     a [String] that contains the types
     * @return a [LiveData] of [List] of [PointOfInterest]
     */
    fun getPOIsSearch(
        context: Context? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radius: Double? = null,
        types: String? = null
    ): LiveData<List<POI>> {
        if (this.poisSearch == null) {
            this.poisSearch = POIsSearchLiveData()
        }

        // Fetch the data
        context?.let {
            this.fetchPOIsSearch(context, latitude!!, longitude!!, radius!!, types!!)
        }

        return this.poisSearch!!
    }

    /**
     * Fetches a [List] of [PointOfInterest]
     * @param context   a [Context]
     * @param latitude  a [Double] that contains the latitude value
     * @param longitude a [Double] that contains the longitude value
     * @param radius    a [Double] that contains the radius value
     * @param types     a [String] that contains the types
     */
    fun fetchPOIsSearch(
        context: Context,
        latitude: Double,
        longitude: Double,
        radius: Double,
        types: String
    ) {
        // Single
        val single = this.placeRepository.getStreamToFetchPointsOfInterest(
            location = "$latitude,$longitude",
            radius = radius,
            types = types,
            key = context.resources.getString(R.string.google_maps_key)
        )

        // Updates LiveData
        this.poisSearch?.getPOIsSearchWithSingle(single)
    }

    /**
     * Adds all current [PointOfInterest]
     * @param poiList a [List] of [PointOfInterest]
     */
    fun addCurrentPOIs(poiList: List<POI>) = this.poisSearch?.addCurrentPOIs(poiList)

    /**
     * Checks if the [PointOfInterest] is selected
     * @param poi a [PointOfInterest]
     */
    fun checkPOI(poi: POI) = this.poisSearch?.checkPOI(poi)

    /**
     * Gets all selected [PointOfInterest]
     */
    fun getSelectedPOIs() =  this.poisSearch?.getSelectedPOIs()

    /**
     * Gets just new selected [PointOfInterest]
     */
    // todo: 17/04/2020 - Remove it when the RealEstateViewModel#updateRealEstate method will be update
    fun getJustNewSelectedPOIs() =  this.poisSearch?.getJustNewSelectedPOIs()

    /**
     * Inserts several [PointOfInterest] into database
     * @param pointsOfInterest  a [List] of [PointOfInterest]
     * @param realEstateId      a [Long] that contains the real estate Id
     */
    private suspend fun insertPOIsWithRealEstateId(
        pointsOfInterest: List<POI>?,
        realEstateId: Long
    ) = withContext(Dispatchers.IO) {

        /*
            + -> Fetch all POIs from database (Data must fetch with Fragment)
            |
            + -> Loop on each POI of List in argument (User's choice)
                 |
                 + -> INSERT POI
                      |
                      + -> Yes (new poi Id) -> INSERT Cross Ref
                      |
                      + -> No (poiId == 0L) -> Search a POI that match with the same data
                                               |
                                               + -> Yes (poi Id) -> INSERT Cross Ref
                                               |
                                               + -> No -> Do nothing
         */

        pointsOfInterest?.let {
            // FETCH: All Points Of Interest
            // [Warning] To fetch LiveData's value, the current Fragment must observe this LiveData
            val allPOIsFromDB = this@RMViewModel.pois?.value ?: emptyList()

            // Action on each POI from argument
            pointsOfInterest.forEach { poi ->
                // INSERT: Point Of Interest
                val deferred: Deferred<Long> = async {
                    try {
                        this@RMViewModel
                            .poiRepository
                            .insertPointOfInterest(poi)
                    } catch (e: SQLiteConstraintException) {
                        // UNIQUE constraint failed
                        Timber.e("insertPointOfInterest: ${e.message}")
                        0L
                    }
                }

                deferred.await().let { poiId ->
                    // Insert impossible
                    if (poiId == 0L) {
                        // Only one POI so filteredPOIs.size == 1
                        val filteredPOIs = allPOIsFromDB.filter {
                            it.name == poi.name &&
                                    it.address!!.latitude == poi.address!!.latitude &&
                                    it.address!!.longitude == poi.address!!.longitude
                        }

                        if (!filteredPOIs.isNullOrEmpty() && filteredPOIs.size == 1) {
                            // INSERT: Cross Ref
                            this@RMViewModel.insertRealEstatePointOfInterestCrossRef(
                                realEstateId,
                                filteredPOIs[0].id
                            )
                        }
                        else {
                            Timber.e("Error: Unique indices")
                        }
                    }
                    else {
                        // INSERT: Cross Ref
                        this@RMViewModel.insertRealEstatePointOfInterestCrossRef(
                            realEstateId,
                            poiId
                        )
                    }
                }
            }
        }
    }

    // -- RealEstatePointOfInterestCrossRef --

    /**
     * Inserts a [insertRealEstatePointOfInterestCrossRef] into database
     * @param realEstateId      a [Long] that contains the [RealEstate] Id
     * @param PointOfInterestId a [Long] that contains the [PointOfInterest] Id
     */
    private suspend fun insertRealEstatePointOfInterestCrossRef(
        realEstateId: Long,
        PointOfInterestId: Long
    ) = withContext(Dispatchers.IO) {
        // INSERT: Cross Ref
        val deferred: Deferred<Long> = async(start = CoroutineStart.LAZY) {
            try {
                this@RMViewModel
                    .rmAndPoiRefCrossRepository
                    .insertCrossRef(
                        RMAndPoiRefCross(
                            rmId = realEstateId,
                            poiId = PointOfInterestId
                        )
                    )
            }
            catch (e: SQLiteConstraintException) {
                // UNIQUE constraint failed
                Timber.e("insertCrossRef: ${e.message}")
                0L
            }
        }
        // Lazily started async
        deferred.start()
    }

}