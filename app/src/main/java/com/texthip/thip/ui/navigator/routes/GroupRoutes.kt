package com.texthip.thip.ui.navigator.routes

import kotlinx.serialization.Serializable

@Serializable
sealed class GroupRoutes : Routes() {
    @Serializable
    data object MakeRoom : GroupRoutes()

    @Serializable
    data class MakeRoomWithBook(
        val isbn: String,
        val title: String,
        val imageUrl: String,
        val author: String
    ) : GroupRoutes()

    @Serializable
    data class Search(val viewAll: Boolean = false) : GroupRoutes()

    @Serializable
    data object My : GroupRoutes()

    @Serializable
    data class Recruit(val roomId: Int) : GroupRoutes()

    @Serializable
    data class RoomUnlock(val roomId: Int) : GroupRoutes()

    @Serializable
    data class Room(val roomId: Int, val isExpired: Boolean = false) : GroupRoutes()

    @Serializable
    data class RoomMates(val roomId: Int) : GroupRoutes()

    @Serializable
    data class RoomChat(val roomId: Int, val isExpired: Boolean = false) : GroupRoutes()

    @Serializable
    data class Note(
        val roomId: Int,
        val page: Int? = null,
        val openComments: Boolean = false,
        val isExpired: Boolean = false,
        val postId: Int? = null,
        val isOverview: Boolean? = null
    ) : GroupRoutes()

    @Serializable
    data class NoteCreate(
        val roomId: Int,
        val recentBookPage: Int,
        val totalBookPage: Int,
        val isOverviewPossible: Boolean,
        val postId: Int? = null,
        val page: Int? = null,
        val content: String? = null,
        val isOverview: Boolean? = null
    )

    @Serializable
    data class VoteCreate(
        val roomId: Int,
        val recentPage: Int,
        val totalPage: Int,
        val isOverviewPossible: Boolean,
        val postId: Int? = null,
        val page: Int? = null,
        val isOverview: Boolean? = null,
        val title: String? = null,
        val options: List<String>? = null
    )

    @Serializable
    data class NoteAi(val roomId: Int) : GroupRoutes()
}