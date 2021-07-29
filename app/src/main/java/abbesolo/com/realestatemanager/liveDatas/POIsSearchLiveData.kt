package abbesolo.com.realestatemanager.liveDatas

import abbesolo.com.realestatemanager.models.POI
import androidx.lifecycle.LiveData
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import timber.log.Timber

/**
 * Created by HOUNSA Romuald on 19/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 *
 * A [LiveData] of [List] of [PointOfInterest] subclass.
 */
class POIsSearchLiveData : LiveData<List<POI>>() {

    // FIELDS --------------------------------------------------------------------------------------

    private var mDisposable: Disposable? = null
    private val mPOIs = mutableListOf<POI>()
    private val mAlreadySelectedPOIs = mutableListOf<POI>()

    // METHODS -------------------------------------------------------------------------------------

    // -- LiveData --

    override fun onInactive() {
        super.onInactive()

        // Disposes the Disposable
        this.mDisposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    // -- Points of interest --

    /**
     * Gets the POIs with an [Single]
     * @param single a [Single] of [List] of [PointOfInterest]
     */
    fun getPOIsSearchWithSingle(single: Single<List<POI>>) {
        // Creates stream
        this.mDisposable = single.subscribeWith(object : DisposableSingleObserver<List<POI>>() {

            override fun onSuccess(result: List<POI>) {
                with(this@POIsSearchLiveData.mPOIs) {
                    clear()
                    addAll(result)
                }

                // Add current POIs if possible
                if (this@POIsSearchLiveData.mAlreadySelectedPOIs.isNotEmpty()) {
                    this@POIsSearchLiveData.mAlreadySelectedPOIs.forEach { poiFromDB ->
                        // Search if already present
                        val index = this@POIsSearchLiveData.mPOIs.indexOfFirst {
                            it.name == poiFromDB.name &&
                                    it.address?.latitude == poiFromDB.address?.latitude &&
                                    it.address?.longitude == poiFromDB.address?.longitude
                        }

                        if (index != -1) {
                            this@POIsSearchLiveData.mPOIs[index].isSelected = true
                        }
                        else {
                            this@POIsSearchLiveData.mPOIs.add(poiFromDB)
                        }
                    }
                }

                // Notify
                this@POIsSearchLiveData.value = this@POIsSearchLiveData.mPOIs
            }

            override fun onError(e: Throwable) {
                Timber.e("onError: ${e.message}")
            }
        })
    }

    /**
     * Adds all current [PointOfInterest]
     * @param poiList a [List] of [PointOfInterest]
     */
    fun addCurrentPOIs(poiList: List<POI>) {
        // MODE EDIT
        with(this.mAlreadySelectedPOIs) {
            clear()
            addAll(poiList)
        }

        // Add POIs if possible
        this.mAlreadySelectedPOIs.forEach { poiFromDB ->
            // Search if already present
            val index = this.mPOIs.indexOfFirst {
                it.name == poiFromDB.name &&
                        it.address?.latitude == poiFromDB.address?.latitude &&
                        it.address?.longitude == poiFromDB.address?.longitude
            }

            if (index != -1) {
                this.mPOIs[index].isSelected = true
            }
            else {
                this.mPOIs.add(poiFromDB)
            }
        }

        // Notify
        this.value = this.mPOIs
    }

    /**
     * Checks if the [PointOfInterest] is selected
     * @param poi a [PointOfInterest]
     */
    fun checkPOI(poi: POI) {
        this.mPOIs.forEach {
            if (it == poi) {
                it.isSelected = !poi.isSelected
            }
        }

        // Notify
        this.value = this.mPOIs
    }

    /**
     * Gets all selected [PointOfInterest]
     */
    fun getSelectedPOIs(): List<POI> = this.mPOIs.filter { it.isSelected }

    /**
     * Gets just new selected [PointOfInterest]
     */
    // todo: 17/04/2020 - Remove it when the RealEstateViewModel#updateRealEstate method will be update
    fun getJustNewSelectedPOIs(): List<POI> =
        this.mPOIs.filter { it.isSelected && it.id == 0L}
}