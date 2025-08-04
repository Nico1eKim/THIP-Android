package com.texthip.thip.ui.navigator.routes

import kotlinx.serialization.Serializable

@Serializable
sealed class CommonRoutes : Routes() {
    @Serializable
    data object Alarm : CommonRoutes()
}