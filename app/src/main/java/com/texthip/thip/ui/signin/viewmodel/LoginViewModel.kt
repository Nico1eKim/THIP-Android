package com.texthip.thip.ui.signin.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.texthip.thip.data.manager.FcmTokenManager
import com.texthip.thip.data.manager.TokenManager
import com.texthip.thip.data.model.auth.response.AuthResponse
import com.texthip.thip.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val response: AuthResponse) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val fcmTokenManager: FcmTokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun kakaoLogin(context: Context) {
        viewModelScope.launch {
            _uiState.update { LoginUiState.Loading }

            //카카오 로그인부터 서버 통신까지
            authRepository.loginWithKakao(context)
                .onSuccess { response ->
                    if (response != null) {
                        if (response.isNewUser) {
                            tokenManager.saveTempToken(response.token) // 신규 유저는 임시 토큰으로 저장
                            _uiState.update { LoginUiState.Success(response) }
                        } else {
                            tokenManager.saveToken(response.token) // 기존 유저는 정식 토큰으로 저장
                            // 기존 유저의 경우 FCM 토큰 전송 후 Success 상태 업데이트
                            sendFcmTokenAndUpdateState(response)
                        }
                    } else {
                        _uiState.update { LoginUiState.Error("서버로부터 응답을 받지 못했습니다.") }
                    }
                }
                .onFailure { throwable ->
                    Log.e("LoginViewModel", "Login failed: ${throwable.message}", throwable)
                    _uiState.update {
                        LoginUiState.Error(throwable.message ?: "알 수 없는 통신 오류가 발생했습니다.")
                    }
                }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _uiState.update { LoginUiState.Loading }

            //구글 로그인부터 서버 통신까지
            authRepository.loginWithGoogle(idToken)
                .onSuccess { response ->
                    if (response != null) {
                        if (response.isNewUser) {
                            tokenManager.saveTempToken(response.token) // 신규 유저는 임시 토큰으로 저장
                            _uiState.update { LoginUiState.Success(response) }
                        } else {
                            tokenManager.saveToken(response.token) // 기존 유저는 정식 토큰으로 저장
                            // 기존 유저의 경우 FCM 토큰 전송 후 Success 상태 업데이트
                            sendFcmTokenAndUpdateState(response)
                        }
                    } else {
                        _uiState.update { LoginUiState.Error("서버로부터 응답을 받지 못했습니다.") }
                    }
                }
                .onFailure { throwable ->
                    Log.e("LoginViewModel", "Login failed: ${throwable.message}", throwable)
                    _uiState.update {
                        LoginUiState.Error(throwable.message ?: "알 수 없는 통신 오류가 발생했습니다.")
                    }
                }
        }
    }

    //상태를 다시 초기화-> 이벤트 중복 실행 방지
    fun clearLoginState() {
        _uiState.update { LoginUiState.Idle }
    }

    private fun sendFcmTokenAndUpdateState(response: AuthResponse) {
        viewModelScope.launch {
            fcmTokenManager.sendCurrentTokenIfExists()
            _uiState.update { LoginUiState.Success(response) }
        }
    }
}