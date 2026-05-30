package com.nyxtesla.talk2u

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    // If Firebase already has a current user, navigate immediately.
    LaunchedEffect(auth.currentUser) {
        if (auth.currentUser != null) onLoginSuccess()
    }
    val firestore = FirebaseFirestore.getInstance()

    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val clientIdWeb = "663594224067-ilau0mrmv1krck86qa2ha018d52vg9l5.apps.googleusercontent.com"

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientIdWeb)
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                loading = true
                auth.signInWithCredential(credential).addOnCompleteListener { authResult ->
                    loading = false
                    if (authResult.isSuccessful) {
                        // Unlock navigation immediately when Firebase authentication completes.
                        Handler(Looper.getMainLooper()).post { onLoginSuccess() }

                        // Persist minimal profile info asynchronously; do not block navigation.
                        val user = auth.currentUser
                        val uid = user?.uid ?: ""
                        val doc = hashMapOf<String, Any>(
                            "name" to (user?.displayName ?: ""),
                            "email" to (user?.email ?: ""),
                            "photoUrl" to (user?.photoUrl?.toString() ?: ""),
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                        firestore.collection("users").document(uid).set(doc)
                            .addOnSuccessListener {
                                // profile saved; nothing else required for navigation
                            }
                            .addOnFailureListener { ex -> error = ex?.message ?: "Failed to save profile" }
                    } else {
                        error = authResult.exception?.message ?: "Sign-in failed"
                    }
                }
            } catch (e: ApiException) {
                error = "Google sign in failed: ${e.message}"
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("Secure Gateway", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
            Button(onClick = { launcher.launch(googleSignInClient.signInIntent) }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary) else Text("Sign in with Google")
            }
            Spacer(Modifier.height(8.dp))
            Text("Sign in with your Google account to continue.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
    }
}