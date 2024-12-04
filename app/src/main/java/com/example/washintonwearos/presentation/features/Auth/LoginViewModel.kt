package com.example.washintonwearos.presentation.features.Auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow<SignInState>(SignInState.Nothing)
    val state = _state.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = SignInState.Loading
            Log.d("LoginViewModel", "Attempting to sign in with email: $email")

            try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginViewModel", "Login successful for user: ${task.result?.user?.email}")
                        _state.value = SignInState.Success
                    } else {
                        Log.e("LoginViewModel", "Login failed: ${task.exception?.message}")
                        _state.value = SignInState.Error
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Exception during login: ${e.message}", e)
                _state.value = SignInState.Error
            }
        }
    }
}



sealed class SignInState {
    object Nothing : SignInState()
    object Loading : SignInState()
    object Error: SignInState()
    object Success : SignInState()

}