@file:Suppress("UNREACHABLE_CODE")

package abbesolo.com.realestatemanager.fragments

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.fragments.adapters.RMAdapterListener
import abbesolo.com.realestatemanager.fragments.adapters.RMListAdapter
import abbesolo.com.realestatemanager.models.RMAndPhotos
import abbesolo.com.realestatemanager.viewModel.RMViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.view.*

import kotlinx.android.synthetic.main.fragment_r_m_list.view.*


/**
 * A simple [Fragment] subclass.
 * Use the [RMListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RMListFragment :  RMBaseFragment(), RMAdapterListener {

    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var mAdapter: RMListAdapter

    // METHODS -------------------------------------------------------------------------------------

    // -- BaseFragment --

    @LayoutRes
    override fun getFragmentLayout(): Int = R.layout.fragment_list

    override fun configureDesign(savedInstanceState: Bundle?) {
        // UI
        this.configureRecyclerView()

        // LiveData
        this.configureRealEstateLiveData()
    }

    // -- AdapterListener interface --

    override fun onDataChanged() {
        this.mRootView.fragment_list_no_data.visibility =
            if (this.mAdapter.itemCount == 0)
                View.VISIBLE
            else
                View.GONE
    }

    override fun onClick(v: View?) {
        // Callback from Fragment to Activity
        this.mCallback?.navigateToDetailsFragment(v)
    }

    // -- RecyclerView --

    /**
     * Configures the RecyclerView
     */
    private fun configureRecyclerView() {
        // Adapter
        this.mAdapter = RMListAdapter(
            mCallback = this@RMListFragment
        )

        // LayoutManager
        val viewManager = LinearLayoutManager(
            this.requireContext()
        )

        // Divider
        val divider = DividerItemDecoration(
            this.requireContext(),
            DividerItemDecoration.VERTICAL
        )

        // RecyclerView
        with(this.mRootView.fragment_list_RecyclerView) {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(divider)
            adapter = this@RMListFragment.mAdapter
        }
    }

    // -- LiveData --

    /**
     * Configures the LiveData thanks to a simple format
     */
    private fun configureRealEstateLiveData() {

        this.mViewModel
            .getRealEstatesWithPhotosByUserId(userId = 1L)
            .observe(
                this.viewLifecycleOwner,
                Observer { this.mAdapter.updateData(it) }
            )
    }
}