package abbesolo.com.realestatemanager.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.dialogs.DialogListener
import abbesolo.com.realestatemanager.dialogs.RMPhotoDialog
import abbesolo.com.realestatemanager.fragments.adapters.RMAdapterListener
import abbesolo.com.realestatemanager.fragments.adapters.RMPhotoAdapter
import abbesolo.com.realestatemanager.fragments.adapters.RMPoiAdapter
import abbesolo.com.realestatemanager.models.*
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_edit.view.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class RMUpdateFragment : RMBaseFragment(), RMAdapterListener, DialogListener, OnMapReadyCallback {

    // FIELDS --------------------------------------------------------------------------------------

    private val mItemId: Long by lazy {
        RMUpdateFragmentArgs.fromBundle(this.requireArguments()).itemId
    }

    private lateinit var mPhotoAdapter: RMPhotoAdapter
    private lateinit var mPOIsAdapter: RMPoiAdapter
    private var mPhotosOfCurrentRealEstate: List<Photo>? = null
    private var mAllPhotosFromDatabase: List<Photo>? = null
    private var mAllPhotosFromCreator: List<Photo>? = null

    private var mGoogleMap: GoogleMap? = null

    companion object {
        const val REQUEST_CODE_PHOTO = 100
        const val REQUEST_CODE_AUTOCOMPLETE = 200
    }

    // METHODS -------------------------------------------------------------------------------------

    // -- BaseFragment --

    @LayoutRes
    override fun getFragmentLayout(): Int = R.layout.fragment_edit

    override fun configureDesign(savedInstanceState: Bundle?) {
        // Argument
        this.eventWhenArgumentEqualsDefaultValue()

        // UI
        this.configureFieldsOfData()
        this.configureListenerOfEachButton()
        this.configurePhotoRecyclerView()
        this.configurePOIsRecyclerView()
        this.configureSupportMapFragment()

        // LiveData
        this.configureRealEstateLiveData()
        this.configurePOIsLiveData()
        this.configurePhotosFomDatabase()
        this.configurePhotoCreatorLiveData()
        this.configurePOIsSearch()
    }

    override fun actionAfterPermission(media: Media?) {
        when (media) {
            Media.PHOTO -> this.actionToAddPhoto()
            Media.VIDEO -> { /* Do nothing */ }
            null -> { /* Do nothing */ }
        }
    }

    // -- Fragment --

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            // Photo
            REQUEST_CODE_PHOTO -> this.handlePhoto(resultCode, data)

            // Search
            REQUEST_CODE_AUTOCOMPLETE -> this.handleAddress(resultCode, data)

            else -> { /* Ignore all other requests */ }
        }
    }

    // -- AdapterListener interface --

    override fun onDataChanged() {
        // Photos
        this.mRootView.fragment_edit_RecyclerView.visibility =
            if (this.mPhotoAdapter.itemCount != 0)
                View.VISIBLE
            else
                View.GONE

        // POIs
        this.mRootView.fragment_edit_RecyclerView_poi.visibility =
            if (this.mPOIsAdapter.itemCount != 0)
                View.VISIBLE
            else
                View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // Button: DELETE
            R.id.item_photo_delete_media -> this.mViewModel.deletePhotoToPhotoCreator(v.tag as Photo)

            // Button: EDIT
            R.id.item_photo_edit_media -> {
                RMPhotoDialog.newInstance(
                    callback = this@RMUpdateFragment,
                    photoId = (v.tag as Photo).id,
                    urlPhoto = (v.tag as Photo).urlPicture,
                    description = (v.tag as Photo).description,
                    realEstateId = (v.tag as Photo).id!!,
                    mode = RMPhotoDialog.PhotoDialogMode.UPDATE
                )
                    .show(
                        this.requireActivity().supportFragmentManager,
                        "DIALOG PHOTO"
                    )
            }

            // CheckBox
            R.id.item_poi_is_selected -> this.mViewModel.checkPOI(v.tag as POI)

            else -> { /* Ignore all other ids */ }
        }
    }

    // -- DialogListener interface --

    override fun getSelectedPhotoFromDialog(
        photo: Photo,
        mode: RMPhotoDialog.PhotoDialogMode
    ) {
        when (mode) {
            // ADD
            RMPhotoDialog.PhotoDialogMode.ADD -> this.mViewModel.addPhotoToPhotoCreator(photo)

            // UPDATE
            RMPhotoDialog.PhotoDialogMode.UPDATE -> this.mViewModel.updatePhotoToPhotoCreator(photo)
        }
    }

    // -- OnMapReadyCallback interface --

    override fun onMapReady(googleMap: GoogleMap?) {
        this.mGoogleMap = googleMap

        // TOOLBAR
        this.mGoogleMap?.uiSettings?.isMapToolbarEnabled = false
    }

    // -- Argument --

    /**
     * Event when argument equals default value (0L)
     */
    private fun eventWhenArgumentEqualsDefaultValue() {
        if (this.mItemId == 0L) {
            this.mCallback?.showMessage(this.getString(R.string.edit_impossible))

            // Finish this fragment
            this.findNavController().popBackStack()
        }
    }

    // -- Fields of data --

    /**
     * Configures the fields of data
     */
    private fun configureFieldsOfData() {
        this.configureListenerOfFields(
            this.mRootView.fragment_edit_type,
            this.mRootView.fragment_edit_price,
            this.mRootView.fragment_edit_surface,
            this.mRootView.fragment_edit_number_of_room,
            this.mRootView.fragment_edit_description,
            this.mRootView.fragment_edit_effective_date
        )

        // Type: Populates the adapter
        (this.mRootView.fragment_edit_type.editText as? AutoCompleteTextView)?.setAdapter(
            ArrayAdapter(
                this.requireContext(),
                R.layout.item_type,
                this.resources.getStringArray(R.array.creator_types)
            )
        )
    }

    /**
     * Configures the listener of each field
     * @param textInputLayouts a variable array of [TextInputLayout]
     */
    private fun configureListenerOfFields(vararg textInputLayouts: TextInputLayout) {
        for (textInputLayout in textInputLayouts) {
            // Add listener
            textInputLayout.editText?.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Do nothing
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Reset error
                    textInputLayout.error = null
                }

                override fun afterTextChanged(s: Editable?) {
                    // Do nothing
                }
            })
        }
    }

    /**
     * Configures the error of each field
     * @param textInputLayouts a variable array of [TextInputLayout]
     * @return a [Boolean] with true if is canceled
     */
    private fun configureErrorOfFields(vararg textInputLayouts: TextInputLayout): Boolean {
        var isIncorrect = false

        for (textInputLayout in textInputLayouts) {
            // No Data
            if (textInputLayout.editText?.text.toString().isEmpty()) {
                textInputLayout.error = this.getString(R.string.no_data)
                isIncorrect = true
            }
        }

        return isIncorrect
    }

    /**
     * Shows the address into [TextInputLayout]
     * @param place a [Place] that contains the address
     */
    private fun showAddressIntoField(place: Place) {
        // Retrieve address
        var streetNumber: String? = null
        var route: String? = null
        var locality: String? = null
        var postalCode: String? = null
        var country: String? = null

        place.addressComponents?.asList()?.forEach {
            when (it.types[0]) {
                "street_number"-> streetNumber = it.name
                "route"-> route = it.name
                "locality"-> locality = it.name
                "postal_code"-> postalCode = it.name
                "country"-> country = it.name

                else -> { /* Ignore all other types */ }
            }
        }

        // Street
        this.mRootView.fragment_edit_address.editText?.text?.let {
            it.clear()

            val fullStreet = StringBuilder().run {
                append(streetNumber ?: this@RMUpdateFragment.getString(R.string.details_no_street_number))
                append(" ")
                append(route ?: this@RMUpdateFragment.getString(R.string.details_no_street))
                toString()
            }

            it.append(fullStreet)
        }

        // City
        this.mRootView.fragment_edit_city.editText?.text?.let {
            it.clear()
            it.append(locality ?: this.getString(R.string.details_no_city))
        }

        // Post code
        this.mRootView.fragment_edit_post_code.editText?.text?.let {
            it.clear()
            it.append(
                if (postalCode.isNullOrEmpty())
                    "0"
                else
                    postalCode
            )
        }

        // Country
        this.mRootView.fragment_edit_country.editText?.text?.let {
            it.clear()
            it.append(country ?: this.getString(R.string.details_no_country))
        }
    }

    // -- Listeners --

    /**
     * Configures the listener of Each button
     */
    private fun configureListenerOfEachButton() {
        // Button: Add address
        this.mRootView.fragment_edit_add_address.setOnClickListener {
            this.actionToSearchAddress()
        }

        // Button: Add photo
        this.mRootView.fragment_edit_add_photo.setOnClickListener {
            this.actionToAddPhoto()
        }

        // Button: Add points of interest
        this.mRootView.fragment_edit_search_poi.setOnClickListener {
            this.actionToAddPOIs()
        }

        // FAB
        this.mRootView.fragment_edit_fab.setOnClickListener {
            this.actionToUpdateRealEstate()
        }
    }

    // -- RecyclerView --

    /**
     * Configures the photo RecyclerView
     */
    private fun configurePhotoRecyclerView() {
        // Adapter
        this.mPhotoAdapter = RMPhotoAdapter(
            mCallback = this@RMUpdateFragment,
            mButtonDisplayMode = RMPhotoAdapter.ButtonDisplayMode.EDIT_MODE
        )

        // LayoutManager
        val viewManager = LinearLayoutManager(
            this.requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // Divider
        val divider = DividerItemDecoration(
            this.requireContext(),
            DividerItemDecoration.HORIZONTAL
        )

        // RecyclerView
        with(this.mRootView.fragment_edit_RecyclerView) {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(divider)
            adapter = this@RMUpdateFragment.mPhotoAdapter
            visibility = View.GONE
        }
    }

    /**
     * Configures the POIs RecyclerView
     */
    private fun configurePOIsRecyclerView() {
        // Adapter
        this.mPOIsAdapter = RMPoiAdapter(
            mCallback = this@RMUpdateFragment,
            mMode = RMPoiAdapter.CheckBoxDisplayMode.SELECT_MODE
        )

        // LayoutManager
        val viewManager = LinearLayoutManager(
            this.requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // Divider
        val divider = DividerItemDecoration(
            this.requireContext(),
            DividerItemDecoration.HORIZONTAL
        )

        // RecyclerView
        with(this.mRootView.fragment_edit_RecyclerView_poi) {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(divider)
            adapter = this@RMUpdateFragment.mPOIsAdapter
            visibility = View.GONE
        }
    }

    // -- Child Fragment --

    /**
     * Configures the child fragment which contains the Google Maps
     */
    private fun configureSupportMapFragment() {
        var childFragment = this.childFragmentManager
            .findFragmentById(R.id.fragment_edit_map_lite_mode) as? SupportMapFragment

        if (childFragment == null) {
            childFragment = SupportMapFragment.newInstance()

            this.childFragmentManager.beginTransaction()
                .add(R.id.fragment_edit_map_lite_mode, childFragment)
                .commit()
        }

        childFragment?.getMapAsync(this@RMUpdateFragment)

        // To keep the instance after configuration change (rotation)
        childFragment?.retainInstance = true
    }

    // -- LiveData --

    /**
     * Configures the LiveData thanks to a simple format
     */
    private fun configureRealEstateLiveData() {
        this.mViewModel
            .getRealEstateWithPhotosById(realEstateId = this.mItemId)
            .observe(
                this.viewLifecycleOwner,
                androidx.lifecycle.Observer {
                    this.mPhotosOfCurrentRealEstate = it.photos

                    it.photos?.let { currentPhotos ->
                        this.mViewModel.addCurrentPhotos(currentPhotos)
                    }

                    this.configureUI(it) }
            )
    }

    /**
     * Configures the POIs with cross reference table
     */
    private fun configurePOIsLiveData() {
        this.mViewModel
            .getRealEstateAndInterestPointById(realEstateId = this.mItemId)
            .observe(
                this.viewLifecycleOwner,
                androidx.lifecycle.Observer {
                    it.poi?.let { poiList ->
                        this.mViewModel.addCurrentPOIs(poiList)
                    }
                }
            )
    }

    /**
     * Configures all [Photo] from database
     */
    private fun configurePhotosFomDatabase() {
        this.mViewModel
            .getPhotos()
            .observe(
                this.viewLifecycleOwner,
                androidx.lifecycle.Observer { this.mAllPhotosFromDatabase = it }
            )
    }

    /**
     * Configures the [PhotoCreatorLiveData]
     */
    private fun configurePhotoCreatorLiveData() {
        this.mViewModel
            .getPhotoCreator()
            .observe(
                this.viewLifecycleOwner,
                androidx.lifecycle.Observer {
                    this.mAllPhotosFromCreator = it
                    this.mPhotoAdapter.updateData(it)
                }
            )
    }

    /**
     * Configures the POIs search
     */
    private fun configurePOIsSearch() {
        this.mViewModel
            .getPOIsSearch()
            .observe(
                this.viewLifecycleOwner,
                androidx.lifecycle.Observer {
                    // Sorts the list on its name from A to Z
                    Collections.sort(it, POI.AZTitleComparator())

                    this.mPOIsAdapter.updateData(it)
                }
            )

        // Useful just for Coroutine calls
        this.mViewModel
            .getPOIs()
            .observe(
                this.viewLifecycleOwner,
               androidx.lifecycle.Observer { /* Do nothing */ }
            )
    }

    // -- UI --

    /**
     * Configures UI
     * @param realEstateWithPhotos a [RealEstateWithPhotos]
     */
    private fun configureUI(realEstateWithPhotos: RMAndPhotos?) {
        realEstateWithPhotos?.let {
            // Real estate
            it.rm?.let { realEstate ->
                // Type
                this.mRootView.fragment_edit_type.editText?.text?.let { type ->
                    type.clear()
                    type.append(realEstate.type ?: this.getString(R.string.edit_no_type))
                }

                // Price
                this.mRootView.fragment_edit_price.editText?.text?.let { price ->
                    price.clear()
                    price.append(
                        realEstate.price?.toString() ?:
                        this.getString(R.string.edit_no_price)
                    )
                }

                // Surface
                this.mRootView.fragment_edit_surface.editText?.text?.let { surface ->
                    surface.clear()
                    surface.append(realEstate.surface?.toString() ?: "0.0")
                }

                // Number of room
                this.mRootView.fragment_edit_number_of_room.editText?.text?.let { nbRoom ->
                    nbRoom.clear()
                    nbRoom.append(realEstate.roomNumber?.toString() ?: "0")
                }

                // Description
                this.mRootView.fragment_edit_description.editText?.text?.let { description ->
                    description.clear()
                    description.append(
                        realEstate.description ?:
                        this.getString(R.string.details_no_description)
                    )
                }

                // Address
                realEstate.address?.let { address ->
                    // Street
                    this.mRootView.fragment_edit_address.editText?.text?.let { street ->
                        street.clear()
                        street.append(
                            address.street ?:
                            this.getString(R.string.details_no_street)
                        )
                    }

                    // City
                    this.mRootView.fragment_edit_city.editText?.text?.let { city ->
                        city.clear()
                        city.append(address.city ?: this.getString(R.string.details_no_city))
                    }

                    // Post code
                    this.mRootView.fragment_edit_post_code.editText?.text?.let { postCode ->
                        postCode.clear()
                        postCode.append(
                            address.postCode?.toString() ?: "0"
                        )
                    }

                    // Country
                    this.mRootView.fragment_edit_country.editText?.text?.let { country ->
                        country.clear()
                        country.append(
                            address.country ?:
                            this.getString(R.string.details_no_country)
                        )
                    }

                    // Google Maps
                    this.showPointOfInterest(
                        LatLng(
                            address.latitude ?: 0.0,
                            address.longitude ?: 0.0
                        )
                    )
                }

                // Enable
                this.mRootView.fragment_edit_enable.isEnabled = realEstate.isEnable ?: false

                // Effective date
                this.mRootView.fragment_edit_effective_date.editText?.text?.let { effectiveDate ->
                    effectiveDate.clear()

                    val date = realEstate.effectiveDate?.run {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        dateFormat.format(this)
                    } ?: "00/00/0000"

                    effectiveDate.append(date)
                }
            }
        }
    }

    // -- Google Maps --

    /**
     * Shows the point of interest into Google Maps
     * @param latLng a [LatLng] that contains the location
     */
    private fun showPointOfInterest(latLng: LatLng) {
        /* After configuration change (rotation), this.mGoogleMap is null
           because onMapReady method is called after configureUI method
           To keep the marker, add this line into configureSupportMapFragment method
                childFragment?.retainInstance = true
        */
        this.mGoogleMap?.let {
            it.clear()
            it.addMarker(
                MarkerOptions().position(latLng)
                    .title(this.getString(R.string.title_marker))
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_BLUE
                        )
                    )
            )

            it.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    /**
     * Action to search the address
     */
    private fun actionToSearchAddress() {
        // Configures Places with the Google Maps key
        Places.initialize(this.requireContext(),
            this.getString(R.string.google_maps_key))

        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY,
            listOf(
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG))
            .setTypeFilter(TypeFilter.ADDRESS)
            .build(this.requireContext())

        this.startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)
    }

    /**
     * Handles the address
     * @param resultCode    an [Int] that contains the result code
     * @param data          an [Intent] that contains the data
     */
    private fun handleAddress(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                // Data
                val place = Autocomplete.getPlaceFromIntent(data!!)

                this.showPointOfInterest(place.latLng!!)
                this.showAddressIntoField(place)
            }

            AutocompleteActivity.RESULT_ERROR -> {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Timber.e("${status.statusMessage} [Place API]")
            }

            else -> {
                this.mCallback?.showMessage(
                    this.getString(R.string.creator_search_cancel)
                )
            }
        }
    }

    // -- Photo --

    /**
     * Action to add a [Photo]
     */
    private fun actionToAddPhoto() {
        if (this.checkReadExternalStoragePermission(Media.PHOTO)) {
            // Goal: Retrieves a photo from external storage
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            this.startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }
    }

    /**
     * Handles the photo
     * @param resultCode    an [Int] that contains the result code
     * @param data          an [Intent] that contains the data
     */
    private fun handlePhoto(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Database: Search if is already present into the database
                val isAlreadyPresentIntoDatabase = this.mAllPhotosFromDatabase?.any {
                    it.urlPicture == uri.toString()
                } ?: false

                // CreatorLiveData: Search if is already present into the LiveData
                val isAlreadyPresentIntoCreator = this.mAllPhotosFromCreator?.any {
                    it.urlPicture == uri.toString()
                } ?: false

                // Current photos: Search if is already present into the LiveData
                val isAlreadyPresentIntoCurrentPhotos = this.mPhotosOfCurrentRealEstate?.any {
                    it.urlPicture == uri.toString()
                } ?: false

                /*
                    Present: True else False

                    +----------+---------+---------+
                    | Database | Creator | Current |
                    +----------+---------+---------+
                    | False    | False   | False   | -> OK
                    +----------+---------+---------+
                    | False    | True    | False   | -> NO
                    +----------+---------+---------+
                    | True     | False   | True    | -> OK
                    +----------+---------+---------+
                    | True     | False   | False   | -> NO
                    +----------+---------+---------+
                    | False    | False   | True    | -> NO (Impossible)
                    +----------+---------+---------+
                    | False    | True    | True    | -> NO (Impossible)
                    +----------+---------+---------+
                    | True     | True    | True    | -> NO
                    +----------+---------+---------+
                    | True     | True    | False   | -> NO
                    +----------+---------+---------+
                 */

                if ((!isAlreadyPresentIntoDatabase && !isAlreadyPresentIntoCreator && !isAlreadyPresentIntoCurrentPhotos) ||
                    (isAlreadyPresentIntoDatabase && !isAlreadyPresentIntoCreator && isAlreadyPresentIntoCurrentPhotos) ) {

                    RMPhotoDialog.newInstance(
                        callback = this@RMUpdateFragment,
                        urlPhoto = uri.toString()
                    )
                        .show(
                            this.requireActivity().supportFragmentManager,
                            "DIALOG PHOTO"
                        )
                }
                else {
                    this.mCallback?.showMessage(
                        this.getString(R.string.creation_photo_already_present)
                    )
                }
            }
        }
        else {
            this.mCallback?.showMessage(
                this.getString(R.string.creation_photo_cancel)
            )
        }
    }

    // -- Points of interest --

    /**
     * Action to add POIs
     */
    private fun actionToAddPOIs() {
        // Raw POIs
        val rawTypes = mutableListOf<String>().apply {
            // SCHOOL
            if (this@RMUpdateFragment.mRootView.fragment_edit_Chip_school.isChecked) {
                this.add("school")
            }

            // RESTAURANT
            if (this@RMUpdateFragment.mRootView.fragment_edit_Chip_restaurant.isChecked) {
                this.add("restaurant")
            }

            // DOCTOR
            if (this@RMUpdateFragment.mRootView.fragment_edit_Chip_doctor.isChecked) {
                this.add("doctor")
            }

            // HOSPITAL
            if (this@RMUpdateFragment.mRootView.fragment_edit_Chip_hospital.isChecked) {
                this.add("hospital")
            }

            // AIRPORT
            if (this@RMUpdateFragment.mRootView.fragment_edit_Chip_airport.isChecked) {
                this.add("airport")
            }
        }

        // No POI
        if (rawTypes.isEmpty()) {
            this.mCallback?.showMessage(
                this.getString(R.string.no_pois)
            )
            return
        }

        // No distance for search POI
        val isCanceled = this.configureErrorOfFields(
            this.mRootView.fragment_edit_distance_poi
        )

        if (isCanceled) {
            this.mCallback?.showMessage(
                this.getString(R.string.no_distance)
            )
            return
        }

        this.mGoogleMap?.let {
            // No address
            if (it.projection.visibleRegion.latLngBounds.center.latitude == 0.0 &&
                it.projection.visibleRegion.latLngBounds.center.longitude == 0.0
            ) {
                this.mCallback?.showMessage(
                    this.getString(R.string.no_address)
                )
                return
            }

            // For Google Maps request
            val types = StringBuilder().run {
                rawTypes.forEachIndexed { index, value ->
                    if (index != 0) this.append(",")
                    this.append(value)
                }

                this.toString()
            }

            // Fetch POIsSearch
            this.mViewModel.fetchPOIsSearch(
                context = this.requireContext(),
                latitude = it.projection.visibleRegion.latLngBounds.center.latitude,
                longitude = it.projection.visibleRegion.latLngBounds.center.longitude,
                radius = this.mRootView.fragment_edit_distance_poi.editText?.text.toString().toDouble(),
                types = types
            )
        }
    }

    // -- Real Estate --

    /**
     * Action to update a [RealEstate]
     */
    private fun actionToUpdateRealEstate() {
        // Check if no update
        // todo: 18/04/2020 - No update

        // Errors
        val isCanceled = this.configureErrorOfFields(
            this.mRootView.fragment_edit_type,
            this.mRootView.fragment_edit_price,
            this.mRootView.fragment_edit_surface,
            this.mRootView.fragment_edit_number_of_room,
            this.mRootView.fragment_edit_description,
            this.mRootView.fragment_edit_effective_date
        )

        if (isCanceled) {
            this.mCallback?.showMessage(
                this.getString(R.string.creator_real_estate_lack_information)
            )
            return
        }

        this.mGoogleMap?.let {
            // No address
            if (it.projection.visibleRegion.latLngBounds.center.latitude == 0.0 &&
                it.projection.visibleRegion.latLngBounds.center.longitude == 0.0
            ) {
                this.mCallback?.showMessage(
                    this.getString(R.string.no_address)
                )
                return
            }

            // Show AlertDialog to validate the User's choice
            MaterialAlertDialogBuilder(this.requireContext())
                .setTitle(R.string.navigation_edit_name)
                .setMessage(R.string.message_edit_user_choice)
                .setPositiveButton(R.string.yes) { _, _ ->
                    // todo - 06/04/2020 - Next feature: Add user's authentication instead of 1L
                    val realEstate = RM(
                        mId = this.mItemId,
                        type = this.mRootView.fragment_edit_type.editText?.text?.toString(),
                        price = this.mRootView.fragment_edit_price.editText?.text?.toString()?.toDouble(),
                        surface = this.mRootView.fragment_edit_surface.editText?.text?.toString()?.toDouble(),
                        roomNumber = this.mRootView.fragment_edit_number_of_room.editText?.text?.toString()?.toInt(),
                        description = this.mRootView.fragment_edit_description.editText?.text?.toString(),
                        isEnable = this.mRootView.fragment_edit_enable.isChecked,
                        effectiveDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(this.mRootView.fragment_edit_effective_date.editText?.text?.toString()),
                        saleDate = null,
                        rmAgentId = 1L,
                        address = Address(
                            street = this.mRootView.fragment_edit_address.editText?.text?.toString(),
                            city = this.mRootView.fragment_edit_city.editText?.text?.toString(),
                            postCode = this.mRootView.fragment_edit_post_code.editText?.text?.toString()?.toInt(),
                            country = this.mRootView.fragment_edit_country.editText?.text?.toString(),
                            latitude = this.mGoogleMap?.projection?.visibleRegion?.latLngBounds?.center?.latitude,
                            longitude = this.mGoogleMap?.projection?.visibleRegion?.latLngBounds?.center?.longitude
                        )
                    )

                    this.mViewModel.updateRealEstate(
                        realEstate,
                        this.mPhotosOfCurrentRealEstate,
                        this.mAllPhotosFromCreator,
                        this.mViewModel.getJustNewSelectedPOIs()
                    )
                }
                .setNegativeButton(R.string.no) { _, _ -> /* Do nothing */ }
                .create()
                .show()
        }
    }
}