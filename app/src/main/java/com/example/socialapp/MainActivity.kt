package com.example.socialapp

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialapp.doas.PostDao
import com.example.socialapp.doas.UserDoa
import com.example.socialapp.models.Post
import com.example.socialapp.models.User
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInApi
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.tasks.await


class MainActivity : AppCompatActivity(), IPostAdapter {

    private lateinit var adapter: PostAdapter
    private lateinit var postDao: PostDao
    var mFirebaseUserId: String = ""
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val auth by lazy{
        FirebaseAuth.getInstance()
    }
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener{
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }
        setUpRecyclerView()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            /*.requestIdToken(getString(R.string.default_web_client_id))*/
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
        mGoogleApiClient.connect()
    }

    private fun setUpRecyclerView() {
        postDao = PostDao()
        val postCollections = postDao.postCollection
        val query = postCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        adapter = PostAdapter(recyclerViewOptions, this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.log_out_alert_dialog_title_text)
        builder.setMessage(R.string.log_out_alert_dialog_message_text)
        if (auth.currentUser != null)
            mFirebaseUserId = auth.currentUser!!.uid
        builder.setPositiveButton(R.string.yes_text) { dialogInterface, _ ->
            auth.signOut()
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel_text) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        mDialog = builder.create()
        mDialog.setCancelable(false)
        when (id) {
            R.id.logOutButton -> {
                mDialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}