package com.texthip.thip.ui.signin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.texthip.thip.data.manager.FcmTokenManager
import com.texthip.thip.data.manager.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SplashDestination {
    data object Loading : SplashDestination
    data object NavigateToLogin : SplashDestination
    data object NavigateToHome : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val fcmTokenManager: FcmTokenManager
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination = _destination.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            delay(2000L) // 스플래시 최소 노출 시간

            val token = tokenManager.getTokenOnce()

            if (token.isNullOrBlank()) {
                _destination.value = SplashDestination.NavigateToLogin
            } else {
                // 자동 로그인 시 FCM 토큰 전송
                sendFcmToken()
                _destination.value = SplashDestination.NavigateToHome
            }
        }
    }

    private fun sendFcmToken() {
        viewModelScope.launch {
            fcmTokenManager.sendCurrentTokenIfExists()
        }
    }
}