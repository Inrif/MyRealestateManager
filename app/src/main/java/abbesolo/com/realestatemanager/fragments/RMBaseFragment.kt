package abbesolo.com.realestatemanager.fragments

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.viewModel.RMViewModel
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import org.koin.androidx.viewmodel.ext.android.viewModel


abstract class RMBaseFragment : Fragment()
{

    // ENUMS ---------------------------------------------------------------------------------------

    enum class Media {PHOTO, VIDEO}

    // FIELDS --------------------------------------------------------------------------------------

    protected lateinit var mRootView: View
    protected var mCallback: FragmentListener? = null

    protected val mViewModel: RMViewModel by viewModel()

    companion object {
        const val REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE_PHOTO = 1000
        const val REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE_VIDEO = 2000
        const val REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION = 3000
        const val REQUEST_CODE_CHECK_SETTINGS_TO_LOCATION = 4000
    }

    // METHODS -------------------------------------------------------------------------------------

    /**
     * Gets the integer value of the fragment layout
     * @return an integer that corresponds to the fragment layout
     */
    @LayoutRes
    protected abstract fun getFragmentLayout(): Int

    /**
     * Configures the design of each daughter class
     * @param savedInstanceState a [Bundle] to check the configuration changes of [Fragment]
     */
    protected abstract fun configureDesign(savedInstanceState: Bundle?)

    // -- Fragment --

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Configures the callback to the parent activity
        if (context is FragmentListener) {
            this.mCallback = context
        }
        else {
            throw ClassCastException("$context must implement FragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.mRootView = inflater.inflate(this.getFragmentLayout(), container, false)

        this.configureDesign(savedInstanceState)

        return this.mRootView
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        super. registerForActivityResult(requestCode, permissions, grantResults)

        when (requestCode) {
            // Access to the external storage for fetch a photo
            REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE_PHOTO -> {
                this.handlePermissionsResult(grantResults, Media.PHOTO)
            }

            // Access to the external storage for fetch a video
            REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE_VIDEO -> {
                this.handlePermissionsResult(grantResults, Media.VIDEO)
            }

            // Access to the current location of device
            REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION -> {
                this.handlePermissionsResult(grantResults)
            }

            else -> { /* Ignore all other requests */}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check settings to location
        if (requestCode == REQUEST_CODE_CHECK_SETTINGS_TO_LOCATION && resultCode == Activity.RESULT_OK) {
            this.actionAfterPermission()
        }
    }

    // -- Permission --

    /**
     * Checks the permission: READ_EXTERNAL_STORAGE to fetch photo
     * @param media a [Media]
     */
    protected fun checkReadExternalStoragePermission(
        media: Media
    ): Boolean {
        val permissionResult = ContextCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        return when (permissionResult) {
            PackageManager.PERMISSION_GRANTED -> true

            else -> {
                this.requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    when (media) {
                        Media.PHOTO -> REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE_PHOTO
                        Media.VIDEO -> REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE_VIDEO
                    }
                )

                false
            }
        }
    }

    /**
     * Checks the permission: ACCESS_FINE_LOCATION
     */
    private fun checkAccessFineLocationPermission(): Boolean {
        val permissionResult = ContextCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        return when (permissionResult) {
            PackageManager.PERMISSION_GRANTED -> true

            else -> {
                this.requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION
                )

                false
            }
        }
    }

    /**
     * Method to override to perform action after the granted permission
     * @param media a [Media]
     */
    protected open fun actionAfterPermission(media: Media? = null) {/* Do nothing here */}

    /**
     * Handles the permissions result
     * @param grantResults  a [IntArray]
     * @param media         a [Media]
     */
    private fun handlePermissionsResult(grantResults: IntArray, media: Media? = null) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.actionAfterPermission(media)
        }
        else {
            this.mCallback?.showMessage(this.getString(R.string.permission_denied))
        }
    }

    // -- Exceptions --

    /**
     * Handles the location [Exception]
     * @param exception an [Exception]
     * @return a boolean that is true if there is an [Exception]
     */
    protected fun handleLocationException(exception: Exception?): Boolean {
        // No exception
        if (exception == null) {
            return false
        }

        when (exception) {
            is SecurityException -> this.checkAccessFineLocationPermission()

            is ResolvableApiException -> {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this.requireActivity(),
                        REQUEST_CODE_CHECK_SETTINGS_TO_LOCATION
                    )
                }
                catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

        return true
    }
}