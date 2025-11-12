package com.texthip.thip.data.model.rooms.response

import kotlinx.serialization.Serializable

@Serializable
data class RoomsAiUsageResponse(
    val recordReviewCount: Int,
    val recordCount: Int
)
