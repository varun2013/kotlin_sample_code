package com.sample.sampleloginsignup

import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import com.antonyt.infiniteviewpager.CommonInterface
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.Profile
import com.facebook.ProfileTracker
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.facebook.share.ShareApi
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.talentelgia.littlepicasso.UserData.UserDetail

import org.json.JSONException
import org.json.JSONObject

import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Date
import java.util.regex.Matcher
import java.util.regex.Pattern

import android.content.Context.MODE_PRIVATE


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Login.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Login.newInstance] factory method to
 * create an instance of this fragment.
 */
class Login : Fragment(), GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    internal var savedInstanceState: Bundle? = null
    internal var facebookemail = ""
    private var spinner: ProgressBar? = null
    private var mDatabase: DatabaseReference? = null
    private var invitedref: DatabaseReference? = null
    internal var firebaseStorage = FirebaseStorage.getInstance()

    internal var storageRef = firebaseStorage.getReference()
    internal var dataSnapshot: DataSnapshot

    val defaults: String
        get() {
            try {
                run { }
                val editor = activity.getSharedPreferences("authentication", MODE_PRIVATE)
                return editor.getString("userid", "")
            } catch (ex: Exception) {
                return ""
            }

        }


    //    public void addUserId(String key, Context context) {
    //        try {
    //            SharedPreferences.Editor editor = context.getSharedPreferences("authentication", MODE_PRIVATE).edit();
    //            editor.putString("userid", key);
    //            editor.commit();
    //        }
    //        catch (Exception ex){
    //
    //        }
    //    }
    val facebookAcc: Boolean
        get() {
            try {
                val editor = activity.getSharedPreferences("facebookacc", MODE_PRIVATE)
                return if (editor.getString("facebook", "")!!.isEmpty())
                    false
                else
                    true
            } catch (ex: Exception) {
                return false
            }

        }
    val googleAcc: Boolean
        get() {
            try {
                val editor = activity.getSharedPreferences("googleacc", MODE_PRIVATE)
                return if (editor.getString("google", "")!!.isEmpty())
                    false
                else
                    true
            } catch (ex: Exception) {
                return false
            }

        }
    val emailAcc: Boolean
        get() {
            try {
                val editor = activity.getSharedPreferences("emailacc", MODE_PRIVATE)

                return if (editor.getString("email", "")!!.isEmpty())
                    false
                else
                    true

            } catch (ex: Exception) {
                return false
            }

        }

    internal var sharedPreferences: SharedPreferences
    // [START declare_auth]
    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]

    private var auth: FirebaseAuth? = null


    private var loginButton: LoginButton? = null
    internal var fbLoginManager: com.facebook.login.LoginManager? = null
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    // List<String> permissionNeeds= Arrays.asList("email","public_profile");
    internal var databaseArtists: DatabaseReference
    internal var sign_in: Button
    internal var connected: Boolean = false
    internal var fb_auth_login: LinearLayout
    internal var signup: TextView
    internal var fb_text: TextView
    internal var context: LoginActivity
    internal var fragment: Fragment? = null
    internal var email: EditText
    internal var password: EditText
    internal var first_name: String
    internal var last_name: String
    internal var email_fb: String
    internal var user_id: String? = null

    private val mListener: OnFragmentInteractionListener? = null

    private var fpwd: TextView? = null
    private var mAuth1: FirebaseAuth? = null

    private var signInButton: SignInButton? = null
    private var callbackManager: CallbackManager? = null
    //Signing Options
    private var gso: GoogleSignInOptions? = null
    private val accessTokenTracker: AccessTokenTracker? = null
    private var profileTracker: ProfileTracker? = null
    //google api client
    private var mGoogleApiClient: GoogleApiClient? = null

    //Signin constant to check the activity result
    private val RC_SIGN_IN = 100

    internal var count = 0

    fun addEmail(email: String?) {
        try {
            run { }
            val editor = activity.getSharedPreferences("useremail", MODE_PRIVATE).edit()
            editor.putString("email", email)
            editor.commit()
        } catch (ex: Exception) {

        }

    }

    private fun prepareMovieData() {
        mDatabase = FirebaseDatabase.getInstance().reference
        invitedref = mDatabase!!.child("Posts")
        invitedref!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (noteSnapshot in dataSnapshot.children) {
                    val note = noteSnapshot.getValue<AddMomentPojo>(AddMomentPojo::class.java)
                    // Log.d("OnCancelledCalling ","aaa1"+ note.getSenderid().equals(addUserId()));
                    //                   if(note.getParentid().equals(getDefaults()))
                    if (note!!.getPostImageOrVideoUrl().isEmpty()) {

                    } else {
                        val islandRef = storageRef.child(note!!.getPostImageOrVideoUrl())


                        val ONE_MEGABYTE = (1024 * 1024).toLong()
                        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(OnSuccessListener<ByteArray> { bytes ->
                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                            CommonInterface.bitmapArrayList.add(bmp)
                            CommonInterface.stringArrayList.add(note!!.getPostDate())
                            //Toast.makeText(getActivity(),"message",Toast.LENGTH_SHORT).show();
                            //                                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, imageView.getWidth(),
                            //                                            imageView.getHeight(), false));
                        }).addOnFailureListener(OnFailureListener {
                            // Handle any errors
                        })

                    }
                    //arrayList.add(note);
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {

                Log.d("On Cancelled Calling ", databaseError.toString())

            }


        })


        // mAdapter.notifyDataSetChanged();
    }

    private fun sharePhotoToFacebook() {
        val image = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val photo = SharePhoto.Builder()
                .setBitmap(image)
                .setCaption("Give me my codez or I will ... you know, do that thing you don't like!")
                .build()

        val content = SharePhotoContent.Builder()
                .addPhoto(photo)
                .build()

        ShareApi.share(content, null)
        Toast.makeText(activity, "shared", Toast.LENGTH_SHORT).show()

    }

    fun userFacebookAcc(key: String) {
        try {
            val editor = activity.getSharedPreferences("facebookacc", MODE_PRIVATE).edit()
            editor.putString("facebook", key)
            editor.commit()
        } catch (ex: Exception) {

        }

    }

    fun userGoogleAcc(key: String) {
        try {
            val editor = activity.getSharedPreferences("googleacc", MODE_PRIVATE).edit()
            editor.putString("google", key)
            editor.commit()
        } catch (ex: Exception) {

        }

    }

    fun userEmailAcc(key: String) {
        try {
            val editor = activity.getSharedPreferences("emailacc", MODE_PRIVATE).edit()
            editor.putString("email", key)
            editor.commit()
        } catch (ex: Exception) {

        }

    }

    fun setTextDate() {


        val childPojoArrayList = ArrayList<ChildPojo>()
        mDatabase = FirebaseDatabase.getInstance().reference
        invitedref = mDatabase!!.child("Posts")
        invitedref!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (noteSnapshot in dataSnapshot.children) {
                    val note = noteSnapshot.getValue<AddMomentPojo>(AddMomentPojo::class.java)
                    // Log.d("whatisid",note.getParentId()+","+userid);
                    //Toast.makeText(getActivity(),""+note.getParentId()+","+userid,Toast.LENGTH_SHORT).show();
                    if (note!!.getUserId().equals(defaults)) {
                        CommonInterface.date.add(note!!.getPostDate())
                    }
                    //arrayList.add(note);
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

                Log.d("On Cancelled Calling ", databaseError.toString())

            }


        })

        //
        //        TextPostDatabase textPostDatabase=new TextPostDatabase(this);
        //        CommonInterface.date=textPostDatabase.getAllDate();

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseArtists = FirebaseDatabase.getInstance().getReference("Users")
        FacebookSdk.sdkInitialize(activity.applicationContext)
        setTextDate()
        prepareMovieData()
        val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).state == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).state == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true
        } else
            connected = false
        auth = FirebaseAuth.getInstance()
        //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        callbackManager = CallbackManager.Factory.create()
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        // accessTokenTracker.startTracking();
        // profileTracker.startTracking();

        databaseArtists.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                this@Login.dataSnapshot = dataSnapshot


            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    private fun signIn() {
        //Creating an intent
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)

        //Starting intent for result
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.savedInstanceState = savedInstanceState


        // Inflate the layout for this fragment

        val v = inflater.inflate(R.layout.fragment_login, container, false)
        spinner = v.findViewById(R.id.progressBar)
        sign_in = v.findViewById(R.id.sign_in)
        signup = v.findViewById(R.id.signup)
        fpwd = v.findViewById(R.id.fpwd)
        email = v.findViewById(R.id.email)
        fb_text = v.findViewById(R.id.fb_text)
        password = v.findViewById(R.id.password)
        fb_auth_login = v.findViewById(R.id.fb_auth_login)
        loginButton = v.findViewById(R.id.login_button) as LoginButton
        //google signin
        signInButton = v.findViewById(R.id.sign_in_button)



        password.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val isvalidEmail = emailValidator(email.text.toString())

                if (isvalidEmail != true) {

                    email.error = "not valid email"
                }


            }
        }



        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        // [END config_signin]
        Log.d("whatdata", gso.toString() + "" + Auth.GOOGLE_SIGN_IN_API)
        sharedPreferences = activity.getSharedPreferences("facebookgoogledata", MODE_PRIVATE)


        mGoogleApiClient = GoogleApiClient.Builder(activity)
                .enableAutoManage(LoginActivity.loginActivity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso!!)
                .build()

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance()
        signInButton!!.setOnClickListener(this)



        mAuth1 = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()
        loginButton!!.setReadPermissions(Arrays.asList("email"))
        loginButton!!.setFragment(this)
        // Other app specific specialization

        // Callback registration

        loginButton!!.setOnClickListener(View.OnClickListener {
            if (connected == true) {
                loginButton!!.registerCallback(callbackManager, object : FacebookCallback<LoginResult>() {
                    fun onSuccess(loginResult: LoginResult) {
                        Log.d("onsuccess", "onsuccess1")

                        getEmail(loginResult)
                        if (Profile.getCurrentProfile() == null) {
                            profileTracker = object : ProfileTracker() {
                                protected fun onCurrentProfileChanged(profile: Profile, profile2: Profile) {
                                    // profile2 is the new profile
                                    //Log.v("facebook - profile", profile2.getFirstName());
                                    Login.profile = profile2
                                    handleFacebookAccessToken(loginResult.getAccessToken(), Login.profile)
                                    // Toast.makeText(getActivity(), "" + profile2.getFirstName(), Toast.LENGTH_SHORT).show();
                                    //profileTracker.stopTracking();

                                }
                            }

                            // no need to call startTracking() on mProfileTracker
                            // because it is called by its constructor, internally.
                        } else {

                            val profile = Profile.getCurrentProfile()

                            handleFacebookAccessToken(loginResult.getAccessToken(), profile)
                            //Toast.makeText(getActivity(), "" + profile.getFirstName(), Toast.LENGTH_SHORT).show();
                            Log.v("facebook - profile", profile.getFirstName())
                        }


                    }

                    fun onCancel() {
                        Log.d("onsuccess", "onsuccess")
                    }

                    fun onError(exception: FacebookException) {
                        Log.d("onsuccess", "onsuccess")
                    }
                })


            } else
                Toast.makeText(activity, "no internet connection", Toast.LENGTH_SHORT).show()
        })



        sign_in.setOnClickListener {
            if (connected == true) {
                if (!email.text.toString().isEmpty() || !password.text.toString().isEmpty()) {
                    if (password.length() < 8)
                        password.error = "8 digit password"
                    else {
                        spinner!!.visibility = View.VISIBLE

                        auth!!.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                                .addOnCompleteListener(activity) { task ->
                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful) {
                                        // there was an error
                                        Toast.makeText(activity, "invalid email/password", Toast.LENGTH_LONG).show()
                                        spinner!!.visibility = View.GONE

                                    } else {
                                        val user = task.result.user
                                        addEmail(email.text.toString())
                                        Log.d(TAG, "onComplete: uid=" + user.uid)
                                        //Toast.makeText(getActivity(), "" + user.getUid(), Toast.LENGTH_SHORT).show();
                                        //                                    if (sharedPreferences.getString("child", null) == null) {
                                        val editor1 = activity.getSharedPreferences("userdetail", MODE_PRIVATE).edit()
                                        editor1.putString("firstname", task.result.user.displayName)
                                        editor1.putString("lastname", task.result.user.displayName)
                                        editor1.putString("userid", task.result.user.uid)
                                        editor1.commit()


                                        val editor = activity.getSharedPreferences("authentication", MODE_PRIVATE).edit()
                                        editor.putString("userid", task.result.user.uid)
                                        editor.commit()
                                        if (emailAcc == false) {
                                            userEmailAcc(task.result.user.uid)
                                            //addUserId(task.getResult().getUser().getUid(),getActivity());
                                            val addChild = AddChild()
                                            val manager = fragmentManager
                                            val transaction = manager.beginTransaction()
                                            transaction.replace(R.id.mainFrame, addChild)
                                            transaction.commit()
                                            spinner!!.visibility = View.GONE
                                            //Toast.makeText(getActivity(), task.getResult().getUser().getUid(), Toast.LENGTH_LONG).show();
                                            //
                                        } else {
                                            userEmailAcc(task.result.user.uid)
                                            val intent = Intent(activity, DashBoard::class.java)
                                            startActivity(intent)
                                            spinner!!.visibility = View.GONE

                                            //Toast.makeText(getActivity(), task.getResult().getUser().getUid(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                    }
                } else
                    Toast.makeText(activity, "enter email/pwd", Toast.LENGTH_SHORT).show()

            }


            //
            //        boolean exists = false;
            //
            //      // Toast.makeText(getActivity(),dataSnapshot.,Toast.LENGTH_SHORT).show();
            //try {
            //    for (DataSnapshot data : dataSnapshot.getChildren()) {
            //        //    i++;
            //        String userName = String.valueOf(data.child("userEmail").getValue());
            //
            //
            //        Map<String, Object> model = (Map<String, Object>) data.getValue();
            //        try {
            //            if (model.get("userEmail").equals(email.getText().toString().trim()) && model.get("userPassword").equals(password.getText().toString().trim())) {
            //                exists = true;
            //
            //                email_fb = model.get("userEmail").toString();
            //                first_name = model.get("userName").toString();
            //                last_name = model.get("lastName").toString();
            //                user_id = model.get("userId").toString();
            //                setDefaults("email", "user_firstname", "user_lastname", "userId", email_fb, first_name, last_name, user_id, getActivity());
            //                Toast.makeText(getActivity(), getDefaults("userId", getActivity()), Toast.LENGTH_SHORT).show();
            //                break;
            //            }
            //        } catch (Exception ex) {
            //
            //        }
            //    }
            //}
            //catch (Exception ex){
            //
            //}
            //        if(exists) {
            //
            //            Toast.makeText(getActivity(),"successfully logged in...",Toast.LENGTH_SHORT).show();
            //
            //          Intent intent=new Intent(getActivity(),DashBoard.class);
            //            startActivity(intent);
            //
            //
            //            // This user already exists in firebase.
            //        }
            //        else {
            //
            //            Toast.makeText(getActivity(),"User not found...",Toast.LENGTH_SHORT).show();
            //            // This user doesn't exists in firebase.
            //        }
            //
        }

        signup.setOnClickListener {
            fragment = SignUp()

            if (savedInstanceState == null) {
                if (connected == true) {

                    val manager = fragmentManager
                    val transaction = manager.beginTransaction()
                    transaction.replace(R.id.mainFrame, fragment)
                    transaction.commit()
                } else
                    Toast.makeText(activity, "no internet connection", Toast.LENGTH_SHORT).show()
            }
        }

        fpwd!!.setOnClickListener {
            fragment = ForgotPassword()

            if (savedInstanceState == null) {

                if (connected == true) {
                    val manager = fragmentManager
                    val transaction = manager.beginTransaction()
                    transaction.replace(R.id.mainFrame, fragment)
                    transaction.commit()
                }
            } else
                Toast.makeText(activity, "no internet connection", Toast.LENGTH_SHORT).show()
        }
        return v
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        mListener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {

        this.context = context as LoginActivity


        super.onAttach(context)

    }

    fun emailValidator(email: String): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern.matcher(email)
        return matcher.matches()
    }

    override fun onDetach() {
        super.onDetach()
        //mListener = null;
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.sign_in_button) {
            signIn()
        }

        //        else
        //            signOut();

    }


    private fun handleFacebookAccessToken(token: AccessToken, profile: Profile) {
        Log.d("handleFacebookAcce", "handleFacebookAccessToken:$token")


        val credential = FacebookAuthProvider.getCredential(token.getToken())
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information

                        val user = mAuth!!.currentUser
                        // Log.d("signInWithemail",task.getResult().getUser().getEmail());
                        updateUI(user)

                        val artist1 = UserDetail(profile.getFirstName(), profile.getLastName(), task.result.user.uid, facebookemail, SimpleDateFormat("dd:MMM:yyyy").format(Date()), "user", "android")
                        val editor1 = activity.getSharedPreferences("userdetail", MODE_PRIVATE).edit()
                        editor1.putString("firstname", profile.getFirstName())
                        editor1.putString("lastname", profile.getLastName())
                        editor1.putString("userid", task.result.user.uid)
                        editor1.commit()
                        //Saving the Artist
                        databaseArtists.child(task.result.user.uid).setValue(artist1)
                        displayMessage(profile)
                        userFacebookAcc(task.result.user.uid)
                        val editor = activity.getSharedPreferences("authentication", MODE_PRIVATE).edit()
                        editor.putString("userid", task.result.user.uid)
                        editor.commit()
                        //addUserId(task.getResult().getUser().getUid(),getActivity());
                        // Toast.makeText(getActivity(),user.getEmail(),Toast.LENGTH_SHORT).show();
                        //
                        //                            mAuth.createUserWithEmailAndPassword(profile.getId(),profile.getId())
                        //                                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        //                                        @Override
                        //                                        public void onComplete(@NonNull Task<AuthResult> task) {
                        //                                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        //
                        //                                            // If sign in fails, display a message to the user. If sign in succeeds
                        //                                            // the auth state listener will be notified and logic to handle the
                        //                                            // signed in user can be handled in the listener.
                        //                                            if (!task.isSuccessful()) {
                        //                                                Toast.makeText(getActivity(), "success",
                        //                                                        Toast.LENGTH_SHORT).show();
                        //                                            }
                        //
                        //                                            // ...
                        //                                        }
                        //                                    });


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(activity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // ...
                }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

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
    //    @Override
    //    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        super.onActivityResult(requestCode, resultCode, data);
    //        callbackManager.onActivityResult(requestCode, resultCode, data);
    //        addUser();
    //
    //    }


    override fun onStart() {
        super.onStart()


        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)


        //attaching value event listener
        databaseArtists.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                this@Login.dataSnapshot = dataSnapshot

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        //        FirebaseUser currentUser = mAuth.getCurrentUser();
        //        updateUI(currentUser);
        if (mGoogleApiClient != null)
            mGoogleApiClient!!.connect()


    }

    //    private void addUser() {
    //        //getting the values to save
    //
    ////        }
    //        String id1 = databaseArtists.push().getKey();
    //
    //
    //
    //            //getting a unique id using push().getKey() method
    //            //it will create a unique id and we will use it as the Primary Key for our Artist
    //            String id = databaseArtists.push().getKey();
    //
    //
    //
    //
    //
    //            //creating an Artist Object
    //            UserDetail artist1 = new UserDetail(id, first_name, first_name,email_fb," ");
    //
    //            //Saving the Artist
    //            databaseArtists.child(id).setValue(artist1);
    //
    //            //setting edittext to blank again
    //         //   user_firstname.setText("");
    //
    //            //displaying a success toast
    //            //Toast.makeText(getActivity(), "User added", Toast.LENGTH_LONG).show();
    //
    //    }

    private fun displayMessage(profile: Profile?) {
        if (profile != null) {

            first_name = profile!!.getFirstName()
            last_name = profile!!.getLastName()
            email_fb = profile!!.getId()

            if (facebookAcc == false) {
                //Toast.makeText(getActivity(), "displayMessage", Toast.LENGTH_SHORT).show();
                val addChild = AddChild()
                val manager = fragmentManager
                val transaction = manager.beginTransaction()
                transaction.add(R.id.mainFrame, addChild)
                transaction.commit()

            } else {
                val intent = Intent(activity, DashBoard::class.java)
                activity.startActivity(intent)
                activity.finish()
            }

        }

    }

    override fun onStop() {
        super.onStop()
        // accessTokenTracker.stopTracking();
        //        profileTracker.stopTracking();
        //                mGoogleApiClient.stopAutoManage(LoginActivity.loginActivity);
        mGoogleApiClient!!.disconnect()
    }

    override fun onResume() {
        super.onResume()
        val profile = Profile.getCurrentProfile()
        //  displayMessage(profile);
    }

    //
    //    public void RequestData(){
    //        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
    //            @Override
    //            public void onCompleted(JSONObject object, GraphResponse response) {
    //
    //                JSONObject json = response.getJSONObject();
    //                Log.d("json:::",json.toString());
    //                try {
    //                    if(json != null){
    //                        String text = "<b>Name :</b> "+json.getString("name")+"<br><br><b>Email :</b> "+json.getString("email")+"<br><br><b>Profile link :</b> "+json.getString("link");
    //                       // details_txt.setText(Html.fromHtml(text));
    //                        first_name=json.getString("name");
    //                        email_fb=json.getString("email");
    //                        user_id=json.getString("id");
    //                        String id1 = databaseArtists.push().getKey();
    //
    //
    //
    //                        //getting a unique id using push().getKey() method
    //                        //it will create a unique id and we will use it as the Primary Key for our Artist
    //                        String id = databaseArtists.push().getKey();
    //
    //
    //                        for (DataSnapshot data : dataSnapshot.getChildren()) {
    //
    //                            String userName = String.valueOf(data.child("userEmail").getValue());
    //                            if (userName.equalsIgnoreCase(email_fb)) {
    //                               // Toast.makeText(getActivity(), "Already have account::", Toast.LENGTH_SHORT).show();
    //
    //email_fb="";
    //                            }
    //
    //                        }
    //
    //
    //                        if (!(TextUtils.isEmpty(email_fb) )) {
    //
    //
    //                            UserDetail artist1 = new UserDetail(id, first_name, first_name,email_fb," ");
    //
    //                            //Saving the Artist
    //                            databaseArtists.child(id).setValue(artist1);
    //
    //
    //                            setDefaults("email","user_firstname","user_lastname","userId",email_fb,first_name,last_name,user_id,getActivity());
    //                        } else {
    //                            //if the value is not given displaying a toast
    //                            //Toast.makeText(getActivity(), "Please Sign in with unique email......", Toast.LENGTH_LONG).show();
    //                        }
    //
    //
    //
    //                    }
    //
    //                } catch (JSONException e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //        });
    //        Bundle parameters = new Bundle();
    //        parameters.putString("fields", "id,name,link,email,picture");
    //        request.setParameters(parameters);
    //        request.executeAsync();
    //    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        //Log.d("firebaseAuthWithGoogle:",acct.getIdToken()+","+acct.getId());
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]
        //if(connected==true) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this@Login.activity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth!!.currentUser
                        //updateUI(user);
                        Log.d("firebaseAuthWithGoogle:", task.result.user.uid + "" + user!!.uid)
                        //Toast.makeText(getActivity(),task.getResult().getUser().getUid(),Toast.LENGTH_SHORT).show();
                        val artist1 = UserDetail(task.result.user.displayName, task.result.user.displayName, task.result.user.uid, user.email, SimpleDateFormat("dd:MMM:yyyy").format(Date()), "user", "android")
                        addEmail(user.email)
                        //Saving the Artist
                        databaseArtists.child(task.result.user.uid).setValue(artist1)
                        //addUserId(task.getResult().getUser().getUid(), getActivity());
                        try {
                            run { }
                            val editor1 = activity.getSharedPreferences("userdetail", MODE_PRIVATE).edit()
                            editor1.putString("firstname", task.result.user.displayName)
                            editor1.putString("lastname", task.result.user.displayName)
                            editor1.putString("userid", task.result.user.uid)
                            editor1.commit()
                        } catch (ex: Exception) {

                        }

                        if (googleAcc == false) {

                            try {
                                val addChild = AddChild()
                                val manager = fragmentManager
                                val transaction = manager.beginTransaction()
                                transaction.add(R.id.mainFrame, addChild)
                                transaction.commit()
                            } catch (ex: Exception) {

                            }

                        } else {
                            try {

                                val intent = Intent(activity, DashBoard::class.java)
                                activity.startActivity(intent)
                                activity.finish()
                            } catch (ex: Exception) {

                            }

                        }
                        userGoogleAcc(task.result.user.uid)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(activity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // [START_EXCLUDE]
                    //hideProgressDialog();
                    // [END_EXCLUDE]
                }
        //}
        //else
        //    Toast.makeText(getActivity(),"no internet connection",Toast.LENGTH_SHORT).show();
    }

    private fun signOut() {
        // Firebase sign out
        // mAuth.signOut();
        // LoginManager.getInstance().logOut();
        // Google sign out
        //        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
        //                new ResultCallback<Status>() {
        //                    @Override
        //                    public void onResult(@NonNull Status status) {
        //                        updateUI(null);
        //                    }
        //                });
    }

    private fun revokeAccess() {
        // Firebase sign out
        // mAuth.signOut();

        // Google revoke access
        //        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
        //                new ResultCallback<Status>() {
        //                    @Override
        //                    public void onResult(@NonNull Status status) {
        //                        updateUI(null);
        //                    }
        //                });
    }

    private fun updateUI(user: FirebaseUser?) {
        // hideProgressDialog();
        if (user != null) {

        } else {

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        //if(connected==true) {

        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                val account = result.getSignInAccount()
                firebaseAuthWithGoogle(account)
                //Toast.makeText(getActivity(),""+account.getId(),Toast.LENGTH_SHORT).show();

                val id = databaseArtists.push().key
                //creating an Artist Object


                if (sharedPreferences.getString("child", null) == null) {


                    val addChild = AddChild()
                    val manager = fragmentManager
                    val transaction = manager.beginTransaction()
                    transaction.replace(R.id.mainFrame, addChild)
                    transaction.commit()

                    //
                } else {
                    val intent = Intent(activity, DashBoard::class.java)
                    startActivity(intent)
                }
            }
            //        else
            //
            //            //addUser();
            //    }
            //}
            //else
            //    Toast.makeText(getActivity(),"no internet connection",Toast.LENGTH_SHORT).show();
        }
    }


    //getting login info from device

    //    public static String getDefaults(String key, Context context) {
    //        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    //        return preferences.getString(key, "No detail");
    //    }


    override fun onDestroy() {
        super.onDestroy()
        mGoogleApiClient!!.stopAutoManage(LoginActivity.loginActivity)
        mGoogleApiClient!!.disconnect()
    }


    fun getEmail(loginResult: LoginResult) {


        val request = GraphRequest.newMeRequest(loginResult.getAccessToken(), object : GraphRequest.GraphJSONObjectCallback() {

            fun onCompleted(`object`: JSONObject, response: GraphResponse) {
                Log.i("LoginActivity", response.toString())
                // Get facebook data from login
                val bFacebookData = getFacebookData(`object`)
            }
        })
        val parameters = Bundle()
        parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location") // Parámetros que pedimos a facebook
        request.setParameters(parameters)
        request.executeAsync()


    }

    private fun getFacebookData(`object`: JSONObject): Bundle? {

        try {
            val bundle = Bundle()
            val id = `object`.getString("id")

            try {
                val profile_pic = URL("https://graph.facebook.com/$id/picture?width=200&height=150")
                Log.i("profile_pic", profile_pic.toString() + "")
                bundle.putString("profile_pic", profile_pic.toString())

            } catch (e: MalformedURLException) {
                e.printStackTrace()
                return null
            }

            bundle.putString("idFacebook", id)
            if (`object`.has("first_name"))
                bundle.putString("first_name", `object`.getString("first_name"))
            if (`object`.has("last_name"))
                bundle.putString("last_name", `object`.getString("last_name"))
            if (`object`.has("email"))
                facebookemail = `object`.getString("email")
            if (`object`.has("gender"))
                bundle.putString("gender", `object`.getString("gender"))
            if (`object`.has("birthday"))
                bundle.putString("birthday", `object`.getString("birthday"))
            if (`object`.has("location"))
                bundle.putString("location", `object`.getJSONObject("location").getString("name"))

            return bundle
        } catch (e: JSONException) {
            Log.d(TAG, "Error parsing JSON")
        }

        return null
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"
        internal var profile: Profile
        var pagestatus = 0
        private val TAG = "GoogleActivity"
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
        //setting login data method

        fun setDefaults(key: String, key1: String, key2: String, key3: String, value1: String, value2: String, value3: String, value4: String, context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            editor.putString(key, value1)
            editor.putString(key1, value2)
            editor.putString(key2, value3)
            editor.putString(key3, value4)
            editor.commit()
        }
    }
}
