package com.texthip.thip.ui.group.note.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.texthip.thip.data.model.base.BaseResponse
import com.texthip.thip.data.repository.RoomsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class GroupNoteAiUiState(
    val isLoading: Boolean = true,
    val aiReviewText: String? = null,
    val error: String? = null
)

@HiltViewModel
class GroupNoteAiViewModel @Inject constructor(
    private val repository: RoomsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupNoteAiUiState())
    val uiState: StateFlow<GroupNoteAiUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    fun generateAiReview(roomId: Int) {
        if (!_uiState.value.isLoading && _uiState.value.aiReviewText != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.postRoomsAiReview(roomId)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            aiReviewText = response?.content
                        )
                    }
                }
                .onFailure { throwable ->
                    val errorMessage = when (throwable) {
                        is HttpException -> {
                            val errorBody = throwable.response()?.errorBody()?.string()
                            if (errorBody != null) {
                                try {
                                    val errorResponse = json.decodeFromString<BaseResponse<Unit>>(errorBody)
                                    errorResponse.message
                                } catch (e: Exception) {
                                    throwable.message()
                                }
                            } else {
                                throwable.message()
                            }
                        }
                        is IOException -> "네트워크 연결을 확인해주세요."
                        else -> throwable.message ?: "알 수 없는 오류가 발생했습니다."
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
        }
    }
}