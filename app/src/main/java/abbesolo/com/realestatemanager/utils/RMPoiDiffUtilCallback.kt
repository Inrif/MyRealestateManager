package abbesolo.com.realestatemanager.utils

import abbesolo.com.realestatemanager.models.POI
import androidx.recyclerview.widget.DiffUtil

/**
 * Created by HOUNSA Romuald on 18/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class RMPoiDiffUtilCallback (
    private val mOldList: List<POI>,
    private val mNewList: List<POI>
) : DiffUtil.Callback() {

    // METHODS -------------------------------------------------------------------------------------

    override fun getOldListSize(): Int = this.mOldList.size

    override fun getNewListSize(): Int = this.mNewList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Comparison based on Name:
        val oldName = this.mOldList[oldItemPosition].name
        val newName = this.mNewList[newItemPosition].name

        return oldName == newName
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Comparison on all fields
        return this.mOldList[oldItemPosition] == this.mNewList[newItemPosition]
    }
}