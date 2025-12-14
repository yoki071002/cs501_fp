// File: app/src/main/java/com/example/cs501_fp/data/firebase/FirebaseAuthManager.kt
// Encapsulates Firebase Authentication logic (Login & Register) to separate API calls from UI.

package com.example.cs501_fp.data.firebase

import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()

    fun register(email: String, pwd: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun login(email: String, pwd: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
}