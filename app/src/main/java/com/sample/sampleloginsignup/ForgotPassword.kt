package com.sample.sampleloginsignup

import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

import java.util.Random


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ForgotPassword.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ForgotPassword.newInstance] factory method to
 * create an instance of this fragment.
 */
class ForgotPassword : Fragment() {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    internal var sendvcode: Button
    private var auth: FirebaseAuth? = null
    // TODO: Rename and change types of parameters
    private val mParam1: String? = null
    private val mParam2: String? = null
    internal var fragment: Fragment? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var spinner: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle): View? {
        // Inflate the layout for this fragment

        val v = inflater.inflate(R.layout.fragment_forgot_password, container, false)
        spinner = v.findViewById(R.id.progressBar)
        sendvcode = v.findViewById(R.id.sendvcode)
        val editText = v.findViewById(R.id.quesmark) as EditText
        val click = v.findViewById(R.id.click) as TextView
        click.setOnClickListener {
            fragment = SignUp()
            val manager = fragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.mainFrame, fragment)
            transaction.commit()
        }
        auth = FirebaseAuth.getInstance()
        sendvcode.setOnClickListener {
            if (!editText.text.toString().isEmpty()) {

                spinner!!.visibility = View.VISIBLE
                auth!!.sendPasswordResetEmail(editText.text.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(activity, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show()
                                fragment = Login()
                                val manager = fragmentManager
                                val transaction = manager.beginTransaction()
                                transaction.replace(R.id.mainFrame, fragment)
                                transaction.commit()
                                spinner!!.visibility = View.GONE

                            } else {
                                Toast.makeText(activity, "Failed to send reset email!", Toast.LENGTH_SHORT).show()
                                spinner!!.visibility = View.GONE
                            }
                        }
            } else
                Toast.makeText(activity, "enter email", Toast.LENGTH_SHORT).show()
        }
        return v
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ForgotPassword.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): ForgotPassword {
            val fragment = ForgotPassword()
            val args = Bundle()

            fragment.arguments = args
            return fragment
        }

        //OTP generation method
        internal fun OTP(len: Int): CharArray {
            println("Generating OTP using random() : ")


            // Using numeric values
            val numbers = "0123456789"

            // Using random method
            val rndm_method = Random()

            val otp = CharArray(4)

            for (i in 0..3) {
                // Use of charAt() method : to get character value
                // Use of nextInt() as it is scanning the value as int
                otp[i] = numbers[rndm_method.nextInt(numbers.length)]
            }
            print("You OTP is : " + otp.toString())
            return otp
        }

        fun getDefaults(key: String, context: Context): String {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString(key, "No detail")
        }

        fun setDefaults(key: String, value: String, context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            editor.putString(key, value)

            editor.commit()
        }
    }

}// Required empty public constructor
