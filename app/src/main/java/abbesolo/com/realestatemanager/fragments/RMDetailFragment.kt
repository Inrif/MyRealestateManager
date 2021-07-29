package abbesolo.com.realestatemanager.fragments

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.fragments.adapters.RMAdapterListener
import abbesolo.com.realestatemanager.fragments.adapters.RMPhotoAdapter
import abbesolo.com.realestatemanager.fragments.adapters.RMPoiAdapter
import abbesolo.com.realestatemanager.models.POI
import abbesolo.com.realestatemanager.models.RMAndPhotos
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
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
import kotlinx.android.synthetic.main.fragment_r_m_detail.view.*
import kotlinx.android.synthetic.main.fragment_r_m_details.view.*
import java.util.*

@Suppress("UNREACHABLE_CODE")
class RMDetailFragment :RMBaseFragment(), RMAdapterListener, OnMapReadyCallback {

    // FIELDS --------------------------------------------------------------------------------------

    private val mItemId: Long by lazy {
        RMDetailFragmentArgs.fromBundle(this.requireArguments()).itemId
    }

    private lateinit var mPhotoAdapter: RMPhotoAdapter
    private lateinit var mPOIsAdapter: RMPoiAdapter
    private var mGoogleMap: GoogleMap? = null

    // METHODS -------------------------------------------------------------------------------------

    // -- BaseFragment --

    @LayoutRes
    override fun getFragmentLayout(): Int = R.layout.fragment_r_m_details

    override fun configureDesign(savedInstanceState: Bundle?) {
        // Argument
        this.eventWhenArgumentEqualsDefaultValue()

        // UI
        this.configurePhotoRecyclerView()
        this.configurePOIsRecyclerView()
        this.configureSupportMapFragment()

        // LiveData
        this.configureRealEstateLiveData()
        this.configurePOIsLiveData()
    }

    // -- AdapterListener interface --

    override fun onDataChanged() {
        // Photos
        this.mRootView.fragment_details_no_data_photo.visibility =
            if (this.mPhotoAdapter.itemCount == 0)
                View.VISIBLE
            else
                View.GONE

        // POIs
        this.mRootView.fragment_details_no_data_poi.visibility =
            if (this.mPOIsAdapter.itemCount == 0)
                View.VISIBLE
            else
                View.GONE
    }

    override fun onClick(v: View?) { /* Do nothing */ }

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
            this.mCallback?.showMessage(this.getString(R.string.details_impossible))

            // Finish this fragment
            this.findNavController().popBackStack()
        }
    }

    // -- RecyclerView --

    /**
     * Configures the RecyclerView
     */
    private fun configurePhotoRecyclerView() {
        // Adapter
        this.mPhotoAdapter = RMPhotoAdapter(
            mCallback = this@RMDetailFragment
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
        with(this.mRootView.fragment_details_RecyclerView_photo) {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(divider)
            adapter = this@RMDetailFragment.mPhotoAdapter
        }
    }

    /**
     * Configures the POIs RecyclerView
     */
    private fun configurePOIsRecyclerView() {
        // Adapter
        this.mPOIsAdapter = RMPoiAdapter(
            mCallback = this@RMDetailFragment
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
        with(this.mRootView.fragment_details_RecyclerView_poi) {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(divider)
            adapter = this@RMDetailFragment.mPOIsAdapter
        }
    }

    // -- Child Fragment --

    /**
     * Configures the child fragment which contains the Google Maps
     */
    private fun configureSupportMapFragment() {
        var childFragment = this.childFragmentManager
            .findFragmentById(R.id.fragment_details_map_lite_mode) as? SupportMapFragment

        if (childFragment == null) {
            childFragment = SupportMapFragment.newInstance()

            this.childFragmentManager.beginTransaction()
                .add(R.id.fragment_details_map_lite_mode, childFragment)
                .commit()
        }

        childFragment?.getMapAsync(this@RMDetailFragment)

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
            .observe(this.viewLifecycleOwner,
                Observer { this.configureUI(it) }
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
                Observer {
                    it.poi?.let { poiList ->
                        // Sorts the list on its name from A to Z
                        Collections.sort(poiList, POI.AZTitleComparator())

                        this.mPOIsAdapter.updateData(poiList)
                    }
                }
            )
    }

    // -- UI --

    /**
     * Configures UI
     * @param realEstateWithPhotos a [RealEstateWithPhotos]
     */
    private fun configureUI(realEstateWithPhotos: RMAndPhotos?) {
        realEstateWithPhotos?.let {
            // Photos
            it.photos?.let { photos ->
                this.mPhotoAdapter.updateData(photos)
            }

            // Real estate
            it.rm?.let { realEstate ->
                // Description
                this.mRootView.fragment_details_description.text =
                    realEstate.description ?:
                            this.getString(R.string.details_no_description)

                // Characteristics
                this.mRootView.fragment_details_surface.text = this.getString(
                    R.string.details_characteristics,
                    realEstate.surface ?: 0.0,
                    realEstate.roomNumber ?: 0
                )

                // Address
                realEstate.address?.let { address ->
                    val fullAddress = """
                        ${address.street ?: this.getString(R.string.details_no_street)}
                        ${address.city ?: this.getString(R.string.details_no_city)}
                        ${address.postCode ?: "0"}
                        ${address.country ?: this.getString(R.string.details_no_country)}
                    """.trimIndent()

                    this.mRootView.fragment_details_address.text = fullAddress

                    // Google Maps
                    this.showPointOfInterest(
                        LatLng(
                            address.latitude ?: 0.0,
                            address.longitude ?: 0.0
                        )
                    )
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



}
