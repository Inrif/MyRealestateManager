package abbesolo.com.realestatemanager.ui

import abbesolo.com.realestatemanager.viewModel.RMViewModel
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Romuald Hounsa on 20/07/21.
 */
abstract class RMBaseActivity : AppCompatActivity() {

    // FIELDS --------------------------------------------------------------------------------------

    protected val mViewModel: RMViewModel by viewModel()

    // METHODS -------------------------------------------------------------------------------------

    /**
     * Gets the integer value of the activity layout
     * @return an integer that corresponds to the activity layout
     */
    @LayoutRes
    protected abstract fun getActivityLayout(): Int

    /**
     * Get the [Toolbar]
     * @return a [Toolbar]
     */
    protected abstract fun getToolBar(): Toolbar?

    /**
     * Configures the design of each daughter class
     */
    protected abstract fun configureDesign()

    // -- Activity --

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(this.getActivityLayout())
        this.configureDesign()
    }

    // -- Toolbar --

    /**
     * Configures the [Toolbar]
     */
    protected fun configureToolbar() {
        // No Toolbar
        if (this.getToolBar() == null) {
            return
        }

        this.setSupportActionBar(this.getToolBar())
    }
}