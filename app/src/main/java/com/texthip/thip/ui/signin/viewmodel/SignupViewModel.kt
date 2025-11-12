package com.texthip.thip.ui.signin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.texthip.thip.R
import com.texthip.thip.data.manager.FcmTokenManager
import com.texthip.thip.data.manager.TokenManager
import com.texthip.thip.data.model.base.ThipApiFailureException
import com.texthip.thip.data.model.users.request.SignupRequest
import com.texthip.thip.data.repository.UserRepository
import com.texthip.thip.ui.mypage.mock.RoleItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignupUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val isNicknameVerified: Boolean = false,
    val nicknameWarningMessageResId: Int? = null,
    val roleCards: List<RoleItem> = emptyList(),
    val selectedIndex: Int = -1,
    val errorMessage: String? = null,
    val navigateToGenreScreen: Boolean = false,
    val isSignupSuccess: Boolean = false
)

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val fcmTokenManager: FcmTokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState = _uiState.asStateFlow()

    fun onNicknameChange(nickname: String) {
        _uiState.update {
            it.copy(
                nickname = nickname,
                isNicknameVerified = false, // 닉네임이 바뀌면 인증 상태 초기화
                nicknameWarningMessageResId = null,
                navigateToGenreScreen = false
            )
        }
    }

    fun checkNickname() {
        if (_uiState.value.isLoading || _uiState.value.nickname.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, nicknameWarningMessageResId = null) }
            userRepository.checkNickname(_uiState.value.nickname)
                .onSuccess { response ->
                    if (response?.isVerified == true) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isNicknameVerified = true,
                                navigateToGenreScreen = true
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                nicknameWarningMessageResId = R.string.nickname_warning
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nicknameWarningMessageResId = R.string.error_unknown
                        )
                    }
                }
        }
    }

    fun onNavigatedToGenre() {
        _uiState.update { it.copy(navigateToGenreScreen = false) }
    }

    fun fetchAliasChoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.getAliasChoices()
                .onSuccess { response ->
                    val roleCards = response?.aliasChoices?.map {
                        RoleItem(
                            it.aliasName,
                            it.categoryName,
                            it.imageUrl,
                            it.aliasColor
                        )
                    } ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, roleCards = roleCards) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
        }
    }

    fun selectCard(index: Int) {
        _uiState.update { it.copy(selectedIndex = index) }
    }

    fun signup() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val selectedRole = currentState.roleCards.getOrNull(currentState.selectedIndex)

            if (selectedRole == null || currentState.nickname.isBlank() || !currentState.isNicknameVerified) {
                _uiState.update { it.copy(errorMessage = "닉네임과 역할을 모두 선택해주세요.") }
                return@launch
            }

            val request = SignupRequest(
                nickname = currentState.nickname,
                aliasName = selectedRole.genre
            )

            _uiState.update { it.copy(isLoading = true) }
            userRepository.signup(request)
                .onSuccess { signupResponse ->
                    if (signupResponse != null) {
                        tokenManager.saveToken(signupResponse.accessToken)
                        tokenManager.deleteTempToken()

                        // 회원가입 완료 후 FCM 토큰 전송
                        sendFcmToken()

                        _uiState.update { it.copy(isLoading = false, isSignupSuccess = true) }
                    }
                }
                .onFailure { exception ->
                    Log.e("SignupDebug", "Signup 실패: ", exception)

                    val errorMsg =
                        if (exception is ThipApiFailureException) exception.message else "회원가입에 실패했습니다."
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }
        }
    }

    private fun sendFcmToken() {
        viewModelScope.launch {
            fcmTokenManager.sendCurrentTokenIfExists()
        }
    }
}