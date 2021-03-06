package abbesolo.com.realestatemanager.ui

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.fragments.*
import abbesolo.com.realestatemanager.repositories.UserSingleton
import abbesolo.com.realestatemanager.utils.RMMessageTools
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : RMBaseActivity(), FragmentListener {

    // ENUMS ---------------------------------------------------------------------------------------

    private enum class Mode {PHONE_MODE, TABLET_MODE}
    private lateinit var mMode: Mode

    // FIELDS --------------------------------------------------------------------------------------

    private val mNavController by lazy {
        this.findNavController(R.id.activity_main_NavHostFragment)
    }

    private var mItemId: Long = 0L

    companion object {
        const val BUNDLE_ITEM_ID = "BUNDLE_ITEM_ID"


    }

    // METHODS -------------------------------------------------------------------------------------

    // -- BaseActivity --

    @LayoutRes
    override fun getActivityLayout(): Int = R.layout.activity_main

    override fun getToolBar(): Toolbar? = this.activity_main_Toolbar

    override fun configureDesign() {
        // UI
        this.configureToolbar()

        // Mode
        this.checkMode()

        // Navigation
        this.configureFragmentNavigation()

    }

    // -- Activity --

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.toolbar_menu, menu)
        this.configureBehaviorOfToolBar()


        val myUser = UserSingleton.getUser()
        println(myUser)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Edit fragment (Need to pass data to start fragment)
        item.itemId.let {
            if (it == R.id.toolbar_menu_edit) {
                // By destination (Safe Args)
                val bundle = RMUpdateFragmentArgs(this.mItemId).toBundle()
                this.mNavController.navigate(R.id.navigation_editFragment, bundle)

                return true
            }
        }

        return item.onNavDestinationSelected(this.mNavController) || super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (this.activity_main_drawer_layout.isDrawerOpen(GravityCompat.START)) {
            this.activity_main_drawer_layout.closeDrawer(GravityCompat.START)
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Call the onActivityResult method of current Fragment
        this.supportFragmentManager
            .findFragmentById(R.id.activity_main_NavHostFragment)!!
            .childFragmentManager
            .fragments[0]
            .onActivityResult(requestCode, resultCode, data)

    }


    override fun onSaveInstanceState(outState: Bundle) {
        // Save item id for EditFragment
        outState.putLong(BUNDLE_ITEM_ID, this.mItemId)

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState)

        // Restores item id for EditFragment
        savedInstanceState?.let {
            this.mItemId = it.getLong(BUNDLE_ITEM_ID, 0L)
        }
    }


    // -- FragmentListener interface --

    override fun showMessage(message: String) {
        RMMessageTools.showMessageWithSnackbar(
            this.activity_main_CoordinatorLayout,
            message
        )
    }

    override fun navigateToDetailsFragment(v: View?) {
        // Tag
        this.mItemId = v?.tag as Long

        // According to the device type
        when (this.mMode) {
            Mode.PHONE_MODE -> {
                // By action (Safe Args)
                val action = RMListFragmentDirections.actionListFragmentToDetailsFragment(this.mItemId)
                this.mNavController.navigate(action)
            }

            Mode.TABLET_MODE -> {
                // By destination (Safe Args)
                val bundle = RMDetailFragmentArgs(this.mItemId).toBundle()
                this.mNavController.navigate(R.id.navigation_detailsFragment, bundle)
            }
        }
    }

    // -- Mode --

    /**
     * Checks the device type: phone or tablet
     */
    private fun checkMode() {
        // Checks the mode
        this.mMode = if (this.findViewById<View>(R.id.activity_main_fragment) != null)
            Mode.TABLET_MODE
        else
            Mode.PHONE_MODE
    }

    // -- Navigation --

    /**
     * Configures the fragment navigation
     */
    private fun configureFragmentNavigation() {
        // AppBarConfiguration
        val appBarConfiguration = AppBarConfiguration(this.mNavController.graph,
            this.activity_main_drawer_layout)

        // Toolbar
        this.activity_main_Toolbar.setupWithNavController(this.mNavController,
            appBarConfiguration)

        // NavigationView
        this.activity_main_navigation_view.setupWithNavController(this.mNavController)
    }

    // -- Behaviors of Toolbar --

    /**
     * Configures the behavior of the [Toolbar]
     */
    private fun configureBehaviorOfToolBar() {
        this.mNavController.addOnDestinationChangedListener { _, destination, _ ->
            when (this.mMode) {
                Mode.PHONE_MODE -> this.getBehaviorOfToolbarForPhoneMode(destination)
                Mode.TABLET_MODE -> this.getBehaviorOfToolbarForTabletMode(destination)
            }
        }
    }

    /**
     * Gets the behavior of the [Toolbar] for [Mode.PHONE_MODE]
     */
    private fun getBehaviorOfToolbarForPhoneMode(destination: NavDestination) {
        val addItem = this.getToolBar()!!
            .menu!!
            .findItem(R.id.navigation_creatorFragment)

        val editItem = this.getToolBar()!!
            .menu!!
            .findItem(R.id.toolbar_menu_edit)

        val searchItem = this.getToolBar()!!
            .menu!!
            .findItem(R.id.navigation_locationFragment)

        when (destination.id) {
            R.id.navigation_listFragment -> {
                addItem.isVisible = true
                editItem.isVisible = false
                searchItem.isVisible = true
            }

            R.id.navigation_detailsFragment -> {
                addItem.isVisible = false
                editItem.isVisible = true
                searchItem.isVisible = false
            }

            R.id.navigation_locationFragment,
            R.id.navigation_searchFragment -> {
                addItem.isVisible = false
                editItem.isVisible = false
                searchItem.isVisible = false
            }

            R.id.navigation_creatorFragment,
            R.id.navigation_editFragment -> {
                addItem.isVisible = false
                editItem.isVisible = false
                searchItem.isVisible = false
            }

            else -> { /* Ignore all other ids */ }
        }
    }

    /**
     * Gets the behavior of the [Toolbar] for [Mode.TABLET_MODE]
     */
    private fun getBehaviorOfToolbarForTabletMode(destination: NavDestination) {
        val editItem = this.getToolBar()!!
            .menu!!
            .findItem(R.id.toolbar_menu_edit)

        val searchItem = this.getToolBar()!!
            .menu!!
            .findItem(R.id.navigation_locationFragment)

        when (destination.id) {
            R.id.navigation_detailsFragment -> {
                editItem.isVisible = true
                searchItem.isVisible = false

            }

            R.id.navigation_locationFragment,
            R.id.navigation_searchFragment -> {
                editItem.isVisible = false
                searchItem.isVisible = false
            }

            R.id.navigation_creatorFragment,
            R.id.navigation_editFragment -> {
                editItem.isVisible = false
                searchItem.isVisible = true
            }

            else -> { /* Ignore all other ids */ }
        }
    }
}