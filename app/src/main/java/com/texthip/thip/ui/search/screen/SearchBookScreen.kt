package com.texthip.thip.ui.search.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.texthip.thip.R
import com.texthip.thip.ui.common.forms.SearchBookTextField
import com.texthip.thip.ui.common.topappbar.LeftNameTopAppBar
import com.texthip.thip.ui.search.component.SearchActiveField
import com.texthip.thip.ui.search.component.SearchBookFilteredResult
import com.texthip.thip.ui.search.component.SearchEmptyResult
import com.texthip.thip.ui.search.component.SearchRecentBook
import com.texthip.thip.ui.search.mock.BookData
import com.texthip.thip.ui.search.viewmodel.SearchBookViewModel
import com.texthip.thip.ui.theme.ThipTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SearchBookScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchBookViewModel = hiltViewModel(),
    onBookClick: (String) -> Unit = {},
    onRequestBook: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshData()
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SearchBookScreenContent(
        modifier = modifier,
        searchQuery = uiState.searchQuery,
        isInitial = uiState.isInitial,
        isLiveSearching = uiState.isLiveSearching,
        isCompleteSearching = uiState.isCompleteSearching,
        searchResults = uiState.searchResults.map { item ->
            BookData(
                title = item.title,
                author = item.authorName,
                publisher = item.publisher,
                imageUrl = item.imageUrl,
                isbn = item.isbn
            )
        },
        popularBooks = uiState.popularBooks.map { item ->
            BookData(
                title = item.title,
                author = "",
                publisher = "",
                imageUrl = item.imageUrl,
                isbn = item.isbn
            )
        },
        recentSearches = uiState.recentSearches.map { it.searchTerm },
        totalElements = uiState.totalElements,
        isSearching = uiState.isSearching,
        isLoadingMore = uiState.isLoadingMore,
        canLoadMore = uiState.canLoadMore,
        hasResults = uiState.hasResults,
        showEmptyState = uiState.showEmptyState,
        onSearchQueryChange = { query ->
            viewModel.updateSearchQuery(query)
        },
        onSearchClick = {
            viewModel.onSearchButtonClick()
        },
        onRecentSearchClick = { keyword ->
            viewModel.updateSearchQuery(keyword)
            viewModel.onSearchButtonClick()
        },
        onRemoveRecentSearch = { keyword ->
            viewModel.deleteRecentSearchByKeyword(keyword)
        },
        onBookClick = { book ->
            onBookClick(book.isbn)
        },
        onLoadMore = {
            viewModel.loadMoreBooks()
        },
        onRequestBook = onRequestBook
    )
}

@Composable
private fun SearchBookScreenContent(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    isInitial: Boolean = true,
    isLiveSearching: Boolean = false,
    isCompleteSearching: Boolean = false,
    searchResults: List<BookData> = emptyList(),
    popularBooks: List<BookData> = emptyList(),
    recentSearches: List<String> = emptyList(),
    totalElements: Int = 0,
    isSearching: Boolean = false,
    isLoadingMore: Boolean = false,
    canLoadMore: Boolean = true,
    hasResults: Boolean = false,
    showEmptyState: Boolean = false,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onRecentSearchClick: (String) -> Unit = {},
    onRemoveRecentSearch: (String) -> Unit = {},
    onBookClick: (BookData) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onRequestBook: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LeftNameTopAppBar(
                title = stringResource(R.string.nav_search)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                SearchBookTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    hint = stringResource(R.string.book_search_hint),
                    text = searchQuery,
                    onValueChange = onSearchQueryChange,
                    onSearch = { 
                        onSearchClick()
                        focusManager.clearFocus()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isInitial) {
                    SearchRecentBook(
                        recentSearches = recentSearches,
                        popularBooks = popularBooks,
                        popularBookDate = SimpleDateFormat("MM.dd", Locale.getDefault()).format(Date()),
                        onSearchClick = onRecentSearchClick,
                        onRemove = onRemoveRecentSearch,
                        onBookClick = onBookClick
                    )
                } else if (isLiveSearching) {
                    if (hasResults) {
                        SearchActiveField(
                            bookList = searchResults,
                            isLoading = isSearching || isLoadingMore,
                            hasMore = canLoadMore,
                            onLoadMore = onLoadMore,
                            onBookClick = onBookClick
                        )
                    } else if (showEmptyState) {
                        SearchEmptyResult(
                            mainText = stringResource(R.string.book_no_search_result1),
                            subText = stringResource(R.string.book_no_search_result2),
                            onRequestBook = onRequestBook
                        )
                    }
                } else if (isCompleteSearching) {
                    if (hasResults) {
                        SearchBookFilteredResult(
                            resultCount = totalElements,
                            bookList = searchResults,
                            isLoading = isSearching || isLoadingMore,
                            hasMore = canLoadMore,
                            onLoadMore = onLoadMore,
                            onBookClick = onBookClick
                        )
                    } else if (showEmptyState) {
                        SearchEmptyResult(
                            mainText = stringResource(R.string.book_no_search_result1),
                            subText = stringResource(R.string.book_no_search_result2),
                            onRequestBook = onRequestBook
                        )
                    }
                }
            }
        }
    }
}


// Preview용 Mock 데이터
private val mockPopularBooks = listOf(
    BookData(
        title = "데미안",
        author = "헤르만 헤세",
        publisher = "민음사",
        imageUrl = "https://example.com/demian.jpg",
        isbn = "9788954682152"
    ),
    BookData(
        title = "1984",
        author = "조지 오웰", 
        publisher = "민음사",
        imageUrl = "https://example.com/1984.jpg",
        isbn = "9788954682153"
    ),
    BookData(
        title = "어린왕자",
        author = "생텍쥐페리",
        publisher = "문예출판사",
        imageUrl = "https://example.com/prince.jpg",
        isbn = "9788954682154"
    )
)

private val mockSearchResults = listOf(
    BookData(
        title = "데미안",
        author = "헤르만 헤세",
        publisher = "민음사",
        imageUrl = "https://example.com/demian.jpg",
        isbn = "9788954682152"
    ),
    BookData(
        title = "데미안 읽기의 즐거움",
        author = "김철수",
        publisher = "문학동네",
        imageUrl = "https://example.com/demian2.jpg",
        isbn = "9788954682155"
    ),
    BookData(
        title = "헤르만 헤세의 데미안 해설서",
        author = "이영희",
        publisher = "해냄출판사",
        imageUrl = "https://example.com/demian3.jpg",
        isbn = "9788954682156"
    )
)

private val mockRecentSearches = listOf("데미안", "1984", "어린왕자", "카프카", "괴테")

@Preview(showBackground = true)
@Composable
fun SearchBookScreenContentInitialPreview() {
    ThipTheme {
        SearchBookScreenContent(
            isInitial = true,
            popularBooks = mockPopularBooks,
            recentSearches = mockRecentSearches
        )
    }
}

@Preview(showBackground = true) 
@Composable
fun SearchBookScreenContentLiveSearchPreview() {
    ThipTheme {
        SearchBookScreenContent(
            searchQuery = "데미안",
            isInitial = false,
            isLiveSearching = true,
            searchResults = mockSearchResults,
            hasResults = true,
            isSearching = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBookScreenContentCompleteSearchPreview() {
    ThipTheme {
        SearchBookScreenContent(
            searchQuery = "데미안",
            isInitial = false,
            isCompleteSearching = true,
            searchResults = mockSearchResults,
            totalElements = 15,
            hasResults = true,
            isSearching = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBookScreenContentEmptyPreview() {
    ThipTheme {
        SearchBookScreenContent(
            searchQuery = "없는책제목",
            isInitial = false,
            isCompleteSearching = true,
            searchResults = emptyList(),
            hasResults = false,
            showEmptyState = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBookScreenContentLoadingPreview() {
    ThipTheme {
        SearchBookScreenContent(
            searchQuery = "데미안",
            isInitial = false,
            isCompleteSearching = true,
            searchResults = mockSearchResults.take(2),
            totalElements = 15,
            hasResults = true,
            isSearching = false,
            isLoadingMore = true,
            canLoadMore = true
        )
    }
}
