package com.example.socialapp.doas

import com.example.socialapp.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDoa {
    private val db = FirebaseFirestore.getInstance()
    private  val usersCollection = db.collection("users")

    fun addUser(user: User?){
        user?.let{
            GlobalScope.launch(Dispatchers.IO) {
                usersCollection.document(user.uid).set(it)
            }
        }
    }
    fun getUserById(uId: String): Task<DocumentSnapshot>{
        return usersCollection.document(uId).get()
    }
    fun removeUser(firebaseUser: FirebaseUser?){
        firebaseUser?.let {
            GlobalScope.launch(Dispatchers.IO){
                usersCollection.document(firebaseUser.uid).delete()
            }
        }
    }
}