package com.texthip.thip.ui.group.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.texthip.thip.data.manager.Genre
import com.texthip.thip.data.model.book.response.RecentSearchItem
import com.texthip.thip.data.model.rooms.response.SearchRoomItem
import com.texthip.thip.data.repository.RecentSearchRepository
import com.texthip.thip.data.repository.RoomsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupSearchUiState(
    val searchQuery: String = "",

    // 상태 관리 단순화 - boolean 필드 사용
    val isInitial: Boolean = true,
    val isLiveSearching: Boolean = false,
    val isCompleteSearching: Boolean = false,
    val isAllCategory: Boolean = false,

    // 검색 결과 및 데이터
    val searchResults: List<SearchRoomItem> = emptyList(),
    val recentSearches: List<RecentSearchItem> = emptyList(),
    val genres: List<Genre> = emptyList(),

    // 필터링 상태
    val selectedGenre: Genre? = null,
    val selectedSort: String = "deadline", // "deadline" 또는 "memberCount"

    // 로딩 상태
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,

    // 페이징 정보
    val nextCursor: String? = null,
    val hasMore: Boolean = true,

    // 에러/토스트
    val error: String? = null,
    val showToast: Boolean = false,
    val toastMessage: String = ""
) {
    val hasResults: Boolean get() = searchResults.isNotEmpty()
    val canLoadMore: Boolean get() = hasMore && !isSearching && !isLoadingMore
    val showEmptyState: Boolean get() = (isCompleteSearching || isAllCategory) && searchResults.isEmpty() && !isSearching
}

@HiltViewModel
class GroupSearchViewModel @Inject constructor(
    private val roomsRepository: RoomsRepository,
    private val recentSearchRepository: RecentSearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupSearchUiState())
    val uiState: StateFlow<GroupSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null

    // Map 기반 빠른 최근 검색어 관리
    private val recentSearchMap = mutableMapOf<String, RecentSearchItem>()

    init {
        loadInitialData()
    }

    private fun updateState(update: (GroupSearchUiState) -> GroupSearchUiState) {
        _uiState.update(update)
    }

    private fun loadInitialData() {
        loadGenres()
        loadRecentSearches()
    }

    private fun loadGenres() {
        viewModelScope.launch {
            roomsRepository.getGenres()
                .onSuccess { genres ->
                    updateState {
                        it.copy(
                            genres = genres,
                            selectedGenre = null  // 기본적으로 아무 장르도 선택하지 않음
                        )
                    }
                }
                .onFailure {
                    // 장르 로딩 실패는 조용히 처리
                }
        }
    }

    fun updateSearchQuery(query: String) {
        updateState { it.copy(searchQuery = query) }
        searchJob?.cancel()
        loadMoreJob?.cancel()

        // 공백도 검색 가능하도록 수정 (빈 문자열만 제외)
        if (query.isNotEmpty()) {
            updateState {
                it.copy(
                    isInitial = false,
                    isLiveSearching = true,
                    isCompleteSearching = false,
                    isAllCategory = false
                )
            }
            searchJob = viewModelScope.launch {
                delay(300)
                performSearch(query, isLiveSearch = true)
            }
        } else {
            clearSearchResults()
        }
    }

    fun onSearchButtonClick() {
        val query = uiState.value.searchQuery.trim()
        searchJob?.cancel()
        loadMoreJob?.cancel()

        // 검색어가 비어있으면 '전체 모임방' 검색 실행
        if (query.isEmpty()) {
            onViewAllRooms()
        } else {
            // 검색어가 있으면 기존 검색 로직 실행
            updateState {
                it.copy(
                    isInitial = false,
                    isLiveSearching = false,
                    isCompleteSearching = true,
                    isAllCategory = false
                )
            }
            viewModelScope.launch {
                performSearch(query, isLiveSearch = false)
                loadRecentSearches()
            }
        }
    }

    fun onViewAllRooms() {
        searchJob?.cancel()
        loadMoreJob?.cancel()
        updateState {
            it.copy(
                searchQuery = "",
                isInitial = false,
                isLiveSearching = false,
                isCompleteSearching = false,
                isAllCategory = true
            )
        }
        performSearchWithCurrentQuery()
    }

    fun updateSelectedGenre(genre: Genre?) {
        updateState { it.copy(selectedGenre = genre) }
        // 필터 변경 시 새로운 검색 수행 (공백도 허용)
        if (uiState.value.isCompleteSearching || uiState.value.isAllCategory) {
            performSearchWithCurrentQuery()
        }
    }

    fun updateSortType(sort: String) {
        updateState { it.copy(selectedSort = sort) }
        // 정렬 변경 시 새로운 검색 수행 (공백도 허용)
        if (uiState.value.isCompleteSearching || uiState.value.isAllCategory) {
            performSearchWithCurrentQuery()
        }
    }

    private fun performSearchWithCurrentQuery() {
        searchJob?.cancel()
        loadMoreJob?.cancel()

        searchJob = viewModelScope.launch {
            val currentState = uiState.value
            performSearch(
                query = currentState.searchQuery,
                isLiveSearch = currentState.isLiveSearching,
                isFinalized = !currentState.isLiveSearching || currentState.isAllCategory
            )
        }
    }

    fun loadMoreRooms() {
        val currentState = uiState.value
        if (currentState.canLoadMore) {
            loadMoreJob?.cancel()
            loadMoreJob = viewModelScope.launch {
                performLoadMore()
            }
        }
    }

    private suspend fun performSearch(query: String, isLiveSearch: Boolean, isFinalized: Boolean = !isLiveSearch) {
        updateState {
            it.copy(
                isSearching = true,
                error = null,
                searchResults = emptyList(),
                nextCursor = null,
                hasMore = true
            )
        }

        val currentState = uiState.value
        val category = currentState.selectedGenre?.apiCategory ?: ""

        roomsRepository.searchRooms(
            keyword = query,
            category = category,
            isAllCategory = currentState.isAllCategory,
            sort = currentState.selectedSort,
            isFinalized = isFinalized,
            cursor = null
        )
            .onSuccess { response ->
                response?.let { searchResponse ->
                    updateState {
                        it.copy(
                            searchResults = searchResponse.roomList,
                            nextCursor = searchResponse.nextCursor,
                            hasMore = !searchResponse.isLast,
                            isSearching = false,
                            error = null
                        )
                    }
                } ?: run {
                    updateState {
                        it.copy(
                            searchResults = emptyList(),
                            isSearching = false,
                            isLiveSearching = isLiveSearch,
                            isCompleteSearching = !isLiveSearch,
                            hasMore = false,
                            error = if (isLiveSearch) null else "검색 결과를 불러올 수 없습니다."
                        )
                    }
                }
            }
            .onFailure { throwable ->
                updateState {
                    it.copy(
                        searchResults = emptyList(),
                        isSearching = false,
                        isLiveSearching = isLiveSearch,
                        isCompleteSearching = !isLiveSearch,
                        error = if (isLiveSearch) null else (throwable.message
                            ?: "검색 중 오류가 발생했습니다.")
                    )
                }
            }
    }

    private suspend fun performLoadMore() {
        val currentState = uiState.value

        updateState { it.copy(isLoadingMore = true) }

        val category = currentState.selectedGenre?.apiCategory ?: ""
        roomsRepository.searchRooms(
            keyword = currentState.searchQuery,
            category = category,
            isAllCategory = currentState.isAllCategory,
            sort = currentState.selectedSort,
            isFinalized = currentState.isCompleteSearching || currentState.isAllCategory,
            cursor = currentState.nextCursor
        )
            .onSuccess { response ->
                response?.let { searchResponse ->
                    updateState {
                        it.copy(
                            searchResults = it.searchResults + searchResponse.roomList,
                            nextCursor = searchResponse.nextCursor,
                            hasMore = !searchResponse.isLast,
                            isLoadingMore = false,
                            error = null
                        )
                    }
                } ?: run {
                    updateState {
                        it.copy(
                            isLoadingMore = false,
                            hasMore = false,
                            error = "추가 결과를 불러올 수 없습니다."
                        )
                    }
                }
            }
            .onFailure { throwable ->
                updateState {
                    it.copy(
                        isLoadingMore = false,
                        error = throwable.message ?: "추가 결과를 불러오는 중 오류가 발생했습니다."
                    )
                }
            }
    }

    fun loadRecentSearches() {
        viewModelScope.launch {
            recentSearchRepository.getRecentSearches("ROOM")
                .onSuccess { response ->
                    response?.let { recentSearchResponse ->
                        // Map에 최근 검색어 저장 (빠른 검색을 위해)
                        recentSearchMap.clear()
                        recentSearchResponse.recentSearchList.forEach { item ->
                            recentSearchMap[item.searchTerm] = item
                        }

                        updateState {
                            it.copy(recentSearches = recentSearchResponse.recentSearchList)
                        }
                    }
                }
                .onFailure {
                    // 최근 검색어 로딩 실패는 조용히 처리
                }
        }
    }

    fun deleteRecentSearch(recentSearchId: Int) {
        viewModelScope.launch {
            recentSearchRepository.deleteRecentSearch(recentSearchId)
                .onSuccess {
                    loadRecentSearches() // 삭제 성공 시 목록 새로고침
                }
                .onFailure {
                    // 삭제 실패는 조용히 처리
                }
        }
    }

    /** 키워드로 빠른 최근 검색어 삭제 (Map 기반) */
    fun deleteRecentSearchByKeyword(keyword: String) {
        recentSearchMap[keyword]?.let { recentSearchItem ->
            deleteRecentSearch(recentSearchItem.recentSearchId)
        }
    }

    private fun clearSearchResults() {
        searchJob?.cancel()
        loadMoreJob?.cancel()
        updateState {
            it.copy(
                searchQuery = "",
                isInitial = true,
                isLiveSearching = false,
                isCompleteSearching = false,
                searchResults = emptyList(),
                nextCursor = null,
                hasMore = true,
                isSearching = false,
                isLoadingMore = false,
                error = null
            )
        }
    }

    fun refreshData() {
        loadInitialData()
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        loadMoreJob?.cancel()
    }
}