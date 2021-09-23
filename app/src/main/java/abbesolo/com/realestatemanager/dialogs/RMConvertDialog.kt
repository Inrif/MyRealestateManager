package abbesolo.com.realestatemanager.dialogs

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.utils.RMSaveTools.convertDollarToEuro
import abbesolo.com.realestatemanager.utils.RMSaveTools.convertEuroToDollar
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.dialog_convert.view.*
/**
 * Created by HOUNSA Romuald on 09/08/21.
 */
class RMConvertDialog : DialogFragment(){
    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var mRootView: View
    private lateinit var mNumber: TextInputLayout
    private lateinit var mResult: AppCompatTextView
    private lateinit var mButtonDollar: AppCompatButton
//    private lateinit var mButtonEuro: AppCompatButton



    companion object {
        private const val BUNDLE_NUMBER= "BUNDLE_NUMBER"
        private const val BUNDLE_RESULT= "BUNDLE_RESULT"

        fun newInstance(

            numberToConvert: Int? = null,
            resultAfterConversion: Int? = null
        ): RMConvertDialog {
            val dialog = RMConvertDialog()

            // Bundle into Argument of Fragment
            dialog.arguments = Bundle().apply {
                if (numberToConvert != null) {
                    putInt(BUNDLE_NUMBER, numberToConvert)
                }
                if (resultAfterConversion != null) {
                    putInt(BUNDLE_RESULT, resultAfterConversion)
                }

            }

            return dialog
        }
    }
    // METHODS -------------------------------------------------------------------------------------

    // -- DialogFragment --

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Creates the View thanks to the inflater
        this.mRootView = this.requireActivity().layoutInflater
            .inflate(R.layout.dialog_convert, null)



        this.initViews()

        this.configureButtons()


        return MaterialAlertDialogBuilder(this.requireContext()).setView(this.mRootView)
            .setTitle(R.string.menu_drawer_convert)
            .create()
    }


    // -- InitViews --

    /**
     * InitViews
     */

    private fun initViews(){

        mNumber = this.mRootView.textInputLayoutName
        mResult = this.mRootView.appCompatTextViewValue
        mButtonDollar = this.mRootView.appCompatButtonDollar
//        mButtonEuro = this.mRootView.appCompatButtonEuros

        }



        // -- Button --

    /**
     * Configures the buttons
     */
    private fun configureButtons() {
        // Button: Dollar
        this.mButtonDollar.setOnClickListener {
            this.actionOfDollarButton()
        }

//        // Button: Euros
//        this.mButtonEuro.setOnClickListener {
//            // Close Dialog
//            this.actionOfEuroButton()
//        }
    }



    /**
     * Action for the Dollar Euro button
     */
    private fun actionOfDollarButton()
    {
        val str = this.mNumber.editText?.text.toString()
        val  myNumber : Int = str.toInt()
        println(myNumber)

        val convertDollarToEuro = convertDollarToEuro(myNumber)
        mResult.text = convertDollarToEuro.toString()


        println(convertDollarToEuro)


    }



    /**
     * Action for the Euro button
     */
    private fun actionOfEuroButton()
    {

            val s = this.mNumber.editText?.text.toString()
            val myNumber: Int = s.toInt()
        val convertDollarToEuro = convertDollarToEuro(myNumber)
        mResult.text = convertDollarToEuro.toString()

        val convertEuroToDollar = convertEuroToDollar(convertDollarToEuro)
        mResult.text = convertEuroToDollar.toString()


    }





}