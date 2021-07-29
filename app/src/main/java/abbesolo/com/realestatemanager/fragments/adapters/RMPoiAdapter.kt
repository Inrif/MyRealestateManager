package abbesolo.com.realestatemanager.fragments.adapters

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.googleMapsApi.MapApi
import abbesolo.com.realestatemanager.models.POI
import abbesolo.com.realestatemanager.utils.RMPhotoDiffUtilCallback
import abbesolo.com.realestatemanager.utils.RMPoiDiffUtilCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_photo.view.*
import kotlinx.android.synthetic.main.item_poi.view.*

/**
 * Created by HOUNSA Romuald on 18/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 *
 *
 */


class RMPoiAdapter( private val mCallback: RMAdapterListener? = null,
                    private val mMode: CheckBoxDisplayMode = CheckBoxDisplayMode.NORMAL_MODE): RecyclerView.Adapter<RMPoiAdapter.RMPoiViewHolder>() {


    // ENUMS ---------------------------------------------------------------------------------------

    enum class CheckBoxDisplayMode {NORMAL_MODE, SELECT_MODE}

    // FIELDS --------------------------------------------------------------------------------------


    private val poi = mutableListOf<POI>()


    class RMPoiViewHolder (itemView: View): RecyclerView.ViewHolder (itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RMPoiViewHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return RMPoiViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RMPoiViewHolder, position: Int) {
        val currentItem = poi[position]


          // Image
        currentItem.urlPicture?.let {
            val urlRequest = MapApi.getPhoto(
                photoReference = it,
                maxWidth = 400,
                key = holder.itemView.context.getString(R.string.google_maps_key)
            )

            Glide.with(holder.itemView)
                .load(urlRequest)
                .centerCrop()
                .placeholder(R.drawable.placeholder_background)
                .fallback(R.drawable.ic_baseline_add_location_24)
                .error(R.drawable.ic_baseline_clear_24)
                .into(holder.itemView.item_poi_image)
        }

        // Name
        currentItem.name?.let { holder.itemView.item_poi_name.text = it }

        // CheckBox
        when (this.mMode) {
            CheckBoxDisplayMode.NORMAL_MODE -> {
                holder.itemView.item_poi_is_selected.visibility = View.GONE
            }

            CheckBoxDisplayMode.SELECT_MODE -> {
                // Is selected
                holder.itemView.item_poi_is_selected.isChecked = currentItem.isSelected

                // To not interact with it in EditFragment
                // todo: 17/04/2020 - Remove it when the RealEstateViewModel#updateRealEstate method will be update
                if (currentItem.isSelected && currentItem.id != 0L) {
                    holder.itemView.item_poi_is_selected.isEnabled = false
                }

                holder.itemView.item_poi_is_selected.setOnClickListener {
                    // Tag -> POI
                    it.tag = currentItem

                    // Starts the callback
                 //   holder.mCallback.get()?.onClick(it)
                }
            }
        }
    }

    // -- Point of interest --

    /**
     * Updates data of [POIsAdapter]
     * @param newPOIs a [List] of [PointOfInterest]
     */
    fun updateData(newPOIs: List<POI>) {
        // Optimizes the performances of RecyclerView
        val diffCallback  = RMPoiDiffUtilCallback(this.poi, newPOIs)
        val diffResult  = DiffUtil.calculateDiff(diffCallback )

        // New data
        this.poi.clear()
        this.poi.addAll(newPOIs)

        // Notifies adapter
        diffResult.dispatchUpdatesTo(this@RMPoiAdapter)

        // Callback
     //   this.mCallback?.onDataChanged()

        // Notify data changes

        this@RMPoiAdapter.notifyDataSetChanged()
    }



    override fun getItemCount() = poi.size

}
