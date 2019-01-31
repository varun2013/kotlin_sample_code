package com.sample.sampleloginsignup

import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Login.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Login.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUp : Fragment() {
    internal var savedInstanceState: Bundle? = null

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    internal var sign_up_main: Button
    internal var signup: TextView? = null
    internal var signin: TextView
    internal var context: LoginActivity
    internal var fragment: Fragment? = null

    internal var email_address: EditText
    internal var user_password: EditText
    internal var dataSnapshot: DataSnapshot? = null
    internal var databaseArtists: DatabaseReference

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseArtists = FirebaseDatabase.getInstance().getReference("userdetail")
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }

        Login.pagestatus = 1

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.savedInstanceState = savedInstanceState


        // Inflate the layout for this fragment

        val v = inflater.inflate(R.layout.fragment_sign__up1, container, false)
        signin = v.findViewById(R.id.signin)
        sign_up_main = v.findViewById(R.id.sign_up_main)
        email_address = v.findViewById(R.id.email_address)
        user_password = v.findViewById(R.id.user_password)
        // signup=(TextView)v.findViewById(R.id.signup);


        user_password.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val isvalidEmail = emailValidator(email_address.text.toString())

                if (isvalidEmail != true) {

                    email_address.error = "invalid email"
                }


            }
        }

        signin.setOnClickListener {
            fragment = Login()

            val manager = fragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.mainFrame, fragment)
            transaction.commit()
        }

        sign_up_main.setOnClickListener {
            fragment = AfterSignUp()
            var email = email_address.text.toString().trim { it <= ' ' }
            var password = user_password.text.toString().trim { it <= ' ' }
            val isvalidEmail = emailValidator(email)

            if (isvalidEmail == true) {
                if (dataSnapshot != null) {
                    for (data in dataSnapshot!!.children) {

                        val userName = data.child("userEmail").value.toString()
                        if (userName.equals(email, ignoreCase = true)) {
                            //Toast.makeText(getActivity(), "Already have account::", Toast.LENGTH_SHORT).show();
                            email = ""
                            password = ""
                            email_address.error = "Already have account"

                        }

                    }

                }
                if (!(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {

                    //getting a unique id using push().getKey() method
                    //it will create a unique id and we will use it as the Primary Key for our Artist
                    if (password.length < 8) {

                        user_password.error = "should be 8 digit"

                        //Toast.makeText(getActivity(),"password should be at least 8 digit",Toast.LENGTH_SHORT).show();
                    } else {
                        if (savedInstanceState == null) {
                            val bundle = Bundle()
                            bundle.putCharSequence("email", email)
                            bundle.putCharSequence("password", password)
                            fragment!!.arguments = bundle
                            val manager = fragmentManager
                            val transaction = manager.beginTransaction()
                            transaction.replace(R.id.mainFrame, fragment)
                            transaction.commit()
                        }
                    }
                } else {
                    //if the value is not given displaying a toast
                    Toast.makeText(activity, "Please enter unique email......", Toast.LENGTH_LONG).show()
                }


            } else {

                Toast.makeText(activity, "Please Provide valid email id......", Toast.LENGTH_LONG).show()
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
    /////Validate email method


    fun emailValidator(email: String): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern.matcher(email)
        return matcher.matches()
    }


    override fun onStart() {
        super.onStart()
        //attaching value event listener
        databaseArtists.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                this@SignUp.dataSnapshot = dataSnapshot

                //              getActivity().this.dataSnapshot=dataSnapshot;
                //
                //                //clearing the previous artist list
                //                artists.clear();
                //
                //                //iterating through all the nodes
                //                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                //                    //getting artist
                //                    Artist artist = postSnapshot.getValue(Artist.class);
                //                    //adding artist to the list
                //                    artists.add(artist);
                //                }
                //
                //                //creating adapter
                //                ArtistList artistAdapter = new ArtistList(MainActivity.this, artists);
                //                //attaching adapter to the listview
                //                listViewArtists.setAdapter(artistAdapter);
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
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
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
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
