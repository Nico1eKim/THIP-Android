package com.texthip.thip.data.model.rooms.response

import kotlinx.serialization.Serializable

@Serializable
data class RoomsAiReviewResponse(
    val content: String,
    val count: Int
)
