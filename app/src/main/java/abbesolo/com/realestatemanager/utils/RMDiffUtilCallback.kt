package abbesolo.com.realestatemanager.utils

import abbesolo.com.realestatemanager.models.RMAndPhotos
import androidx.recyclerview.widget.DiffUtil

/**
 * Created by HOUNSA Romuald on 18/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class RMDiffUtilCallback (
    private val mOldList: List<RMAndPhotos>,
    private val mNewList: List<RMAndPhotos>
) : DiffUtil.Callback() {

    // METHODS -------------------------------------------------------------------------------------

    override fun getOldListSize(): Int = this.mOldList.size

    override fun getNewListSize(): Int = this.mNewList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Comparaison based on Id:
        val oldId = this.mOldList[oldItemPosition].rm?.mId ?: 0L
        val newId = this.mNewList[newItemPosition].rm?.mId ?: 0L

        return oldId == newId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Comparison on all fields
        return this.mOldList[oldItemPosition] == this.mNewList[newItemPosition]
    }
}