package com.texthip.thip.ui.mypage.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kakao.sdk.user.UserApiClient
import com.texthip.thip.data.manager.TokenManager
import com.texthip.thip.data.repository.UserRepository
import com.texthip.thip.data.repository.NotificationRepository
import com.texthip.thip.utils.auth.clearAppScopeDeviceData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import javax.inject.Inject

data class DeleteAccountUiState(
    val isLoading: Boolean = false,
    val isDeleteCompleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeleteAccountUiState())
    val uiState = _uiState.asStateFlow()

    fun deleteAccount(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                // 1. FCM 토큰 삭제 (실패해도 계속 진행)
                notificationRepository.deleteFcmToken()
                    .onFailure { exception ->
                        Log.w("DeleteAccountViewModel", "FCM 토큰 삭제 실패 (계속 진행)", exception)
                    }

                // 2. 회원 탈퇴 요청
                userRepository.deleteAccount()
                    .onSuccess {
                        // 3. 로컬 데이터 정리 (동기화 보장)
                        performLocalDataCleanup(context)
                        
                        _uiState.update { it.copy(isLoading = false, isDeleteCompleted = true) }
                    }
                    .onFailure { exception ->
                        Log.e("DeleteAccountViewModel", "서버 회원탈퇴 요청 실패", exception)
                        throw exception
                    }
            }.onFailure { exception ->
                Log.e("DeleteAccountViewModel", "회원탈퇴 실패", exception)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "회원탈퇴에 실패했습니다."
                    )
                }
            }
        }
    }

    private suspend fun performLocalDataCleanup(context: Context) {
        runCatching {
            // 1. 토큰과 디바이스 데이터 정리
            tokenManager.clearTokens()
            context.clearAppScopeDeviceData()

            // 2. 카카오 SDK 연결 끊기 (동기화 보장)
            runCatching {
                unlinkKakaoAccount()
                Log.d("DeleteAccountViewModel", "카카오 연결 끊기 성공")
            }.onFailure { e ->
                Log.e("DeleteAccountViewModel", "카카오 연결 끊기 실패", e)
            }

            // 3. 구글 SDK 로그아웃
            runCatching {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                GoogleSignIn.getClient(context, gso).signOut()
                Log.d("DeleteAccountViewModel", "구글 로그아웃 성공")
            }.onFailure { e ->
                Log.e("DeleteAccountViewModel", "구글 로그아웃 실패", e)
            }

            Log.i("DeleteAccountViewModel", "로컬 데이터 정리 완료")
        }.onFailure { e ->
            Log.e("DeleteAccountViewModel", "로컬 데이터 정리 중 오류", e)
        }
    }

    private suspend fun unlinkKakaoAccount() = suspendCancellableCoroutine<Unit> { continuation ->
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                continuation.resume(Unit) // 실패해도 정상적으로 완료 처리
                Log.w("DeleteAccountViewModel", "카카오 연결 해제 실패하지만 계속 진행", error)
            } else {
                continuation.resume(Unit)
            }
        }
    }
}