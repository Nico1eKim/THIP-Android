package com.texthip.thip.ui.group.note.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.texthip.thip.data.model.rooms.request.RoomsPostsRequestParams
import com.texthip.thip.data.model.rooms.response.PostList
import com.texthip.thip.data.model.rooms.response.RoomsRecordsPinResponse
import com.texthip.thip.data.repository.RoomsRepository
import com.texthip.thip.utils.type.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupNoteUiState(
    // 데이터 로딩 상태
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val isLastPage: Boolean = false,

    // 화면 데이터
    val posts: List<PostList> = emptyList(),
    val recentBookPage: Int = 0,
    val totalBookPage: Int = 0,
    val isOverviewPossible: Boolean = false,
    val recordReviewCount: Int = 0,
    val recordCount: Int = 0,

    // 필터 및 탭 상태
    val selectedTabIndex: Int = 0,
    val selectedSort: SortType = SortType.LATEST,
    val pageStart: String = "",
    val pageEnd: String = "",
    val isOverview: Boolean = false,
    val isPageFilter: Boolean = false,
    val totalEnabled: Boolean = false,

    // 스크롤 관련 상태
    val scrollToPostId: Int? = null
)

sealed interface GroupNoteSideEffect {
    data class NavigateToFeedWrite(
        val pinInfo: RoomsRecordsPinResponse,
        val recordContent: String
    ) : GroupNoteSideEffect
}

sealed interface GroupNoteEvent {
    data class OnTabSelected(val index: Int) : GroupNoteEvent
    data class OnSortSelected(val sortType: SortType) : GroupNoteEvent
    data class OnPageStartChanged(val page: String) : GroupNoteEvent
    data class OnPageEndChanged(val page: String) : GroupNoteEvent
    data class OnOverviewToggled(val isSelected: Boolean) : GroupNoteEvent
    data object ApplyPageFilter : GroupNoteEvent
    data object LoadMorePosts : GroupNoteEvent
    data class OnVote(val postId: Int, val voteItemId: Int, val type: Boolean) : GroupNoteEvent
    data class OnDeleteRecord(val postId: Int, val postType: String) : GroupNoteEvent
    data class OnLikeRecord(val postId: Int, val postType: String) : GroupNoteEvent
    data class OnPinRecord(val recordId: Int, val content: String) : GroupNoteEvent
    data object RefreshPosts : GroupNoteEvent
    data object ClearScrollTarget : GroupNoteEvent
    data object CheckAiUsage : GroupNoteEvent
}


@HiltViewModel
class GroupNoteViewModel @Inject constructor(
    private val roomsRepository: RoomsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupNoteUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<GroupNoteSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    private var nextCursor: String? = null
    private var roomId: Int = -1

    fun initialize(
        roomId: Int,
        initialPage: Int? = null,
        initialIsOverview: Boolean? = null,
        initialPostId: Int? = null
    ) {
        this.roomId = roomId

        if (initialPage != null || initialIsOverview != null) {
            _uiState.update {
                it.copy(
                    pageStart = initialPage?.toString() ?: "",
                    pageEnd = initialPage?.toString() ?: "",
                    isOverview = initialIsOverview ?: false,
                    isPageFilter = initialPage != null
                )
            }
        }

        if (initialPostId != null) {
            _uiState.update {
                it.copy(scrollToPostId = initialPostId)
            }
        }

        refreshAllData()
    }

    private fun loadBookPageInfo() {
        viewModelScope.launch {
            roomsRepository.getRoomsBookPage(roomId)
                .onSuccess { response ->
                    if (response != null) {
                        _uiState.update {
                            it.copy(
                                recentBookPage = response.recentBookPage,
                                totalBookPage = response.totalBookPage,
                                isOverviewPossible = response.isOverviewPossible
                            )
                        }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(error = throwable.message) }
                }
        }
    }

    private fun loadAiUsageInfo() {
        viewModelScope.launch {
            roomsRepository.getRoomsAiUsage(roomId)
                .onSuccess { usageResponse ->
                    _uiState.update {
                        it.copy(
                            recordReviewCount = usageResponse?.recordReviewCount ?: 0,
                            recordCount = usageResponse?.recordCount ?: 0
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(error = throwable.message) }
                }
        }
    }

    private fun refreshAllData() {
        viewModelScope.launch {
            val jobs = listOf(
                async { loadPosts(isRefresh = true) },
                async { loadBookPageInfo() },
                async { loadAiUsageInfo() }
            )
            jobs.awaitAll()
        }
    }

    fun onEvent(event: GroupNoteEvent) {
        when (event) {
            is GroupNoteEvent.OnTabSelected -> {
                _uiState.update { it.copy(selectedTabIndex = event.index) }
                loadPosts(isRefresh = true)
            }

            is GroupNoteEvent.OnSortSelected -> {
                _uiState.update { it.copy(selectedSort = event.sortType) }
                loadPosts(isRefresh = true)
            }

            is GroupNoteEvent.OnPageStartChanged -> _uiState.update { it.copy(pageStart = event.page) }
            is GroupNoteEvent.OnPageEndChanged -> _uiState.update { it.copy(pageEnd = event.page) }
            is GroupNoteEvent.OnOverviewToggled -> {
                _uiState.update { it.copy(isOverview = event.isSelected) }
                loadPosts(isRefresh = true)
            }

            GroupNoteEvent.ApplyPageFilter -> {
                val currentState = _uiState.value
                val isFilterActive =
                    currentState.pageStart.isNotBlank() || currentState.pageEnd.isNotBlank()

                _uiState.update { it.copy(isPageFilter = isFilterActive) }
                loadPosts(isRefresh = true)
            }

            GroupNoteEvent.LoadMorePosts -> loadPosts(isRefresh = false)

            is GroupNoteEvent.OnVote -> vote(
                postId = event.postId,
                voteItemId = event.voteItemId,
                type = event.type
            )

            is GroupNoteEvent.OnDeleteRecord -> deletePost(event.postId, event.postType)
            is GroupNoteEvent.OnLikeRecord -> likeRecord(event.postId, event.postType)
            is GroupNoteEvent.RefreshPosts -> loadPosts(isRefresh = true)
            is GroupNoteEvent.OnPinRecord -> pinRecord(event.recordId, event.content)
            GroupNoteEvent.ClearScrollTarget -> {
                _uiState.update { it.copy(scrollToPostId = null) }
            }
            GroupNoteEvent.CheckAiUsage -> {
                loadAiUsageInfo()
            }
        }
    }

    private fun pinRecord(recordId: Int, content: String) {
        viewModelScope.launch {
            roomsRepository.getRoomsRecordsPin(roomId = roomId, recordId = recordId)
                .onSuccess { pinInfo ->
                    if (pinInfo != null) {
                        _sideEffect.emit(
                            GroupNoteSideEffect.NavigateToFeedWrite(
                                pinInfo = pinInfo,
                                recordContent = content
                            )
                        )
                    }
                }
                .onFailure {
                }
        }
    }

    private fun likeRecord(postId: Int, postType: String) {
        val currentPosts = _uiState.value.posts
        val postIndex = currentPosts.indexOfFirst { it.postId == postId }
        if (postIndex == -1) return

        val oldPost = currentPosts[postIndex]

        val newIsLiked = !oldPost.isLiked
        val newLikeCount = if (newIsLiked) oldPost.likeCount + 1 else oldPost.likeCount - 1

        val optimisticPost = oldPost.copy(
            isLiked = newIsLiked,
            likeCount = newLikeCount.coerceAtLeast(0)
        )

        val newPosts = currentPosts.toMutableList().apply { this[postIndex] = optimisticPost }
        _uiState.update { it.copy(posts = newPosts) }

        viewModelScope.launch {
            roomsRepository.postRoomsPostsLikes(
                postId = postId,
                type = newIsLiked,
                roomPostType = postType
            )
                .onFailure {
                    val rollbackPosts =
                        currentPosts.toMutableList().apply { this[postIndex] = oldPost }
                    _uiState.update { it.copy(posts = rollbackPosts) }
                }
        }
    }

    private fun deletePost(postId: Int, postType: String) {
        viewModelScope.launch {
            val result = when (postType) {
                "RECORD" -> roomsRepository.deleteRoomsRecord(roomId = roomId, recordId = postId)
                "VOTE" -> roomsRepository.deleteRoomsVote(roomId = roomId, voteId = postId)
                else -> Result.failure(IllegalArgumentException("Unknown post type for deletion: $postType"))
            }

            result.onSuccess {
                val updatedPosts = _uiState.value.posts.filter { it.postId != postId }
                _uiState.update { it.copy(posts = updatedPosts) }
            }.onFailure { throwable ->
                _uiState.update { it.copy(error = throwable.message) }
            }
        }
    }

    private fun vote(postId: Int, voteItemId: Int, type: Boolean) {
        val originalPosts = _uiState.value.posts
        val postIndex = originalPosts.indexOfFirst { it.postId == postId }
        if (postIndex == -1) return
        val postToUpdate = originalPosts[postIndex]

        val optimisticVoteItems = postToUpdate.voteItems.map { voteItem ->
            voteItem.copy(isVoted = if (voteItem.voteItemId == voteItemId) type else false)
        }

        val optimisticPosts = originalPosts.toMutableList().apply {
            this[postIndex] = postToUpdate.copy(voteItems = optimisticVoteItems)
        }

        _uiState.update { it.copy(posts = optimisticPosts) }

        viewModelScope.launch {
            roomsRepository.postRoomsVote(
                roomId = roomId,
                voteId = postId,
                voteItemId = voteItemId,
                type = type
            ).onSuccess { voteResponse ->
                if (voteResponse != null) {
                    val serverVoteItems = voteResponse.voteItems

                    // 현재 UI가 가지고 있는 포스트 목록을 가져오기
                    val currentPosts = _uiState.value.posts
                    val postIndex = currentPosts.indexOfFirst { it.postId == postId }
                    if (postIndex == -1) return@onSuccess

                    val postToUpdate = currentPosts[postIndex]

                    // 기존 순서는 유지하고 내용만 업데이트
                    val updatedVoteItems = postToUpdate.voteItems.map { originalItem ->
                        val newItem =
                            serverVoteItems.find { it.voteItemId == originalItem.voteItemId }
                        newItem ?: originalItem
                    }

                    // 순서가 유지된 목록으로 최종 업데이트
                    val finalPosts = currentPosts.toMutableList().apply {
                        this[postIndex] = postToUpdate.copy(voteItems = updatedVoteItems)
                    }
                    _uiState.update { it.copy(posts = finalPosts) }
                } else {
                    loadPosts(isRefresh = true)
                }
            }.onFailure {
                _uiState.update { it.copy(posts = originalPosts) }
            }
        }
    }

    private fun loadPosts(isRefresh: Boolean = false) {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isLoadingMore || (currentState.isLastPage && !isRefresh)) return

        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) it.copy(
                    isLoading = true,
                    posts = emptyList(),
                    error = null,
                    isLastPage = false
                )
                else it.copy(isLoadingMore = true, error = null)
            }

            val cursor = if (isRefresh) null else nextCursor
            val type = if (currentState.selectedTabIndex == 0) "group" else "mine"

            val params = if (type == "mine") {
                // "mine" 탭일 경우 필수 파라미터만 채워 넣음
                RoomsPostsRequestParams(type = type, cursor = cursor)
            } else {
                // "group" 탭일 경우 모든 필터 파라미터 포함
                RoomsPostsRequestParams(
                    type = type,
                    sort = currentState.selectedSort.apiKey,
                    pageStart = currentState.pageStart.toIntOrNull(),
                    pageEnd = currentState.pageEnd.toIntOrNull(),
                    isOverview = currentState.isOverview,
                    isPageFilter = currentState.isPageFilter,
                    cursor = cursor
                )
            }

            roomsRepository.getRoomsPosts(
                roomId = roomId,
                type = params.type,
                sort = params.sort,
                pageStart = params.pageStart,
                pageEnd = params.pageEnd,
                isOverview = params.isOverview,
                isPageFilter = params.isPageFilter,
                cursor = params.cursor
            ).onSuccess { response ->
                if (response != null) {
                    nextCursor = response.nextCursor
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            posts = if (isRefresh) response.postList else it.posts + response.postList,
                            isLastPage = response.isLast,
                            totalEnabled = response.isOverviewEnabled
                        )
                    }
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, isLoadingMore = false, error = throwable.message)
                }
            }
        }
    }
}
