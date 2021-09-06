package abbesolo.com.realestatemanager.fragments.adapters

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.googleMapsApi.MapApi
import abbesolo.com.realestatemanager.models.POI
import abbesolo.com.realestatemanager.utils.RMPoiDiffUtilCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_photo.view.*
import kotlinx.android.synthetic.main.item_poi.view.*
import java.lang.ref.WeakReference

/**
 * Created by HOUNSA Romuald on 18/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */


class RMPoiAdapter( private val mCallback: RMAdapterListener? = null,
                    private val mMode: CheckBoxDisplayMode = CheckBoxDisplayMode.NORMAL_MODE): RecyclerView.Adapter<RMPoiAdapter.RMPoiViewHolder>() {


    // ENUMS ---------------------------------------------------------------------------------------

    enum class CheckBoxDisplayMode {NORMAL_MODE, SELECT_MODE}

    // FIELDS --------------------------------------------------------------------------------------

    private val mPOIs = mutableListOf<POI>()

    // METHODS -------------------------------------------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RMPoiViewHolder {
        // Creates the View thanks to the inflater
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poi, parent, false)

        return RMPoiViewHolder(view, WeakReference(this.mCallback))
    }

    override fun onBindViewHolder(holder: RMPoiViewHolder, position: Int) {
        this.configureDesign(holder, this.mPOIs[position])
    }

    override fun getItemCount(): Int = this.mPOIs.size

    // -- Design item --

    /**
     * Configures the design of each item
     * @param holder    a [RMPoiViewHolder] that corresponds to the item
     * @param poi       a [POI]
     */
    private fun configureDesign(holder: RMPoiViewHolder, poi: POI) {
        // Image
        poi.urlPicture?.let {
            val urlRequest = MapApi.getPhoto(
                photoReference = it,
                maxWidth = 400,
                key = holder.itemView.context.getString(R.string.google_maps_key)
            )

            Glide.with(holder.itemView)
                .load(urlRequest)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .fallback(R.drawable.ic_add_location)
                .error(R.drawable.ic_clear)
                .into(holder.itemView.item_poi_image)
        }

        // Name
        poi.name?.let { holder.itemView.item_poi_name.text = it }

        // CheckBox
        when (this.mMode) {
            CheckBoxDisplayMode.NORMAL_MODE -> {
                holder.itemView.item_poi_is_selected.visibility = View.GONE
            }

            CheckBoxDisplayMode.SELECT_MODE -> {
                // Is selected
                holder.itemView.item_poi_is_selected.isChecked = poi.isSelected

                // To not interact with it in EditFragment

                if (poi.isSelected && poi.id != 0L) {
                    holder.itemView.item_poi_is_selected.isEnabled = false
                }

                holder.itemView.item_poi_is_selected.setOnClickListener {
                    // Tag -> POI
                    it.tag = poi

                    // Starts the callback
                    holder.mCallback.get()?.onClick(it)
                }
            }
        }
    }

    // -- Point of interest --

    /**
     * Updates data of [RMPoiAdapter ]
     * @param newPOIs a [List] of [POI]
     */
    fun updateData(newPOIs: List<POI>) {
        // Optimizes the performances of RecyclerView
        val diffCallback  = RMPoiDiffUtilCallback (this.mPOIs, newPOIs)
        val diffResult  = DiffUtil.calculateDiff(diffCallback )

        // New data
        this.mPOIs.clear()
        this.mPOIs.addAll(newPOIs)

        // Notifies adapter
        diffResult.dispatchUpdatesTo(this@RMPoiAdapter)

        // Callback
        this.mCallback?.onDataChanged()
    }

    // NESTED CLASSES ------------------------------------------------------------------------------

    /**
     * A [RecyclerView.ViewHolder] subclass.
     */
    class RMPoiViewHolder(
        itemView: View,
        var mCallback: WeakReference<RMAdapterListener?>
    ) : RecyclerView.ViewHolder(itemView)
}