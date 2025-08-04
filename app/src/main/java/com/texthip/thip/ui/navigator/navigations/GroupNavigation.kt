package com.texthip.thip.ui.navigator.navigations

import android.annotation.SuppressLint
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.texthip.thip.ui.group.viewmodel.GroupViewModel
import com.texthip.thip.ui.group.makeroom.screen.GroupMakeRoomScreen
import com.texthip.thip.ui.group.makeroom.viewmodel.GroupMakeRoomViewModel
import com.texthip.thip.ui.group.myroom.mock.GroupBottomButtonType
import com.texthip.thip.ui.group.myroom.mock.GroupRoomData
import com.texthip.thip.ui.group.myroom.screen.GroupMyScreen
import com.texthip.thip.ui.group.room.screen.GroupRoomRecruitScreen
import com.texthip.thip.ui.group.room.screen.GroupRoomScreen
import com.texthip.thip.ui.group.screen.GroupDoneScreen
import com.texthip.thip.ui.group.screen.GroupScreen
import com.texthip.thip.ui.group.search.screen.GroupSearchScreen
import com.texthip.thip.ui.navigator.extensions.navigateToAlarm
import com.texthip.thip.ui.navigator.extensions.navigateToGroupDone
import com.texthip.thip.ui.navigator.extensions.navigateToGroupMakeRoom
import com.texthip.thip.ui.navigator.extensions.navigateToGroupMy
import com.texthip.thip.ui.navigator.extensions.navigateToGroupRecruit
import com.texthip.thip.ui.navigator.extensions.navigateToRecommendedGroupRecruit
import com.texthip.thip.ui.navigator.extensions.navigateToGroupRoom
import com.texthip.thip.ui.navigator.extensions.navigateToGroupSearch
import com.texthip.thip.ui.navigator.routes.GroupRoutes
import com.texthip.thip.ui.navigator.routes.MainTabRoutes

// Group
@SuppressLint("UnrememberedGetBackStackEntry")
fun NavGraphBuilder.groupNavigation(
    navController: NavHostController,
    navigateBack: () -> Unit
) {
    // 메인 Group 화면
    composable<MainTabRoutes.Group> { backStackEntry ->
        val groupViewModel: GroupViewModel = viewModel(
            viewModelStoreOwner = backStackEntry
        )
        
        GroupScreen(
            viewModel = groupViewModel,
            onNavigateToMakeRoom = {
                navController.navigateToGroupMakeRoom()
            },
            onNavigateToGroupDone = {
                navController.navigateToGroupDone()
            },
            onNavigateToAlarm = {
                navController.navigateToAlarm()
            },
            onNavigateToGroupSearch = {
                navController.navigateToGroupSearch()
            },
            onNavigateToGroupMy = {
                navController.navigateToGroupMy()
            },
            onNavigateToGroupRecruit = { roomId ->
                navController.navigateToGroupRecruit(roomId)
            },
            onNavigateToGroupRoom = { roomId ->
                navController.navigateToGroupRoom(roomId)
            }
        )
    }
    
    // Group MakeRoom 화면
    composable<GroupRoutes.MakeRoom> {
        val viewModel: GroupMakeRoomViewModel = viewModel()
        GroupMakeRoomScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navigateBack()
            },
            onGroupCreated = {
                navigateBack()
            }
        )
    }
    
    // Group Done 화면
    composable<GroupRoutes.Done> {
        val parentEntry = remember(navController) {
            try {
                navController.getBackStackEntry(MainTabRoutes.Group)
            } catch (e: Exception) {
                null
            }
        }
        val groupViewModel: GroupViewModel = if (parentEntry != null) {
            viewModel(viewModelStoreOwner = parentEntry)
        } else {
            viewModel()
        }
        val userName by groupViewModel.userName.collectAsState()
        val doneGroups by groupViewModel.doneGroups.collectAsState()
        
        GroupDoneScreen(
            name = userName,
            allDataList = doneGroups,
            onNavigateBack = {
                navigateBack()
            }
        )
    }
    
    // Group My 화면
    composable<GroupRoutes.My> {
        val parentEntry = remember(navController) {
            try {
                navController.getBackStackEntry(MainTabRoutes.Group)
            } catch (e: Exception) {
                null
            }
        }
        val groupViewModel: GroupViewModel = if (parentEntry != null) {
            viewModel(viewModelStoreOwner = parentEntry)
        } else {
            viewModel()
        }
        val myRoomGroups by groupViewModel.myRoomGroups.collectAsState()
        
        GroupMyScreen(
            allDataList = myRoomGroups,
            onCardClick = { room ->
                if (room.isRecruiting) {
                    navController.navigateToGroupRecruit(room.id)
                } else {
                    navController.navigateToGroupRoom(room.id)
                }
            },
            onNavigateBack = {
                navigateBack()
            }
        )
    }
    
    // Group Search 화면
    composable<GroupRoutes.Search> {
        val parentEntry = remember(navController) {
            try {
                navController.getBackStackEntry(MainTabRoutes.Group)
            } catch (e: Exception) {
                null
            }
        }
        val groupViewModel: GroupViewModel = if (parentEntry != null) {
            viewModel(viewModelStoreOwner = parentEntry)
        } else {
            viewModel()
        }
        val searchGroups by groupViewModel.searchGroups.collectAsState()
        
        GroupSearchScreen(
            roomList = searchGroups,
            onNavigateBack = {
                navigateBack()
            },
            onRoomClick = { room ->
                if (room.isRecruiting) {
                    navController.navigateToGroupRecruit(room.id)
                } else {
                    navController.navigateToGroupRoom(room.id)
                }
            }
        )
    }
    
    // Group Recruit 화면
    composable<GroupRoutes.Recruit> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.Recruit>()
        val roomId = route.roomId
        val parentEntry = remember(navController) {
            try {
                navController.getBackStackEntry(MainTabRoutes.Group)
            } catch (e: Exception) {
                null
            }
        }
        val groupViewModel: GroupViewModel = if (parentEntry != null) {
            viewModel(viewModelStoreOwner = parentEntry)
        } else {
            viewModel()
        }
        
        // suspend 함수를 위한 LaunchedEffect 사용
        var roomDetail by remember { mutableStateOf<GroupRoomData?>(null) }
        LaunchedEffect(roomId) {
            roomDetail = groupViewModel.getRoomDetail(roomId)
        }
        
        roomDetail?.let { detail ->
            GroupRoomRecruitScreen(
                detail = detail,
                buttonType = GroupBottomButtonType.JOIN, // 기본값, 실제로는 사용자 상태에 따라 결정
                onRecommendationClick = { recommendation ->
                    navController.navigateToRecommendedGroupRecruit(recommendation.id)
                },
                onParticipation = {
                    // 참여 로직
                },
                onCancelParticipation = {
                    // 참여 취소 로직
                },
                onCloseRecruitment = {
                    // 모집 마감 로직
                },
                onBackClick = {
                    navigateBack()
                }
            )
        } ?: run {
            // 로딩 중이거나 데이터를 찾을 수 없는 경우
        }
    }
    
    // Group Room 화면
    composable<GroupRoutes.Room> { backStackEntry ->
        val route = backStackEntry.toRoute<GroupRoutes.Room>()
        val roomId = route.roomId
        val parentEntry = remember(navController) {
            try {
                navController.getBackStackEntry(MainTabRoutes.Group)
            } catch (e: Exception) {
                null
            }
        }
        val groupViewModel: GroupViewModel = if (parentEntry != null) {
            viewModel(viewModelStoreOwner = parentEntry)
        } else {
            viewModel()
        }
        
        // suspend 함수를 위한 LaunchedEffect 사용
        var roomDetail by remember { mutableStateOf<GroupRoomData?>(null) }
        LaunchedEffect(roomId) {
            roomDetail = groupViewModel.getRoomDetail(roomId)
        }
        
        roomDetail?.let {
            GroupRoomScreen(
                onBackClick = {
                    navigateBack()
                }
            )
        } ?: run {
            // 로딩 중이거나 데이터를 찾을 수 없는 경우
        }
    }
}