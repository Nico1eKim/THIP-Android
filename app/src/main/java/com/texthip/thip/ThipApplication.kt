package com.texthip.thip

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.texthip.thip.data.manager.TokenManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class ThipApplication : Application() {
    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate() {
        super.onCreate()

        // 카카오 SDK 초기화
        try {
            KakaoSdk.init(this, BuildConfig.NATIVE_APP_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}