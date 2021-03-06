package abbesolo.com.realestatemanager.fragments

import android.os.Bundle
import android.view.View
import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.dialogs.DialogListener
import abbesolo.com.realestatemanager.dialogs.RMPhotoDialog
import abbesolo.com.realestatemanager.fragments.adapters.RMAdapterListener
import abbesolo.com.realestatemanager.fragments.adapters.RMPhotoAdapter
import abbesolo.com.realestatemanager.fragments.adapters.RMPoiAdapter
import abbesolo.com.realestatemanager.models.Address
import abbesolo.com.realestatemanager.models.POI
import abbesolo.com.realestatemanager.models.Photo
import abbesolo.com.realestatemanager.models.RM
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.MediaController
import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
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
import kotlinx.android.synthetic.main.fragment_creator.view.*
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_r_m_creator.view.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*



class RMCreatorFragment : RMBaseFragment(), RMAdapterListener, DialogListener, OnMapReadyCallback {

    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var mPhotoAdapter: RMPhotoAdapter
    private lateinit var mPOIsAdapter: RMPoiAdapter
    private var mAllPhotosFromDatabase: List<Photo>? = null
    private var mAllPhotosFromCreator: List<Photo>? = null
    private var mGoogleMap: GoogleMap? = null

    companion object {
        const val REQUEST_CODE_PHOTO = 100
        const val REQUEST_CODE_VIDEO = 200
        const val REQUEST_CODE_AUTOCOMPLETE = 300

        const val BUNDLE_TYPE = "BUNDLE_TYPE"
        const val BUNDLE_PRICE = "BUNDLE_PRICE"
        const val BUNDLE_SURFACE = "BUNDLE_SURFACE"
        const val BUNDLE_ROOM = "BUNDLE_ROOM"
        const val BUNDLE_DESCRIPTION = "BUNDLE_DESCRIPTION"
        const val BUNDLE_STREET = "BUNDLE_STREET"
        const val BUNDLE_CITY = "BUNDLE_CITY"
        const val BUNDLE_POST_CODE = "BUNDLE_POST_CODE"
        const val BUNDLE_COUNTRY = "BUNDLE_COUNTRY"
        const val BUNDLE_LATITUDE = "BUNDLE_LATITUDE"
        const val BUNDLE_LONGITUDE = "BUNDLE_LONGITUDE"
        const val BUNDLE_EFFECTIVE_DATE = "BUNDLE_EFFECTIVE_DATE"

    }

    // METHODS -------------------------------------------------------------------------------------

    // -- BaseFragment --

    @LayoutRes
    override fun getFragmentLayout(): Int = R.layout.fragment_creator

    override fun configureDesign(savedInstanceState: Bundle?) {
        // UI
        this.configureFieldsOfData()
        this.configureListenerOfEachButton()
        this.configurePhotoRecyclerView()
        this.configurePOIsRecyclerView()
        this.configureSupportMapFragment()

        // LiveData
        this.configurePhotosFomDatabase()
        this.configurePhotoCreatorLiveData()
        this.configurePOIsSearch()
    }

    override fun actionAfterPermission(media: Media?) {
        when (media) {
            Media.PHOTO -> this.actionToAddPhoto()
            Media.VIDEO -> this.actionToAddVideo()
            null -> { /* Do nothing */ }
        }
    }

    // -- Fragment --

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            // Photo
            REQUEST_CODE_PHOTO -> this.handlePhoto(resultCode, data)

            // Video
            REQUEST_CODE_VIDEO -> this.handleVideo(resultCode, data)

            // Search
            REQUEST_CODE_AUTOCOMPLETE -> this.handleAddress(resultCode, data)

            else -> { /* Ignore all other requests */ }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save data
        outState.putString(BUNDLE_TYPE, this.mRootView.fragment_creator_type.editText?.text?.toString())
        outState.putString(BUNDLE_PRICE, this.mRootView.fragment_creator_price.editText?.text?.toString())
        outState.putString(BUNDLE_SURFACE, this.mRootView.fragment_creator_surface.editText?.text?.toString())
        outState.putString(BUNDLE_ROOM, this.mRootView.fragment_creator_number_of_room.editText?.text?.toString())
        outState.putString(BUNDLE_DESCRIPTION, this.mRootView.fragment_creator_description.editText?.text?.toString())
        outState.putString(BUNDLE_STREET, this.mRootView.fragment_creator_address.editText?.text?.toString())
        outState.putString(BUNDLE_CITY, this.mRootView.fragment_creator_city.editText?.text?.toString())
        outState.putString(BUNDLE_POST_CODE, this.mRootView.fragment_creator_post_code.editText?.text?.toString())
        outState.putString(BUNDLE_COUNTRY, this.mRootView.fragment_creator_country.editText?.text?.toString())
        outState.putDouble(BUNDLE_LATITUDE, this.mGoogleMap?.projection?.visibleRegion?.latLngBounds?.center?.latitude ?: 0.0)
        outState.putDouble(BUNDLE_LONGITUDE, this.mGoogleMap?.projection?.visibleRegion?.latLngBounds?.center?.longitude ?: 0.0)
//        outState.putBoolean(BUNDLE_ENABLE, this.mRootView.fragment_creator_enable.isChecked)
        outState.putString(BUNDLE_EFFECTIVE_DATE, this.mRootView.fragment_creator_effective_date.editText?.text?.toString())

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Restores data
        savedInstanceState?.let { bundle ->
            // TYPE
            this.mRootView.fragment_creator_type.editText?.text?.let {
                it.clear()
                it.append(bundle.getString(BUNDLE_TYPE, ""))
            }
            // PRICE
            this.mRootView.fragment_creator_price.editText?.text?.let {
                it.clear()
                it.append(bundle.getString(BUNDLE_PRICE, ""))
            }
            // SURFACE
            this.mRootView.fragment_creator_surface.editText?.text?.let {
                it.clear()
                it.append(bundle.getString(BUNDLE_SURFACE, ""))
            }
            // ROOM
            this.mRootView.fragment_creator_number_of_room.editText?.text?.let {
                it.clear()
                it.append(bundle.getString(BUNDLE_ROOM, ""))
            }
            // DESCRIPTION
            this.mRootView.fragment_creator_description.editText?.text?.let {
                it.clear()
                it.append(bundle.getString(BUNDLE_DESCRIPTION, ""))
            }
            // STREET
            this.mRootView.fragment_creator_address.editText?.text?.let {
                it.clear()
                it.append(bundle.getString(BUNDLE_STREET, ""))
            }
            // CITY
            this.mRootView.fragment_creator_city.editText?.text?.let {
                it.clear()
                it.append(bundle.getString(BUNDLE_CITY, ""))
            }
            // POST CODE
            this.mRootView.fragment_creator_post_code.editText?.text?.let {
                it.clear()
                it.append(bundle.getString(BUNDLE_POST_CODE, ""))
            }
            // COUNTRY
            this.mRootView.fragment_creator_country.editText?.text?.let {
                it.clear()
                it.append(bundle.getString(BUNDLE_COUNTRY, ""))
            }
            // LATITUDE & LONGITUDE
            val latitude = bundle.getDouble(BUNDLE_LATITUDE, 0.0)
            val longitude = bundle.getDouble(BUNDLE_LONGITUDE, 0.0)
            this.showPointOfInterest(LatLng(latitude, longitude))
            // ENABLE
//            this.mRootView.fragment_creator_enable.isChecked =
//                bundle.getBoolean(BUNDLE_ENABLE, true)
            // EFFECTIVE DATE
            this.mRootView.fragment_creator_effective_date.editText?.text?.let {
                it.clear()
                it.append(bundle.getString(BUNDLE_EFFECTIVE_DATE, ""))
            }

            // Display fields and child fragment
            if (!this.mRootView.fragment_creator_city.editText?.text.isNullOrEmpty()) {
                this.mRootView.fragment_creator_address.visibility = View.VISIBLE
                this.mRootView.fragment_creator_city.visibility = View.VISIBLE
                this.mRootView.fragment_creator_post_code.visibility = View.VISIBLE
                this.mRootView.fragment_creator_country.visibility = View.VISIBLE

                // Hides the fragment
                this.childFragmentManager.fragments[0].view?.visibility = View.VISIBLE
            }
        }
    }

    // -- AdapterListener interface --

    override fun onDataChanged() {
        // Photos
        this.mRootView.fragment_creator_RecyclerView_photo.visibility =
            if (this.mPhotoAdapter.itemCount != 0)
                View.VISIBLE
            else
                View.GONE

        // POIs
        this.mRootView.fragment_creator_RecyclerView_poi.visibility =
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
                    callback = this@RMCreatorFragment,
                    urlPhoto = (v.tag as Photo).urlPicture,
                    description = (v.tag as Photo).description,
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

    // -- Fields of data --

    /**
     * Configures the fields of data
     */
    private fun configureFieldsOfData() {
        this.configureListenerOfFields(
            this.mRootView.fragment_creator_type,
            this.mRootView.fragment_creator_price,
            this.mRootView.fragment_creator_surface,
            this.mRootView.fragment_creator_number_of_room,
            this.mRootView.fragment_creator_description,
            this.mRootView.fragment_creator_effective_date,
            this.mRootView.fragment_creator_distance_poi,

        )

        // Hides field for address
        this.mRootView.fragment_creator_address.visibility = View.GONE
        this.mRootView.fragment_creator_city.visibility = View.GONE
        this.mRootView.fragment_creator_post_code.visibility = View.GONE
        this.mRootView.fragment_creator_country.visibility = View.GONE

        // Type: Populates the adapter
        (this.mRootView.fragment_creator_type.editText as? AutoCompleteTextView)?.setAdapter(
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
        // Show field for address
        this.mRootView.fragment_creator_address.visibility = View.VISIBLE
        this.mRootView.fragment_creator_city.visibility = View.VISIBLE
        this.mRootView.fragment_creator_post_code.visibility = View.VISIBLE
        this.mRootView.fragment_creator_country.visibility = View.VISIBLE

        // Show Google Maps
        this.childFragmentManager.fragments[0].view?.visibility = View.VISIBLE

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
        this.mRootView.fragment_creator_address.editText?.text?.let {
            it.clear()

            val fullStreet = StringBuilder().run {
                append(streetNumber ?: this@RMCreatorFragment.getString(R.string.details_no_street_number))
                append(" ")
                append(route ?: this@RMCreatorFragment.getString(R.string.details_no_street))
                toString()
            }

            it.append(fullStreet)
        }

        // City
        this.mRootView.fragment_creator_city.editText?.text?.let {
            it.clear()
            it.append(locality ?: this.getString(R.string.details_no_city))
        }

        // Post code
        this.mRootView.fragment_creator_post_code.editText?.text?.let {
            it.clear()
            it.append(
                if (postalCode.isNullOrEmpty())
                    "0"
                else
                    postalCode
            )
        }

        // Country
        this.mRootView.fragment_creator_country.editText?.text?.let {
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
        this.mRootView.fragment_creator_add_address.setOnClickListener {
            this.actionToSearchAddress()
        }

        // Button: Add photo
        this.mRootView.fragment_creator_add_photo.setOnClickListener {
            this.actionToAddPhoto()
        }

        // Button: Add video
        this.mRootView.fragment_creator_video_container.visibility = View.GONE
        this.mRootView.fragment_creator_add_video.setOnClickListener {
            this.actionToAddVideo()
        }

        // Button: Add points of interest
        this.mRootView.fragment_creator_search_poi.setOnClickListener {
            this.actionToAddPOIs()
        }

        // FAB
        this.mRootView.fragment_creator_fab.setOnClickListener {
            this.actionToAddRealEstate()
        }
    }

    // -- RecyclerView --

    /**
     * Configures the photo RecyclerView
     */
    private fun configurePhotoRecyclerView() {
        // Adapter
        this.mPhotoAdapter = RMPhotoAdapter(
            mCallback = this@RMCreatorFragment,
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
        with(this.mRootView.fragment_creator_RecyclerView_photo) {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(divider)
            adapter = this@RMCreatorFragment.mPhotoAdapter
            visibility = View.GONE
        }
    }

    /**
     * Configures the POIs RecyclerView
     */
    private fun configurePOIsRecyclerView() {
        // Adapter
        this.mPOIsAdapter = RMPoiAdapter(
            mCallback = this@RMCreatorFragment,
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
        with(this.mRootView.fragment_creator_RecyclerView_poi) {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(divider)
            adapter = this@RMCreatorFragment.mPOIsAdapter
            visibility = View.GONE
        }
    }

    // -- Child Fragment --

    /**
     * Configures the child fragment which contains the Google Maps
     */
    private fun configureSupportMapFragment() {
        var childFragment = this.childFragmentManager
            .findFragmentById(R.id.fragment_creator_map_lite_mode) as? SupportMapFragment

        if (childFragment == null) {
            childFragment = SupportMapFragment.newInstance()

            this.childFragmentManager.beginTransaction()
                .add(R.id.fragment_creator_map_lite_mode, childFragment)
                .commit()
        }

        childFragment?.getMapAsync(this@RMCreatorFragment)

        // To keep the instance after configuration change (rotation)
        childFragment?.retainInstance = true

        // Hides the fragment
        this.childFragmentManager.fragments[0].view?.visibility = View.GONE
    }

    // -- LiveData --

    /**
     * Configures all [Photo] from database
     */
    private fun configurePhotosFomDatabase() {
        this.mViewModel
            .getPhotos()
            .observe(
                this.viewLifecycleOwner,
               { this.mAllPhotosFromDatabase = it }
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
                 {
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
                 {
                    if (it.isEmpty()) {
                        this.mCallback?.showMessage(
                            this.getString(R.string.no_pois_search)
                        )
                    }

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
                 { /* Do nothing */ }
            )
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
            val intent = Intent(Intent.ACTION_PICK,
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

                /*
                    Present: True else False

                    +----------+---------+
                    | Database | Creator |
                    +----------+---------+
                    | False    | False   | -> OK
                    +----------+---------+
                    | False    | True    | -> NO
                    +----------+---------+
                    | True     | False   | -> NO
                    +----------+---------+
                    | True     | True    | -> NO
                    +----------+---------+
                 */

                if (!isAlreadyPresentIntoDatabase && !isAlreadyPresentIntoCreator) {
                    RMPhotoDialog.newInstance(
                        callback = this@RMCreatorFragment,
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

    // -- Video --

    /**
     * Action to add a video
     */
    private fun actionToAddVideo() {
        if (this.checkReadExternalStoragePermission(Media.VIDEO)) {
            // Goal: Retrieves a photo from external storage
            val intent = Intent(Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI)

            this.startActivityForResult(intent, REQUEST_CODE_VIDEO)
        }
    }

    /**
     * Handles the video
     * @param resultCode    an [Int] that contains the result code
     * @param data          an [Intent] that contains the data
     */
    private fun handleVideo(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Container must be visible
                this.mRootView.fragment_creator_video_container.visibility = View.VISIBLE

                val controller = MediaController(this.requireContext()).apply {
                    setAnchorView(this@RMCreatorFragment.mRootView.fragment_creator_video)
                }

                with(this.mRootView.fragment_creator_video) {
                    setVideoURI(uri)
                    setMediaController(controller)
                }

                this.mRootView.fragment_creator_video.start()
            }
        }
        else {
            this.mCallback?.showMessage(
                this.getString(R.string.creation_video_cancel)
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
            if (this@RMCreatorFragment.mRootView.fragment_creator_Chip_school.isChecked) {
                this.add("school")
            }

            // RESTAURANT
            if (this@RMCreatorFragment.mRootView.fragment_creator_Chip_restaurant.isChecked) {
                this.add("restaurant")
            }

            // DOCTOR
            if (this@RMCreatorFragment.mRootView.fragment_creator_Chip_doctor.isChecked) {
                this.add("doctor")
            }

            // HOSPITAL
            if (this@RMCreatorFragment.mRootView.fragment_creator_Chip_hospital.isChecked) {
                this.add("hospital")
            }

            // AIRPORT
            if (this@RMCreatorFragment.mRootView.fragment_creator_Chip_airport.isChecked) {
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
            this.mRootView.fragment_creator_distance_poi
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
                radius = this.mRootView.fragment_creator_distance_poi.editText?.text.toString().toDouble(),
                types = types
            )
        }
    }

    // -- Real Estate --

    /**
     * Action to add a [RM]
     */
    private fun actionToAddRealEstate() {
        // Errors
        val isCanceled = this.configureErrorOfFields(
            this.mRootView.fragment_creator_type,
            this.mRootView.fragment_creator_price,
            this.mRootView.fragment_creator_surface,
            this.mRootView.fragment_creator_number_of_room,
            this.mRootView.fragment_creator_description,
            this.mRootView.fragment_creator_effective_date,

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
                .setTitle(R.string.navigation_creator_name)
                .setMessage(R.string.message_creator_user_choice)
                .setPositiveButton(R.string.yes) { _, _ ->

                    val realEstate = RM(
                        type = this.mRootView.fragment_creator_type.editText?.text?.toString(),
                        price = this.mRootView.fragment_creator_price.editText?.text?.toString()?.toDouble(),
                        surface = this.mRootView.fragment_creator_surface.editText?.text?.toString()?.toDouble(),
                        roomNumber = this.mRootView.fragment_creator_number_of_room.editText?.text?.toString()?.toInt(),
                        description = this.mRootView.fragment_creator_description.editText?.text?.toString(),
//                        isEnable = this.mRootView.fragment_creator_enable.isChecked,
                        isEnable = null,
                        effectiveDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(this.mRootView.fragment_creator_effective_date.editText?.text?.toString()),
                        saleDate = null,
                        rmAgentId = 1L,
                        address = Address(
                            street = this.mRootView.fragment_creator_address.editText?.text?.toString(),
                            city = this.mRootView.fragment_creator_city.editText?.text?.toString(),
                            postCode = this.mRootView.fragment_creator_post_code.editText?.text?.toString()?.toInt(),
                            country = this.mRootView.fragment_creator_country.editText?.text?.toString(),
                            latitude = this.mGoogleMap?.projection?.visibleRegion?.latLngBounds?.center?.latitude,
                            longitude = this.mGoogleMap?.projection?.visibleRegion?.latLngBounds?.center?.longitude
                        )
                    )

                    this.mViewModel.insertRealEstate(
                        this.requireContext(),
                        realEstate,
                        this.mAllPhotosFromCreator,
                        this.mViewModel.getSelectedPOIs()
                    )



                }
                .setNegativeButton(R.string.no) { _, _ -> /* Do nothing */ }
                .create()
                .show()

        }




    }

}