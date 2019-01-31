package com.sample.sampleloginsignup

import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.CardView
import android.text.TextUtils
import android.util.Log
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
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.talentelgia.littlepicasso.UserData.AddInvitation
import com.talentelgia.littlepicasso.UserData.UserDetail

import java.text.SimpleDateFormat
import java.util.Date

import android.content.Context.MODE_PRIVATE


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Login.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Login.newInstance] factory method to
 * create an instance of this fragment.
 */
class AfterSignUp : Fragment() {
    internal var savedInstanceState: Bundle

    private var mParam1: String? = null
    private var first_name: String? = null
    private var last_name: String? = null
    private var mParam2: String? = null
    internal var sign_up_detail: Button
    internal var email: String
    internal var password: String
    internal var signup: TextView? = null
    internal var context: LoginActivity
    internal var fragment: Fragment? = null
    private var auth: FirebaseAuth? = null
    internal var dataSnapshot: DataSnapshot
    private var spinner: ProgressBar? = null
    //a list to store all the artist from firebase database
    internal var artists: List<UserDetail>? = null

    //our database reference object
    internal var databaseArtists: DatabaseReference

    internal var user_firstname: EditText
    internal var user_lastname: EditText

    private var mListener: OnFragmentInteractionListener? = null
    fun addUserId(key: String, context: Context) {
        try {
            val editor = context.getSharedPreferences("authentication", MODE_PRIVATE).edit()
            editor.putString("userid", key)
            editor.commit()
        } catch (ex: Exception) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as LoginActivity
        databaseArtists = FirebaseDatabase.getInstance().getReference("Users")
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
        Login.pagestatus = 2
    }

    fun userEmailAcc(key: String) {
        try {
            val editor = context.getSharedPreferences("emailacc", MODE_PRIVATE).edit()
            editor.putString("email", key)
            editor.commit()
        } catch (ex: Exception) {

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle): View? {

        this.savedInstanceState = savedInstanceState
        val bundle = arguments

        email = bundle.getString("email", "no value")
        password = bundle.getString("password", "no password")
        auth = FirebaseAuth.getInstance()

        // Inflate the layout for this fragment

        val v = inflater.inflate(R.layout.fragment_sign__up2, container, false)
        spinner = v.findViewById(R.id.progressBar)

        val imageView1 = v.findViewById(R.id.back) as CardView
        imageView1.setOnClickListener(View.OnClickListener {
            val signUp = SignUp()
            val manager = fragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.mainFrame, signUp)
            transaction.commit()
        })

        sign_up_detail = v.findViewById(R.id.sign_up_detail)
        user_firstname = v.findViewById(R.id.user_firstname)
        user_lastname = v.findViewById(R.id.user_lastname)

        sign_up_detail.setOnClickListener {
            spinner!!.visibility = View.VISIBLE
            first_name = user_firstname.text.toString().trim { it <= ' ' }
            last_name = user_lastname.text.toString().trim { it <= ' ' }


            auth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity) { task ->
                        try {
                            addUserId(task.result.user.uid, activity)
                            userEmailAcc(task.result.user.uid)
                        } catch (ex: Exception) {
                            spinner!!.visibility = View.GONE
                        }

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful) {
                            Toast.makeText(activity, "Already have an account",
                                    Toast.LENGTH_SHORT).show()
                            spinner!!.visibility = View.GONE
                        } else {
                            addEmail(email)
                            addUser(task.result.user.uid)

                            val addChild = AddChild()
                            val manager = fragmentManager
                            val transaction = manager.beginTransaction()
                            transaction.add(R.id.mainFrame, addChild)
                            transaction.commit()

                            spinner!!.visibility = View.GONE
                        }
                    }
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

        this.context = context as LoginActivity


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


    ///Add user profile

    override fun onStart() {
        super.onStart()
        //attaching value event listener
        databaseArtists.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                this@AfterSignUp.dataSnapshot = dataSnapshot
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun addEmail(email: String) {
        val editor = activity.getSharedPreferences("useremail", MODE_PRIVATE).edit()
        editor.putString("email", email)
    }

    private fun addUser(str: String) {

        if (!TextUtils.isEmpty(first_name)) {

            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our Artist
            val id = databaseArtists.push().key
            val editor1 = activity.getSharedPreferences("userdetail", MODE_PRIVATE).edit()
            editor1.putString("firstname", first_name)
            editor1.putString("lastname", last_name)
            editor1.putString("userid", str)
            editor1.commit()

            val artist1 = UserDetail(first_name, last_name, str, email, SimpleDateFormat("dd:MMM:yyyy").format(Date()), "user", "android")

            //Saving the Artist
            databaseArtists.child(str).setValue(artist1)

            //setting edittext to blank again
            user_firstname.setText("")

            val dR1 = FirebaseDatabase.getInstance().getReference("InvitedList")
            val child_id = dR1.push().key

            val editor2 = activity.getSharedPreferences("userdetail", MODE_PRIVATE)
            val addInvitation = AddInvitation()
            addInvitation.setFirstName(first_name)
            addInvitation.setLastName(last_name)
            addInvitation.setInvitedUserEmail(email)
            addInvitation.setFromUserId(str)
            addInvitation.setToUserId(str)
            addInvitation.setFromFname(first_name)
            addInvitation.setFromLname(last_name)
            addInvitation.setInvitedStatus("")
            addInvitation.setPermissionType("ReadandWrite")

            dR1.child(child_id).setValue(addInvitation)
        } else {
            Log.d("Invalid credential", "Please eneter unique username")
        }
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Login.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): Login {
            val fragment = Login()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
