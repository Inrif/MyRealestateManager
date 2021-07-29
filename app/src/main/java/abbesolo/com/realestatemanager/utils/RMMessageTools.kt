package abbesolo.com.realestatemanager.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

/**
 * Created by HOUNSA Romuald on 20/07/21.
 */
object RMMessageTools {
    fun showMessageWithSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .show()
    }

}