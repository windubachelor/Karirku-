package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val AUTH_TAG = "KarirKuAuth"

@Volatile
private var pendingSignUpDisplayName: String? = null

fun consumePendingSignUpDisplayName(): String? {
    val name = pendingSignUpDisplayName
    pendingSignUpDisplayName = null
    return name?.takeIf { it.isNotBlank() }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val activityContext = context.findActivity() ?: context
            val credential = credentialManager.getCredential(activityContext, request).credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                Firebase.auth.signInWithCredential(
                    GoogleAuthProvider.getCredential(idToken, null)
                ).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onAuthError("Konfigurasi Google Sign-In belum tersedia (default_web_client_id tidak ditemukan).")
        return
    }
    val activity = context.findActivity()
    if (activity == null) {
        onAuthError("Tidak dapat membuka dialog masuk Google dari konteks ini.")
        return
    }
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val credential = credentialManager.getCredential(activity, request).credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                Firebase.auth.signInWithCredential(
                    GoogleAuthProvider.getCredential(idToken, null)
                ).await()
                onAuthSuccess()
            } else {
                onAuthError("Tipe kredensial tidak dikenali.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(AUTH_TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e(AUTH_TAG, "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Gagal masuk dengan Google. Silakan coba lagi.")
        }
    }
}

fun signInWithEmailPassword(
    email: String,
    password: String,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope
) {
    val trimmedEmail = email.trim()
    if (trimmedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
        onAuthError("Masukkan alamat email yang valid.")
        return
    }
    if (password.length < 6) {
        onAuthError("Kata sandi minimal terdiri dari 6 karakter.")
        return
    }

    scope.launch {
        try {
            Firebase.auth.signInWithEmailAndPassword(trimmedEmail, password).await()
            onAuthSuccess()
        } catch (e: Exception) {
            Log.e(AUTH_TAG, "Email/Password sign-in failed", e)
            onAuthError(mapFirebaseAuthError(e))
        }
    }
}

fun signUpWithEmailPassword(
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope
) {
    val trimmedName = fullName.trim()
    val trimmedEmail = email.trim()
    if (trimmedName.length < 2) {
        onAuthError("Masukkan nama lengkap minimal 2 karakter.")
        return
    }
    if (trimmedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
        onAuthError("Masukkan alamat email yang valid.")
        return
    }
    if (password.length < 6) {
        onAuthError("Kata sandi minimal terdiri dari 6 karakter.")
        return
    }
    if (password != confirmPassword) {
        onAuthError("Konfirmasi kata sandi tidak cocok.")
        return
    }

    scope.launch {
        pendingSignUpDisplayName = trimmedName
        try {
            val result = Firebase.auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            result.user?.let { user ->
                try {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = trimmedName
                    }
                    user.updateProfile(profileUpdates).await()
                } catch (profileErr: Exception) {
                    Log.w(AUTH_TAG, "Failed to update displayName after sign-up: ${profileErr.message}")
                }
            }
            onAuthSuccess()
        } catch (e: Exception) {
            pendingSignUpDisplayName = null
            Log.e(AUTH_TAG, "Email/Password sign-up failed", e)
            onAuthError(mapFirebaseAuthError(e))
        }
    }
}

fun sendPasswordReset(
    email: String,
    onResetSent: (String) -> Unit,
    onResetError: (String) -> Unit,
    scope: CoroutineScope
) {
    val trimmedEmail = email.trim()
    if (trimmedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
        onResetError("Isi alamat email yang valid terlebih dahulu untuk reset kata sandi.")
        return
    }
    scope.launch {
        try {
            Firebase.auth.sendPasswordResetEmail(trimmedEmail).await()
            onResetSent("Tautan reset kata sandi telah dikirim ke $trimmedEmail.")
        } catch (e: Exception) {
            Log.e(AUTH_TAG, "Password reset failed", e)
            onResetError(mapFirebaseAuthError(e))
        }
    }
}

private fun mapFirebaseAuthError(e: Exception): String {
    val code = (e as? FirebaseAuthException)?.errorCode
    return when (code) {
        "ERROR_INVALID_EMAIL" -> "Format alamat email tidak valid."
        "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Email atau kata sandi salah. Periksa kembali atau daftar akun baru."
        "ERROR_USER_NOT_FOUND" -> "Akun dengan email tersebut belum terdaftar. Silakan pilih tab Daftar."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Email ini sudah terdaftar. Silakan pindah ke tab Masuk."
        "ERROR_WEAK_PASSWORD" -> "Kata sandi terlalu lemah. Gunakan minimal 6 karakter."
        "ERROR_USER_DISABLED" -> "Akun ini telah dinonaktifkan."
        "ERROR_TOO_MANY_REQUESTS" -> "Terlalu banyak percobaan. Silakan tunggu sebentar lalu coba lagi."
        "ERROR_OPERATION_NOT_ALLOWED" -> "Login Email/Password belum diaktifkan di Firebase Console untuk proyek ini."
        else -> e.localizedMessage ?: "Autentikasi gagal. Periksa koneksi internet dan coba lagi."
    }
}

fun signOutUser(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(AUTH_TAG, "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}
