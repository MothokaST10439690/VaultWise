package com.example.vaultwise.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaultwise.data.AppDatabase
import com.example.vaultwise.data.entity.User
import com.example.vaultwise.data.repository.UserRepository
import com.example.vaultwise.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val sessionManager = SessionManager(application)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    fun login(username: String, passwordHash: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null && user.passwordHash == passwordHash) {
                sessionManager.saveSession(user.id, user.username)
                _authState.value = AuthState.Success(user)
            } else {
                _authState.value = AuthState.Error("Invalid username or password")
            }
        }
    }

    fun signUp(username: String, passwordHash: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val existingUser = repository.getUserByUsername(username)
            if (existingUser != null) {
                _authState.value = AuthState.Error("Username already exists")
            } else {
                val newUser = User(username = username, passwordHash = passwordHash)
                val id = repository.insertUser(newUser)
                val userWithId = newUser.copy(id = id.toInt())
                sessionManager.saveSession(userWithId.id, userWithId.username)
                _authState.value = AuthState.Success(userWithId)
            }
        }
    }

    fun getLoggedInUserId(): Int {
        return sessionManager.getUserId()
    }

    fun logout() {
        sessionManager.clearSession()
        _authState.value = AuthState.LoggedOut
    }

    fun checkSession(): Boolean {
        return sessionManager.isLoggedIn()
    }

    fun getLoggedInUsername(): String? {
        return sessionManager.getUsername()
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
        object LoggedOut : AuthState()
    }
}
