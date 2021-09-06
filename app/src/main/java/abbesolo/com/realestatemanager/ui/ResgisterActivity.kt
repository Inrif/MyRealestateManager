package abbesolo.com.realestatemanager.ui

import abbesolo.com.realestatemanager.R
import abbesolo.com.realestatemanager.databinding.ActivityResgisterBinding
import abbesolo.com.realestatemanager.models.RMUser
import abbesolo.com.realestatemanager.repositories.*
import abbesolo.com.realestatemanager.utils.RMInputValidation
import abbesolo.com.realestatemanager.viewModel.RMViewModel
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_login2.*
import org.koin.androidx.viewmodel.ext.android.viewModel



class ResgisterActivity : AppCompatActivity() , View.OnClickListener {

    private lateinit var binding: ActivityResgisterBinding

    private val activity = this@ResgisterActivity
    private lateinit var nestedScrollView: NestedScrollView

    private lateinit var textInputLayoutName: TextInputLayout
    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputLayoutPassword: TextInputLayout
    private lateinit var textInputLayoutConfirmPassword: TextInputLayout

    private lateinit var textInputEditTextName: TextInputEditText
    private lateinit var textInputEditTextEmail: TextInputEditText
    private lateinit var textInputEditTextPassword: TextInputEditText
    private lateinit var textInputEditTextConfirmPassword: TextInputEditText

    private lateinit var appCompatButtonRegister: AppCompatButton


    private lateinit var inputValidation: RMInputValidation


//

    val mViewModel: RMViewModel by viewModel()


//


    companion object {

        const val EXTRA_USERNAME = "EXTRA_USERNAME"
        const val EXTRA_EMAIL= "EXTRA_EMAIL"
        const val EXTRA_PASSWORD = "EXTRA_PASSWORD"
        const val EXTRA_URLPICTURE = "EXTRA_URLPICTURE"

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResgisterBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // initializing the views
        initViews()

        // initializing the listeners
        initListeners()

        // initializing the objects
        initObjects()
    }

    private fun initViews() {
        nestedScrollView = binding.nestedScrollView
        textInputLayoutName = binding.textInputLayoutName
        textInputLayoutEmail = binding.textInputLayoutEmail
        textInputLayoutPassword = binding.textInputLayoutPassword
        textInputLayoutConfirmPassword = binding.textInputLayoutConfirmPassword
        textInputEditTextName = binding.textInputEditTextName
        textInputEditTextEmail = binding.textInputEditTextEmail
        textInputEditTextPassword = binding.textInputEditTextPassword
        textInputEditTextConfirmPassword = binding.textInputEditTextConfirmPassword
        appCompatButtonRegister = binding.appCompatButtonRegister


    }


    /**
     * This method is to initialize listeners
     */
    private fun initListeners() {
        appCompatButtonRegister.setOnClickListener(this)
//        appCompatTextViewLoginLink!!.setOnClickListener(this)

    }

    /**
     * This method is to initialize objects to be used
     */
    private fun initObjects() {
        inputValidation = RMInputValidation(activity)


    }

    /**
     * This implemented method is to listen the click on view
     *
     * @param v
     */
    override fun onClick(v: View) {
        when (v.id) {

            R.id.appCompatButtonRegister -> postDataToSQLite()

//            R.id.appCompatTextViewLoginLink -> startConnectionActivity()
        }
    }

    /**
     * This method is to validate the input text fields and post data to SQLite
     */
    private fun postDataToSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextName, textInputLayoutName, getString(R.string.error_message_name))) {
            return
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return
        }
        if (!inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, getString(R.string.error_message_password))) {
            return
        }
        if (!inputValidation.isInputEditTextMatches(textInputEditTextPassword, textInputEditTextConfirmPassword,
                textInputLayoutConfirmPassword, getString(R.string.error_password_match))) {
            return
        }



        val user = RMUser(username = textInputEditTextName.text.toString().trim(),
            email = textInputEditTextEmail.text.toString().trim(),
            password = textInputEditTextPassword.text.toString().trim(),
        urlPicture = null )
        val name = user.username
        val email = user.email
        val password = user.password
        val urlPicture = user.urlPicture

        mViewModel.insertUser(user)

        UserSingleton.mUser = user

        println(name)




//        setResult(RESULT_OK, data)
//        finish()

        // Snack Bar to show success message that record saved successfully
        Snackbar.make(nestedScrollView, getString(R.string.success_message), Snackbar.LENGTH_LONG).show()
        emptyInputEditText()

        Toast.makeText(
            applicationContext,
            " Welcome $name",
            Toast.LENGTH_LONG
        ).show()





        val data = Intent(activity, MainActivity::class.java)
        data.putExtra(EXTRA_USERNAME, name)
        data.putExtra(EXTRA_EMAIL, email)
        data.putExtra(EXTRA_PASSWORD, password)
        data.putExtra(EXTRA_URLPICTURE, urlPicture)

        val id: Long = intent.getIntExtra(NfcAdapter.EXTRA_ID, -1).toLong()
        if (!(-1).equals(id)) {
            data.putExtra(NfcAdapter.EXTRA_ID, id)
        }

//        UserRepository.getInstance().setmUser


        UserSingleton.getUser()?.username = name
        UserSingleton.getUser()?.email = email
        UserSingleton.getUser()?.password = password
        UserSingleton.getUser()?.urlPicture = urlPicture




        startActivity(data)





    }

    /**
     * This method is to start connectionActivity all input edit text
     */
//    private fun startConnectionActivity() {
//
//        val i = Intent(activity, ConnectionActivity::class.java)
//        startActivity(i)
//
//    }

    /**
     * This method is to empty all input edit text
     */
    private fun emptyInputEditText() {
        textInputEditTextName.text = null
        textInputEditTextEmail.text = null
        textInputEditTextPassword.text = null
        textInputEditTextConfirmPassword.text = null
    }

}