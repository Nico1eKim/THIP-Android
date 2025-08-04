package com.texthip.thip.ui.group.search.screen

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.ui.common.buttons.FilterButton
import com.texthip.thip.ui.common.forms.SearchBookTextField
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.group.myroom.component.GroupRecentSearch
import com.texthip.thip.ui.group.myroom.mock.GroupCardItemRoomData
import com.texthip.thip.ui.group.search.component.GroupEmptyResult
import com.texthip.thip.ui.group.search.component.GroupFilteredSearchResult
import com.texthip.thip.ui.group.search.component.GroupLiveSearchResult
import com.texthip.thip.ui.theme.ThipTheme
import kotlinx.serialization.json.Json
import androidx.core.content.edit
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

@Composable
fun GroupSearchScreen(
    modifier: Modifier = Modifier,
    roomList: List<GroupCardItemRoomData>,
    onNavigateBack: () -> Unit = {},
    onRoomClick: (GroupCardItemRoomData) -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPrefs = remember { 
        context.getSharedPreferences("group_search_prefs", Context.MODE_PRIVATE) 
    }

    var recentSearches by remember {
        mutableStateOf(
            try {
                val jsonString = sharedPrefs.getString("recent_searches", "[]") ?: "[]"
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
                putString("recent_searches", jsonString)
            }
            recentSearches = searches
        } catch (e: Exception) {
            recentSearches = emptyList()
        }
    }
    var searchText by rememberSaveable { mutableStateOf("") }
    var isSearched by rememberSaveable { mutableStateOf(false) }
    var selectedGenreIndex by rememberSaveable { mutableIntStateOf(-1) }
    var selectedSortOptionIndex by rememberSaveable { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val genres = listOf(
        stringResource(R.string.literature),
        stringResource(R.string.science_it),
        stringResource(R.string.social_science),
        stringResource(R.string.humanities),
        stringResource(R.string.art)
    )
    val sortOptions = listOf(
        stringResource(R.string.group_filter_deadline),
        stringResource(R.string.group_filter_popular)
    )

    val liveFilteredRoomList by remember(searchText) {
        derivedStateOf {
            if (searchText.isBlank()) emptyList() else
                roomList.filter { room ->
                    room.title.contains(searchText, ignoreCase = true)
                }
        }
    }

    val filteredRoomList by remember(
        searchText,
        selectedGenreIndex,
        selectedSortOptionIndex,
        isSearched
    ) {
        derivedStateOf {
            if (!isSearched) emptyList()
            else {
                val filtered = roomList.filter { room ->
                    (searchText.isBlank() || room.title.contains(searchText, ignoreCase = true)) &&
                            (selectedGenreIndex == -1 || room.genreIndex == selectedGenreIndex)
                }
                when (selectedSortOptionIndex) {
                    0 -> filtered.sortedBy { it.endDate }             // 마감임박순
                    1 -> filtered.sortedByDescending { it.participants } // 인기순
                    else -> filtered
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
            DefaultTopAppBar(
                title = stringResource(R.string.group_room_search_topappbar),
                onLeftClick = onNavigateBack,
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
                    hint = stringResource(R.string.group_room_search_hint),
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
                        selectedGenreIndex = -1
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                when {
                    searchText.isBlank() && !isSearched && recentSearches.isEmpty() -> {
                        GroupRecentSearch(
                            recentSearches = emptyList(),
                            onSearchClick = {},
                            onRemove = {}
                        )
                    }

                    searchText.isBlank() && !isSearched && recentSearches.isNotEmpty() -> {
                        GroupRecentSearch(
                            recentSearches = recentSearches,
                            onSearchClick = { keyword ->
                                searchText = keyword
                                isSearched = true
                            },
                            onRemove = { keyword ->
                                val updatedSearches = recentSearches.filterNot { it == keyword }
                                saveRecentSearches(updatedSearches)
                            }
                        )
                    }

                    searchText.isNotBlank() && !isSearched -> {
                        if (liveFilteredRoomList.isEmpty()) {
                            GroupEmptyResult(
                                mainText = stringResource(R.string.group_no_search_result1),
                                subText = stringResource(R.string.group_no_search_result2)
                            )
                        } else {
                            GroupLiveSearchResult(
                                roomList = liveFilteredRoomList,
                                onRoomClick = onRoomClick
                            )
                        }
                    }

                    isSearched -> {
                        GroupFilteredSearchResult(
                            genres = genres,
                            selectedGenreIndex = selectedGenreIndex,
                            onGenreSelect = { selectedGenreIndex = it },
                            resultCount = filteredRoomList.size,
                            roomList = filteredRoomList,
                            onRoomClick = onRoomClick
                        )
                    }
                }
            }
        }

        if (isSearched) {
            FilterButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 176.dp, end = 20.dp),
                selectedOption = sortOptions[selectedSortOptionIndex],
                options = sortOptions,
                onOptionSelected = { selected ->
                    selectedSortOptionIndex = sortOptions.indexOf(selected)
                }
            )
        }
    }
}


@Preview
@Composable
fun PreviewGroupSearchScreen() {
    ThipTheme {
        GroupSearchScreen(
            roomList = listOf(
                GroupCardItemRoomData(1, "aaa", 22, 30, true, 3, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(2, "abc", 15, 20, true, 7, R.drawable.bookcover_sample, 1, true),
                GroupCardItemRoomData(3, "abcd", 10, 15, true, 5, R.drawable.bookcover_sample, 2, true),
                GroupCardItemRoomData(4, "abcde", 8, 12, false, 2, R.drawable.bookcover_sample, 3, true),
                GroupCardItemRoomData(5, "abcdef", 18, 25, true, 4, R.drawable.bookcover_sample, 4),
                GroupCardItemRoomData(6, "abcdefg", 12, 20, true, 1, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(7, "abcdefgh", 10, 14, true, 6, R.drawable.bookcover_sample, 1)
            )
        )
    }
}
