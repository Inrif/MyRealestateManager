package abbesolo.com.realestatemanager.fragments.adapters

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.models.RMAndPhotos
import abbesolo.com.realestatemanager.utils.RMDiffUtilCallback
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_list_rm.view.*
import kotlinx.android.synthetic.main.item_real_estate.view.*
import java.lang.ref.WeakReference


/**
 * Created by HOUNSA Romuald on 05/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */

class RMListAdapter( private val mCallback: RMAdapterListener? = null
) : RecyclerView.Adapter<RMListAdapter.RealEstateViewHolder>() {

    // FIELDS --------------------------------------------------------------------------------------

    private var mRealEstates = emptyList<RMAndPhotos>()

    // METHODS -------------------------------------------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RealEstateViewHolder {
        // Creates the View thanks to the inflater
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_real_estate, parent, false)

        return RealEstateViewHolder(view, WeakReference(this.mCallback))
    }

    override fun onBindViewHolder(holder: RealEstateViewHolder, position: Int) {
        this.configureDesign(holder, this.mRealEstates[position])
    }

    override fun getItemCount(): Int = this.mRealEstates.size

    // -- Design item --

    /**
     * Configures the design of each item
     * @param holder        a [RealEstateViewHolder] that corresponds to the item
     * @param realEstate    a [RMAndPhotos]
     */
    private fun configureDesign(holder: RealEstateViewHolder, realEstate: RMAndPhotos) {
        // CardView
        holder.itemView.item_real_estate_CardView.setOnClickListener {
            // Background of item
            this.configureBackgroundItem(realEstate)

            // Tag -> Data's Id
            it.tag = realEstate.rm?.mId

            // Starts the callback
            holder.mCallback.get()?.onClick(it)
        }

        // Image
        realEstate.photos?.let {
            if (it.isNotEmpty()) {
                Glide.with(holder.itemView)
                    .load(it[0].urlPicture)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .fallback(R.drawable.ic_photo)
                    .error(R.drawable.ic_clear)
                    .into(holder.itemView.item_real_estate_image)
            }
            else {
                holder.itemView.item_real_estate_image.setImageResource(R.drawable.ic_photo)
            }
        }

        // Type
        realEstate.rm?.type?.let { holder.itemView.item_real_estate_type.text = it }

        // City
        realEstate.rm?.address?.city?.let { holder.itemView.item_real_estate_city.text = it }

        // Price
        realEstate.rm?.price?.let {
            val price = "$it \$"
            holder.itemView.item_real_estate_price.text = price }

        // Background color
        realEstate.rm?.isSelected?.let { isSelected ->
            // CardView
            holder.itemView.item_real_estate_CardView.setCardBackgroundColor(
                if (isSelected)
                    ContextCompat.getColor(holder.itemView.context, R.color.colorAccent)
                else
                    Color.WHITE
            )

            // TextView
            holder.itemView.item_real_estate_price.setTextColor(
                if (isSelected)
                    Color.WHITE
                else
                    ContextCompat.getColor(holder.itemView.context, R.color.colorAccent)
            )
        }
    }

    /**
     * Configures the background of item
     * @param realEstate a [RMAndPhotos]
     */
    private fun configureBackgroundItem(realEstate: RMAndPhotos) {
        realEstate.rm?.mId?.let { id ->
            // Create a new List
            val newRealEstates = mutableListOf<RMAndPhotos>().apply {
                // Add all items
                this@RMListAdapter.mRealEstates.forEach {
                    val item = it.rm?.copy()
                    this.add(it.copy(rm = item))
                }

                // Reset on the previous item selected
                forEach {
                    it.rm?.isSelected = it.rm?.mId == id
                }
            }

            // Update UI
            this.updateData(newRealEstates)
        }
    }

    // -- Real Estate --

    /**
     * Updates data of [RMListAdapter]
     * @param newRealEstates a [List] of [RMAndPhotos]
     */
    fun updateData(newRealEstates: List<RMAndPhotos>) {
        // Optimizes the performances of RecyclerView
        val diffCallback  = RMDiffUtilCallback(this.mRealEstates, newRealEstates)
        val diffResult  = DiffUtil.calculateDiff(diffCallback )

        // New data
        this.mRealEstates = newRealEstates

        // Notifies adapter
        diffResult.dispatchUpdatesTo(this@RMListAdapter)

        // Callback
        this.mCallback?.onDataChanged()
    }

    // NESTED CLASSES ------------------------------------------------------------------------------

    /**
     * A [RecyclerView.ViewHolder] subclass.
     */
    class RealEstateViewHolder(
        itemView: View,
        var mCallback: WeakReference<RMAdapterListener?>
    ) : RecyclerView.ViewHolder(itemView)
}








