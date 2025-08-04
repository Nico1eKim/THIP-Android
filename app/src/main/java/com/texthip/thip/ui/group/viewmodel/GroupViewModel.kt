package com.texthip.thip.ui.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.texthip.thip.data.group.repository.GroupRepository
import com.texthip.thip.ui.group.myroom.mock.GroupCardData
import com.texthip.thip.ui.group.myroom.mock.GroupCardItemRoomData
import com.texthip.thip.ui.group.myroom.mock.GroupRoomSectionData
import com.texthip.thip.ui.group.myroom.mock.GroupRoomData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupViewModel(
    private val repository: GroupRepository = GroupRepository()
) : ViewModel() {

    private val _myGroups = MutableStateFlow<List<GroupCardData>>(emptyList())
    val myGroups: StateFlow<List<GroupCardData>> = _myGroups.asStateFlow()

    private val _roomSections = MutableStateFlow<List<GroupRoomSectionData>>(emptyList())
    val roomSections: StateFlow<List<GroupRoomSectionData>> = _roomSections.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _doneGroups = MutableStateFlow<List<GroupCardItemRoomData>>(emptyList())
    val doneGroups: StateFlow<List<GroupCardItemRoomData>> = _doneGroups.asStateFlow()

    private val _myRoomGroups = MutableStateFlow<List<GroupCardItemRoomData>>(emptyList())
    val myRoomGroups: StateFlow<List<GroupCardItemRoomData>> = _myRoomGroups.asStateFlow()

    private val _searchGroups = MutableStateFlow<List<GroupCardItemRoomData>>(emptyList())
    val searchGroups: StateFlow<List<GroupCardItemRoomData>> = _searchGroups.asStateFlow()

    private val _genres = MutableStateFlow<List<String>>(emptyList())
    val genres: StateFlow<List<String>> = _genres.asStateFlow()
    
    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadUserName()
        loadMyGroups()
        loadRoomSections()
        loadDoneGroups()
        loadMyRoomGroups()
        loadSearchGroups()
    }
    
    private fun loadUserName() {
        viewModelScope.launch {
            repository.getUserName()
                .onSuccess { userName ->
                    _userName.value = userName
                }
        }
    }
    
    private fun loadMyGroups() {
        viewModelScope.launch {
            repository.getMyGroups()
                .onSuccess { groups ->
                    _myGroups.value = groups
                }
        }
    }
    
    private fun loadRoomSections() {
        viewModelScope.launch {
            repository.getRoomSections()
                .onSuccess { sections ->
                    _roomSections.value = sections
                }
        }
    }
    
    private fun loadDoneGroups() {
        viewModelScope.launch {
            repository.getDoneGroups()
                .onSuccess { groups ->
                    _doneGroups.value = groups
                }
        }
    }
    
    private fun loadMyRoomGroups() {
        viewModelScope.launch {
            repository.getMyRoomGroups()
                .onSuccess { groups ->
                    _myRoomGroups.value = groups
                }
        }
    }
    
    private fun loadSearchGroups() {
        viewModelScope.launch {
            repository.getSearchGroups()
                .onSuccess { groups ->
                    _searchGroups.value = groups
                }
        }
    }
    
    fun refreshGroupData() {
        loadInitialData()
    }
    
    
    suspend fun getRoomDetail(roomId: Int): GroupRoomData? {
        return repository.getRoomDetail(roomId).getOrNull()
    }

}