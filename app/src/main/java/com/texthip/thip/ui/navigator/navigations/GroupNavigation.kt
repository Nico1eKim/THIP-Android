package com.texthip.thip.ui.navigator.navigations

import android.annotation.SuppressLint
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.texthip.thip.ui.feed.viewmodel.FeedViewModel
import com.texthip.thip.ui.group.makeroom.screen.GroupMakeRoomScreen
import com.texthip.thip.ui.group.makeroom.viewmodel.GroupMakeRoomViewModel
import com.texthip.thip.ui.group.myroom.mock.RoomType
import com.texthip.thip.ui.group.myroom.screen.GroupMyScreen
import com.texthip.thip.ui.group.myroom.viewmodel.GroupMyViewModel
import com.texthip.thip.ui.group.note.screen.GroupNoteAiScreen
import com.texthip.thip.ui.group.note.screen.GroupNoteCreateScreen
import com.texthip.thip.ui.group.note.screen.GroupNoteScreen
import com.texthip.thip.ui.group.note.screen.GroupVoteCreateScreen
import com.texthip.thip.ui.group.note.viewmodel.GroupNoteViewModel
import com.texthip.thip.ui.group.room.screen.GroupRoomChatScreen
import com.texthip.thip.ui.group.room.screen.GroupRoomMatesScreen
import com.texthip.thip.ui.group.room.screen.GroupRoomRecruitScreen
import com.texthip.thip.ui.group.room.screen.GroupRoomScreen
import com.texthip.thip.ui.group.room.screen.GroupRoomUnlockScreen
import com.texthip.thip.ui.group.room.viewmodel.GroupRoomRecruitViewModel
import com.texthip.thip.ui.group.screen.GroupScreen
import com.texthip.thip.ui.group.search.screen.GroupSearchScreen
import com.texthip.thip.ui.group.search.viewmodel.GroupSearchViewModel
import com.texthip.thip.ui.group.viewmodel.GroupViewModel
import com.texthip.thip.ui.navigator.extensions.navigateToAlarm
import com.texthip.thip.ui.navigator.extensions.navigateToBookDetail
import com.texthip.thip.ui.navigator.extensions.navigateToFeedWrite
import com.texthip.thip.ui.navigator.extensions.navigateToGroupMakeRoom
import com.texthip.thip.ui.navigator.extensions.navigateToGroupMy
import com.texthip.thip.ui.navigator.extensions.navigateToGroupNote
import com.texthip.thip.ui.navigator.extensions.navigateToGroupNoteAi
import com.texthip.thip.ui.navigator.extensions.navigateToGroupNoteCreate
import com.texthip.thip.ui.navigator.extensions.navigateToGroupRecruit
import com.texthip.thip.ui.navigator.extensions.navigateToGroupRoom
import com.texthip.thip.ui.navigator.extensions.navigateToGroupRoomChat
import com.texthip.thip.ui.navigator.extensions.navigateToGroupRoomMates
import com.texthip.thip.ui.navigator.extensions.navigateToGroupRoomUnlock
import com.texthip.thip.ui.navigator.extensions.navigateToGroupSearch
import com.texthip.thip.ui.navigator.extensions.navigateToGroupVoteCreate
import com.texthip.thip.ui.navigator.extensions.navigateToRecommendedGroupRecruit
import com.texthip.thip.ui.navigator.routes.FeedRoutes
import com.texthip.thip.ui.navigator.routes.GroupRoutes
import com.texthip.thip.ui.navigator.routes.MainTabRoutes

private const val PARTICIPATION_APPROVED_KEY = "participation_approved_key"

@SuppressLint("UnrememberedGetBackStackEntry")
fun NavGraphBuilder.groupNavigation(
    navController: NavHostController,
    navigateBack: () -> Unit
) {
    // 메인 Group 화면
    composable<MainTabRoutes.Group> { backStackEntry ->
        val groupViewModel: GroupViewModel = hiltViewModel()

        // 네비게이션 파라미터로 전달된 토스트 메시지가 있는지 확인
        LaunchedEffect(backStackEntry) {
            val toastMessage = backStackEntry.savedStateHandle.get<String>("toast_message")

            toastMessage?.let { message ->
                backStackEntry.savedStateHandle.remove<String>("toast_message")
                groupViewModel.showToastMessage(message)
            }
        }

        GroupScreen(
            viewModel = groupViewModel,
            onNavigateToMakeRoom = {
                navController.navigateToGroupMakeRoom()
            },
            onNavigateToAlarm = {
                navController.navigateToAlarm()
            },
            onNavigateToGroupSearch = {
                navController.navigateToGroupSearch(viewAll = false)
            },
            onNavigateToGroupMy = {
                navController.navigateToGroupMy()
            },
            onNavigateToGroupRecruit = { roomId ->
                navController.navigateToGroupRecruit(roomId)
            },
            onNavigateToGroupRoom = { roomId ->
                navController.navigateToGroupRoom(roomId)
            },
            onNavigateToGroupSearchAllRooms = {
                navController.navigateToGroupSearch(viewAll = true)
            }
        )
    }

    // Group MakeRoom 화면
    composable<GroupRoutes.MakeRoom> {
        val viewModel: GroupMakeRoomViewModel = hiltViewModel()
        GroupMakeRoomScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navigateBack()
            },
            onGroupCreated = { roomId ->
                navController.navigate(GroupRoutes.Recruit(roomId)) {
                    popUpTo<GroupRoutes.MakeRoom> { inclusive = true }
                }
            }
        )
    }

    // Group MakeRoom 화면 (책 정보 미리 선택됨)
    composable<GroupRoutes.MakeRoomWithBook> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.MakeRoomWithBook>()
        val viewModel: GroupMakeRoomViewModel = hiltViewModel()

        // 책 정보를 ViewModel에 미리 설정
        LaunchedEffect(route) {
            viewModel.setPreselectedBook(
                isbn = route.isbn,
                title = route.title,
                imageUrl = route.imageUrl,
                author = route.author
            )
        }

        GroupMakeRoomScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navigateBack()
            },
            onGroupCreated = { roomId ->
                // 생성된 방의 모집 화면으로 이동하고 백스택 제거
                navController.navigateToGroupRecruit(roomId)
                // 백스택에서 MakeRoomWithBook 화면 제거
                navController.popBackStack<GroupRoutes.MakeRoomWithBook>(inclusive = true)
            }
        )
    }

    // Group My 화면
    composable<GroupRoutes.My> {
        val groupMyViewModel: GroupMyViewModel = hiltViewModel()

        GroupMyScreen(
            viewModel = groupMyViewModel,
            onCardClick = { room ->
                val isRecruiting = room.type == RoomType.RECRUITING.value
                val isExpired = (room.type == RoomType.EXPIRED.value)

                if (isRecruiting) {
                    navController.navigateToGroupRecruit(room.roomId)
                } else {
                    navController.navigateToGroupRoom(room.roomId, isExpired = isExpired)
                }
            },
            onNavigateBack = {
                navigateBack()
            }
        )
    }

    // Group Search 화면
    composable<GroupRoutes.Search> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.Search>()
        val viewModel: GroupSearchViewModel = hiltViewModel()

        // [추가] 화면 진입 시 viewAll 플래그를 확인하고 ViewModel의 함수를 호출
        LaunchedEffect(Unit) {
            if (route.viewAll) {
                viewModel.onViewAllRooms()
            }
        }

        GroupSearchScreen(
            onNavigateBack = {
                navigateBack()
            },
            onRoomClick = { roomId ->
                navController.navigateToGroupRecruit(roomId)
            },
            viewModel = viewModel
        )
    }

    // Group Recruit 화면
    composable<GroupRoutes.Recruit> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.Recruit>()
        val roomId = route.roomId
        val viewModel: GroupRoomRecruitViewModel = hiltViewModel()

        val participationApproved by backStackEntry.savedStateHandle
            .getStateFlow(PARTICIPATION_APPROVED_KEY, false)
            .collectAsState()

        LaunchedEffect(participationApproved) {
            if (participationApproved) {
                viewModel.onParticipationClick()
                backStackEntry.savedStateHandle[PARTICIPATION_APPROVED_KEY] = false
            }
        }

        GroupRoomRecruitScreen(
            roomId = roomId,
            onRecommendationClick = { recommendation ->
                navController.navigateToRecommendedGroupRecruit(recommendation.roomId)
            },
            onNavigateToGroupScreen = { toastMessage ->
                // GroupScreen에 토스트 메시지 전달
                val groupEntry = navController.getBackStackEntry(MainTabRoutes.Group)
                groupEntry.savedStateHandle["toast_message"] = toastMessage
                navController.popBackStack(MainTabRoutes.Group, false)
            },
            onBackClick = {
                // MakeRoom에서 바로 온 경우를 확인하여 Group 홈으로 이동
                val canGoBack = navController.previousBackStackEntry != null
                if (canGoBack) {
                    navigateBack()
                } else {
                    // 백스택이 비어있으면 Group 홈으로 이동 (방금 생성된 방의 경우)
                    navController.popBackStack(MainTabRoutes.Group, false)
                }
            },
            onBookDetailClick = { isbn ->
                navController.navigateToBookDetail(isbn)
            },
            onNavigateToPasswordScreen = { roomId ->
                navController.navigateToGroupRoomUnlock(roomId)
            },
            onNavigateToRoomPlayingScreen = { roomId ->
                navController.navigateToGroupRoom(roomId)
            }
        )
    }

    // Group Room Unlock 화면 (비밀번호 입력)
    composable<GroupRoutes.RoomUnlock> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.RoomUnlock>()
        val roomId = route.roomId

        GroupRoomUnlockScreen(
            roomId = roomId,
            onBackClick = {
                navigateBack()
            },
            onSuccessNavigation = {
                // 비밀번호가 맞았다는 '신호'만 이전 화면에 전달
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(PARTICIPATION_APPROVED_KEY, true)
                navigateBack()
            }
        )
    }

    // Group Room 화면
    composable<GroupRoutes.Room> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.Room>()
        val roomId = route.roomId
        val isExpired = route.isExpired

        GroupRoomScreen(
            roomId = roomId,
            isExpired = isExpired,
            onBackClick = {
                navigateBack()
            },
            onNavigateToMates = {
                navController.navigateToGroupRoomMates(roomId)
            },
            onNavigateToChat = {
                navController.navigateToGroupRoomChat(roomId, isExpired)
            },
            onNavigateToNote = { page, isOverview ->
                navController.navigateToGroupNote(roomId, page, isOverview, isExpired)
            },
            onNavigateToBookDetail = { isbn ->
                navController.navigateToBookDetail(isbn)
            }
        )
    }

    // Group Room Mates 화면
    composable<GroupRoutes.RoomMates> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.RoomMates>()
        val roomId = route.roomId

        val feedViewModel: FeedViewModel =
            hiltViewModel(navController.getBackStackEntry(MainTabRoutes.Group))
        val feedUiState by feedViewModel.uiState.collectAsState()
        val myUserId = feedUiState.myFeedInfo?.creatorId

        LaunchedEffect(Unit) {
            if (feedUiState.myFeedInfo == null) {
                feedViewModel.onTabSelected(1)
            }
        }

        GroupRoomMatesScreen(
            roomId = roomId,
            onBackClick = {
                navigateBack()
            },
            onUserClick = { userId ->
                if (myUserId != null && myUserId == userId) {
                    navController.navigate(FeedRoutes.My)
                } else {
                    navController.navigate(FeedRoutes.Others(userId))
                }
            }
        )
    }

    composable<GroupRoutes.RoomChat> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.RoomChat>()

        GroupRoomChatScreen(
            onBackClick = { navigateBack() },
            isExpired = route.isExpired
        )
    }

    // Group Note 화면
    composable<GroupRoutes.Note> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.Note>()
        val roomId = route.roomId
        val page = route.page
        val openComments = route.openComments
        val isOverview = route.isOverview
        val isExpired = route.isExpired
        val postId = route.postId

        val result = backStackEntry.savedStateHandle.get<Int>("selected_tab_index")

        val viewModel: GroupNoteViewModel = hiltViewModel(backStackEntry)

        GroupNoteScreen(
            roomId = roomId,
            resultTabIndex = result,
            initialPage = page,
            initialIsOverview = isOverview,
            isExpired = isExpired,
            initialPostId = postId,
            openComments = openComments,
            onResultConsumed = {
                backStackEntry.savedStateHandle.remove<Int>("selected_tab_index")
            },
            onBackClick = { navigateBack() },
            onCreateNoteClick = { recentPage, totalPage, isOverviewPossible ->
                navController.navigateToGroupNoteCreate(
                    roomId = roomId,
                    recentBookPage = recentPage,
                    totalBookPage = totalPage,
                    isOverviewPossible = isOverviewPossible
                )
            },
            onEditNoteClick = { post ->
                val currentState = viewModel.uiState.value
                navController.navigateToGroupNoteCreate(
                    roomId = roomId,
                    recentBookPage = currentState.recentBookPage,
                    totalBookPage = currentState.totalBookPage,
                    isOverviewPossible = currentState.isOverviewPossible,
                    postId = post.postId,
                    page = post.page,
                    content = post.content,
                    isOverview = post.isOverview
                )
            },
            onEditVoteClick = { post ->
                val currentState = viewModel.uiState.value
                navController.navigateToGroupVoteCreate(
                    roomId = roomId,
                    recentPage = currentState.recentBookPage,
                    totalPage = currentState.totalBookPage,
                    isOverviewPossible = currentState.isOverviewPossible,
                    // 투표 수정 데이터 전달
                    postId = post.postId,
                    page = post.page,
                    isOverview = post.isOverview,
                    title = post.content,
                    options = post.voteItems.map { it.itemName }
                )
            },
            onCreateVoteClick = { recentPage, totalPage, isOverviewPossible ->
                navController.navigateToGroupVoteCreate(
                    roomId = roomId,
                    recentPage = recentPage,
                    totalPage = totalPage,
                    isOverviewPossible = isOverviewPossible
                )
            },
            onNavigateToFeedWrite = { pinInfo, recordContent ->
                navController.navigateToFeedWrite(
                    isbn = pinInfo.isbn,
                    bookTitle = pinInfo.bookTitle,
                    bookAuthor = pinInfo.authorName,
                    bookImageUrl = pinInfo.bookImageUrl,
                    recordContent = recordContent
                )
            },
            onNavigateToUserProfile = { userId ->
                navController.navigate(FeedRoutes.Others(userId))
            },
            onNavigateToMyProfile = {
                navController.navigate(FeedRoutes.My)
            },
            onNavigateToAiReview = {
                navController.navigateToGroupNoteAi(roomId)
            },
            viewModel = viewModel
        )
    }

    // Group Note Create 화면
    composable<GroupRoutes.NoteCreate> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.NoteCreate>()
        val roomId = route.roomId

        GroupNoteCreateScreen(
            roomId = roomId,
            recentPage = route.recentBookPage,
            totalPage = route.totalBookPage,
            isOverviewPossible = route.isOverviewPossible,
            postId = route.postId,
            page = route.page,
            content = route.content,
            isOverview = route.isOverview,
            onBackClick = {
                navigateBack()
            },
            onNavigateBackWithResult = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("selected_tab_index", 1)
                navigateBack()
            }
        )
    }

    composable<GroupRoutes.VoteCreate> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.VoteCreate>()
        val roomId = route.roomId

        GroupVoteCreateScreen(
            roomId = roomId,
            recentPage = route.recentPage,
            totalPage = route.totalPage,
            isOverviewPossible = route.isOverviewPossible,
            postId = route.postId,
            page = route.page,
            isOverview = route.isOverview,
            title = route.title,
            options = route.options,
            onBackClick = { navigateBack() },
            onNavigateBackWithResult = {
                // 투표 생성 후 '내 기록' 탭으로 이동
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("selected_tab_index", 1)
                navigateBack()
            }
        )
    }

    // AI 독후감 스크린
    composable<GroupRoutes.NoteAi> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.NoteAi>()
        GroupNoteAiScreen(
            roomId = route.roomId,
            onBackClick = { navigateBack() }
        )
    }
}