package abbesolo.com.realestatemanager.fragments.adapters

import android.view.View

/**
 * Created by HOUNSA Romuald on 05/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */

interface RMAdapterListener {

    // METHODS -------------------------------------------------------------------------------------

    /**
     * Called with the updateData method of adapter
     */
    fun onDataChanged()

    /**
     * Called when a view has been clicked.
     * @param v The [View] that was clicked.
     */
    fun onClick(v: View?)
}