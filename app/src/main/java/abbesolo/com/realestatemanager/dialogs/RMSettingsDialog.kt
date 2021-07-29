package abbesolo.com.realestatemanager.dialogs

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.utils.RMSaveTools
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.android.synthetic.main.dialog_settings.view.*

/**
 * Created by HOUNSA Romuald on 19/06/2020.
 * Copyright (c) 2020 abbesolo.com.realestatemanager. All rights reserved.
 *
 * A [DialogFragment] subclass.
 */
class RMSettingsDialog : DialogFragment() {

    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var mRootView: View

    companion object {
        const val BUNDLE_SWITCH_NOTIFICATION = "BUNDLE_SWITCH_NOTIFICATION"
    }

    // METHODS -------------------------------------------------------------------------------------

    // -- DialogFragment --

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Creates the View thanks to the inflater
        this.mRootView = this.requireActivity().layoutInflater
                                               .inflate(R.layout.dialog_settings, null)

        this.fetchDataFromSharedPreferences()
        this.configureSwitch()

        return MaterialAlertDialogBuilder(this.requireContext()).setView(this.mRootView)
                                                                .setTitle(R.string.title_settings_dialog)
                                                                .create()
    }

    // -- SharedPreferences --

    /**
     * Fetches data from the SharedPreferences
     */
    private fun fetchDataFromSharedPreferences() {
        this.mRootView.dialog_fragment_settings_switch.isChecked =
            RMSaveTools.fetchBooleanFromSharedPreferences(
                this.requireContext(),
                BUNDLE_SWITCH_NOTIFICATION
            )
    }

    // -- Switch --

    /**
     * Configures the [SwitchMaterial]
     */
    private fun configureSwitch() {
        this.mRootView.dialog_fragment_settings_switch.setOnClickListener {
            if (it is SwitchMaterial) {
                RMSaveTools.saveBooleanIntoSharedPreferences(
                    this.requireContext(),
                    BUNDLE_SWITCH_NOTIFICATION,
                    it.isChecked
                )
            }
        }
    }
}