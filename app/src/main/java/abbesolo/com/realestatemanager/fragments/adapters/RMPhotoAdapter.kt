package abbesolo.com.realestatemanager.fragments.adapters

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.models.Photo
import abbesolo.com.realestatemanager.utils.RMPhotoDiffUtilCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_list_rm.view.*
import kotlinx.android.synthetic.main.item_photo.view.*
import java.lang.ref.WeakReference

/**
 * Created by HOUNSA Romuald on 18/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 */
class RMPhotoAdapter (private val mCallback: RMAdapterListener? = null,
                      private val mButtonDisplayMode: ButtonDisplayMode = ButtonDisplayMode.NORMAL_MODE): RecyclerView.Adapter<RMPhotoAdapter.RMPhotoViewHolder>() {


    // ENUMS ---------------------------------------------------------------------------------------

    enum class ButtonDisplayMode {NORMAL_MODE, EDIT_MODE}

    // FIELDS --------------------------------------------------------------------------------------

    private val mPhotos = mutableListOf<Photo>()

    // METHODS -------------------------------------------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RMPhotoViewHolder {
        // Creates the View thanks to the inflater
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)

        return RMPhotoViewHolder(view, WeakReference(this.mCallback))
    }

    override fun onBindViewHolder(holder: RMPhotoViewHolder, position: Int) {
        this.configureDesign(holder, this.mPhotos[position])
    }

    override fun getItemCount(): Int = this.mPhotos.size

    // -- Design item --

    /**
     * Configures the design of each item
     * @param holder    a [RMPhotoViewHolder] that corresponds to the item
     * @param photo     a [Photo]
     */
    private fun configureDesign(holder: RMPhotoViewHolder, photo: Photo) {
        // Image
        photo.urlPicture?.let {
            Glide.with(holder.itemView)
                .load(it)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .fallback(R.drawable.ic_photo)
                .error(R.drawable.ic_clear)
                .into(holder.itemView.item_photo_image)
        }

        // Description
        photo.description?.let { holder.itemView.item_photo_description.text = it }

        // Buttons: Visibility
        val visibility = when (this.mButtonDisplayMode) {
            ButtonDisplayMode.NORMAL_MODE -> View.GONE
            ButtonDisplayMode.EDIT_MODE -> View.VISIBLE
        }

        holder.itemView.item_photo_delete_media.visibility = visibility
        holder.itemView.item_photo_edit_media.visibility = visibility

        // Here the listeners are not useful
        if (visibility == View.GONE) {
            return
        }

        // Button DELETE: add Listener
        holder.itemView.item_photo_delete_media.setOnClickListener {
            // Tag -> photo
            it.tag = photo

            // Starts the callback
            holder.mCallback.get()?.onClick(it)
        }

        // Button EDIT: add Listener
        holder.itemView.item_photo_edit_media.setOnClickListener {
            // Tag -> photo
            it.tag = photo

            // Starts the callback
            holder.mCallback.get()?.onClick(it)
        }
    }

    // -- Photo --

    /**
     * Updates data of [RMPhotoAdapter]
     * @param newPhotos a [List] of [Photo]
     */
    fun updateData(newPhotos: List<Photo>) {
        // Optimizes the performances of RecyclerView
        val diffCallback  = RMPhotoDiffUtilCallback(this.mPhotos, newPhotos)
        val diffResult  = DiffUtil.calculateDiff(diffCallback )

        // New data
        this.mPhotos.clear()
        this.mPhotos.addAll(newPhotos)

        // Notifies adapter
        diffResult.dispatchUpdatesTo(this@RMPhotoAdapter)

        // Callback
        this.mCallback?.onDataChanged()
    }

    // NESTED CLASSES ------------------------------------------------------------------------------

    /**
     * A [RecyclerView.ViewHolder] subclass.
     */
    class RMPhotoViewHolder(
        itemView: View,
        var mCallback: WeakReference<RMAdapterListener?>
    ): RecyclerView.ViewHolder(itemView)
}
