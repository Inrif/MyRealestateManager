package abbesolo.com.realestatemanager.fragments

import android.view.View

/**
 * Created by Romuald Hounsa on 21/06/2020.
 * Name of the project: RealEstateManager
 */
interface FragmentListener {

    // METHODS -------------------------------------------------------------------------------------

    /**
     * Called from fragment to activity
     */
    fun showMessage(message: String)

    /**
     * Called when the user has selected an item of [RMListFragment]
     * @param v The [View] that was clicked.
     */
    fun navigateToDetailsFragment(v: View?)
}