package com.texthip.thip.ui.navigator.extensions

import android.util.Log
import androidx.navigation.NavController
import com.texthip.thip.data.model.notification.response.NotificationCheckResponse
import com.texthip.thip.data.model.notification.response.NotificationRoute
import com.texthip.thip.ui.navigator.routes.FeedRoutes
import com.texthip.thip.ui.navigator.routes.GroupRoutes
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private fun JsonElement.toStringOrNull(): String? {
    return (this as? JsonPrimitive)?.contentOrNull
}

fun NavController.navigateFromNotification(response: NotificationCheckResponse) {
    val params = response.params

    try {
        when (response.route) {
            NotificationRoute.FEED_USER -> {
                val userId = params["userId"]?.toStringOrNull()?.toLongOrNull()
                if (userId != null) {
                    navigate(FeedRoutes.Others(userId))
                }
            }

            NotificationRoute.FEED_DETAIL -> {
                val feedId = params["feedId"]?.toStringOrNull()?.toLongOrNull()
                if (feedId != null) {
                    navigate(FeedRoutes.Comment(feedId))
                }
            }

            NotificationRoute.ROOM_MAIN -> {
                val roomId = params["roomId"]?.toStringOrNull()?.toIntOrNull()
                if (roomId != null) {
                    navigate(GroupRoutes.Room(roomId))
                }
            }

            NotificationRoute.ROOM_DETAIL -> {
                val roomId = params["roomId"]?.toStringOrNull()?.toIntOrNull()
                if (roomId != null) {
                    navigate(GroupRoutes.Recruit(roomId))
                }
            }

            NotificationRoute.ROOM_POST_DETAIL -> {
                val roomId = params["roomId"]?.toStringOrNull()?.toIntOrNull()
                val page = params["page"]?.toStringOrNull()?.toIntOrNull()
                val postId = params["postId"]?.toStringOrNull()?.toIntOrNull()
                val postType = params["postType"]?.toStringOrNull()
                val openComments =
                    params["openComments"]?.toStringOrNull()?.toBooleanStrictOrNull() ?: false

                if (roomId != null && page != null) {
                    navigate(GroupRoutes.Note(roomId, page, openComments, false, postId))
                }
            }
        }
    } catch (e: Exception) {
        Log.e("NotificationNav", "Navigation failed", e)
    }
}