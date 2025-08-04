package com.texthip.thip.ui.search.screen

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.texthip.thip.R
import com.texthip.thip.ui.common.forms.SearchBookTextField
import com.texthip.thip.ui.common.topappbar.LeftNameTopAppBar
import com.texthip.thip.ui.search.component.SearchActiveField
import com.texthip.thip.ui.search.component.SearchBookFilteredResult
import com.texthip.thip.ui.search.component.SearchEmptyResult
import com.texthip.thip.ui.search.component.SearchRecentBook
import com.texthip.thip.ui.search.mock.BookData
import com.texthip.thip.ui.theme.ThipTheme
import kotlinx.serialization.json.Json
import androidx.core.content.edit
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

@Composable
fun SearchBookScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null,
    bookList: List<BookData> = emptyList(),
    popularBooks: List<BookData> = emptyList()
) {
    val context = LocalContext.current
    val sharedPrefs = remember { 
        context.getSharedPreferences("book_search_prefs", Context.MODE_PRIVATE) 
    }

    var recentSearches by remember {
        mutableStateOf(
            try {
                val jsonString = sharedPrefs.getString("recent_book_searches", "[]") ?: "[]"
                Json.decodeFromString<List<String>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        )
    }

    fun saveRecentSearches(searches: List<String>) {
        try {
            val jsonString = Json.encodeToString(ListSerializer(String.serializer()), searches)
            sharedPrefs.edit {
                putString("recent_book_searches", jsonString)
            }
            recentSearches = searches
        } catch (e: Exception) {
            recentSearches = emptyList()
        }
    }
    var searchText by rememberSaveable { mutableStateOf("") }
    var isSearched by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val liveFilteredBookList by remember(searchText) {
        derivedStateOf {
            if (searchText.isBlank()) emptyList() else
                bookList.filter { book ->
                    book.title.contains(searchText, ignoreCase = true) ||
                            book.author.contains(searchText, ignoreCase = true) ||
                            book.publisher.contains(searchText, ignoreCase = true)
                }
        }
    }

    val filteredBookList by remember(searchText, isSearched) {
        derivedStateOf {
            if (!isSearched) emptyList()
            else {
                bookList.filter { book ->
                    searchText.isBlank() ||
                            book.title.contains(searchText, ignoreCase = true) ||
                            book.author.contains(searchText, ignoreCase = true) ||
                            book.publisher.contains(searchText, ignoreCase = true)
                }
            }
        }
    }

    LaunchedEffect(isSearched) {
        if (isSearched) {
            focusManager.clearFocus()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LeftNameTopAppBar(
                title = stringResource(R.string.book_search_topappbar)
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
                    text = searchText,
                    onValueChange = {
                        searchText = it
                        isSearched = false
                    },
                    onSearch = { query ->
                        if (query.isNotBlank() && !recentSearches.contains(query)) {
                            val newSearches = listOf(query) + recentSearches.take(9) // 최대 10개 유지
                            saveRecentSearches(newSearches)
                        }
                        isSearched = true
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                when {
                    searchText.isBlank() && !isSearched -> {
                        SearchRecentBook(
                            recentSearches = recentSearches,
                            popularBooks = popularBooks,
                            popularBookDate = "01.12", // TODO: 서버로 날짜를 받아 오게 수정
                            onSearchClick = { keyword ->
                                searchText = keyword
                                isSearched = true
                            },
                            onRemove = { keyword ->
                                val updatedSearches = recentSearches.filterNot { it == keyword }
                                saveRecentSearches(updatedSearches)
                            },
                            onBookClick = { book ->
                                // 책 클릭 시 처리
                            }
                        )
                    }

                    searchText.isNotBlank() && !isSearched -> {
                        if (liveFilteredBookList.isEmpty()) {
                            SearchEmptyResult(
                                mainText = stringResource(R.string.book_no_search_result1),
                                subText = stringResource(R.string.book_no_search_result2),
                                onRequestBook = { /*책 요청 처리*/ }
                            )
                        } else {
                            SearchActiveField(
                                bookList = liveFilteredBookList
                            )
                        }
                    }

                    isSearched -> {
                        SearchBookFilteredResult(
                            resultCount = filteredBookList.size,
                            bookList = filteredBookList,
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewBookSearchScreen_Default() {
    ThipTheme {
        SearchBookScreen(
            bookList = listOf(
                BookData("aaa", "리처드 도킨스", "을유문화사", R.drawable.bookcover_sample),
                BookData("abc", "마틴 셀리그만", "물푸레", R.drawable.bookcover_sample),
                BookData("abcd", "빅터 프랭클", "청림출판", R.drawable.bookcover_sample),
                BookData("abcde", "칼 융", "문학과지성사", R.drawable.bookcover_sample),
                BookData("abcdef", "에릭 프롬", "까치글방", R.drawable.bookcover_sample),
                BookData("abcedfg", "알베르 카뮈", "민음사", R.drawable.bookcover_sample),
                BookData("abcdefgh", "장 폴 사르트르", "문학동네", R.drawable.bookcover_sample),
            ),
            popularBooks = listOf(
                BookData("단 한번의 삶", "리처드 도킨스", "을유문화사", R.drawable.bookcover_sample),
                BookData("사랑", "마틴 셀리그만", "물푸레", R.drawable.bookcover_sample),
                BookData("호모 사피엔스", "빅터 프랭클", "청림출판", R.drawable.bookcover_sample),
                BookData("코스모스 실버", "칼 융", "문학과지성사", R.drawable.bookcover_sample),
                BookData("오만과 편견", "에릭 프롬", "까치글방", R.drawable.bookcover_sample),
            )
        )
    }
}

@Preview
@Composable
fun PreviewBookSearchScreen_EmptyPopular() {
    ThipTheme {
        SearchBookScreen(
            bookList = listOf(
                BookData("aaa", "리처드 도킨스", "을유문화사", R.drawable.bookcover_sample),
                BookData("abc", "마틴 셀리그만", "물푸레", R.drawable.bookcover_sample),
                BookData("abcd", "빅터 프랭클", "청림출판", R.drawable.bookcover_sample),
                BookData("abcde", "칼 융", "문학과지성사", R.drawable.bookcover_sample),
                BookData("abcdef", "에릭 프롬", "까치글방", R.drawable.bookcover_sample),
                BookData("abcedfg", "알베르 카뮈", "민음사", R.drawable.bookcover_sample),
                BookData("abcdefgh", "장 폴 사르트르", "문학동네", R.drawable.bookcover_sample),
            ),
            popularBooks = emptyList()
        )
    }
}
