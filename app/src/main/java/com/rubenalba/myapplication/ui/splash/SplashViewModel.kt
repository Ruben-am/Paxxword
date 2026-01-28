package com.rubenalba.myapplication.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.myapplication.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    private val _startDestination = MutableStateFlow("signup")
    val startDestination = _startDestination.asStateFlow()

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        viewModelScope.launch {
            // delay(1000)

            val user = userDao.getAppUser()
            if (user != null) {
                _startDestination.value = "login"
            } else {
                _startDestination.value = "signup"
            }

            _isLoading.value = false
        }
    }
}