package com.example.cs501_fp.data.firebase

import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()

    fun register(email: String, pwd: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener {
                if (it.isSuccessful) onResult(true, null)
                else onResult(false, it.exception?.message)
            }
    }

    fun login(email: String, pwd: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pwd)
            .addOnCompleteListener {
                if (it.isSuccessful) onResult(true, null)
                else onResult(false, it.exception?.message)
            }
    }
}