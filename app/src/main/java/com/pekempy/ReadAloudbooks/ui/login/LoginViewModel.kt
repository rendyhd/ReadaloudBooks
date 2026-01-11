package com.pekempy.ReadAloudbooks.ui.login

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pekempy.ReadAloudbooks.data.UserPreferencesRepository
import com.pekempy.ReadAloudbooks.data.api.AppContainer
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class LoginViewModel(private val repository: UserPreferencesRepository) : ViewModel() {
    var url by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun onLoginClick(onSuccess: () -> Unit) {
        if (url.isBlank() || username.isBlank() || password.isBlank()) {
            errorMessage = "All fields are required"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            try {
                val apiManager = AppContainer.apiClientManager
                apiManager.updateConfig(url, null)
                
                val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
                val passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull())
                
                val response = apiManager.getApi().login(usernamePart, passwordPart)
                
                repository.saveCredentials(apiManager.baseUrl ?: url, username, response.accessToken)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Authentication failed"
            } finally {
                isLoading = false
            }
        }
    }
}
