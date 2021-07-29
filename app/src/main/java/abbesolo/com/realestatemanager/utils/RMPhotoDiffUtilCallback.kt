package abbesolo.com.realestatemanager.utils

import abbesolo.com.realestatemanager.models.Photo
import androidx.recyclerview.widget.DiffUtil

/**
 * Created by HOUNSA Romuald on 18/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class RMPhotoDiffUtilCallback  (private val mOldList: List<Photo>,
private val mNewList: List<Photo>
) : DiffUtil.Callback() {

    // METHODS -------------------------------------------------------------------------------------

    override fun getOldListSize(): Int = this.mOldList.size

    override fun getNewListSize(): Int = this.mNewList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Comparison based on Id:
        val oldId = this.mOldList[oldItemPosition].id
        val newId = this.mNewList[newItemPosition].id

        return oldId == newId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Comparison on all fields
        return this.mOldList[oldItemPosition] == this.mNewList[newItemPosition]
    }
}