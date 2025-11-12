package com.texthip.thip.data.repository

import retrofit2.HttpException
import android.util.Log
import com.texthip.thip.data.manager.TokenManager
import com.texthip.thip.data.model.base.BaseResponse
import com.texthip.thip.data.model.base.ThipApiFailureException
import com.texthip.thip.data.model.base.handleBaseResponse
import com.texthip.thip.data.model.users.request.FollowRequest
import com.texthip.thip.data.model.users.request.NicknameRequest
import com.texthip.thip.data.model.users.request.ProfileUpdateRequest
import com.texthip.thip.data.model.users.request.SignupRequest
import com.texthip.thip.data.model.users.response.AliasChoiceResponse
import com.texthip.thip.data.model.users.response.FollowResponse
import com.texthip.thip.data.model.users.response.MyFollowingsResponse
import com.texthip.thip.data.model.users.response.MyPageInfoResponse
import com.texthip.thip.data.model.users.response.NicknameResponse
import com.texthip.thip.data.model.users.response.OthersFollowersResponse
import com.texthip.thip.data.model.users.response.SignupResponse
import com.texthip.thip.data.model.users.response.UserSearchResponse
import com.texthip.thip.data.service.UserService
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userService: UserService,
    private val tokenManager: TokenManager,
    private val json: Json
) {
    //내 팔로잉 목록 조회
    suspend fun getMyFollowings(
        cursor: String?,
        size: Int = 10
    ): Result<MyFollowingsResponse?> = runCatching {
        userService.getMyFollowings(cursor = cursor, size = size)
            .handleBaseResponse()
            .getOrThrow()
    }

    suspend fun getMyFollowingsRecentFeeds() = runCatching {
        userService.getMyFollowingsRecentFeeds()
            .handleBaseResponse()
            .getOrThrow()
    }

    //다른 유저 팔로워 목록 조회
    suspend fun getOthersFollowers(
        userId: Long,
        cursor: String?,
        size: Int = 10
    ): Result<OthersFollowersResponse?> = runCatching {
        userService.getUserFollowers(userId = userId, cursor = cursor, size = size)
            .handleBaseResponse()
            .getOrThrow()
    }

    //마이페이지 정보 조회
    suspend fun getMyPageInfo(): Result<MyPageInfoResponse?> = runCatching {
        userService.getMyPage()
            .handleBaseResponse()
            .getOrThrow()
    }

    suspend fun checkNickname(
        nickname: String
    ): Result<NicknameResponse?> = runCatching {
        userService.checkNickname(NicknameRequest(nickname))
            .handleBaseResponse()
            .getOrThrow()
    }

    suspend fun getAliasChoices(): Result<AliasChoiceResponse?> = runCatching {
        userService.getAliasChoices()
            .handleBaseResponse()
            .getOrThrow()
    }

    suspend fun toggleFollow(
        followingUserId: Long,
        isFollowing: Boolean
    ): Result<FollowResponse?> = runCatching {
        val request = FollowRequest(type = isFollowing)
        userService.toggleFollow(followingUserId, request)
            .handleBaseResponse()
            .getOrThrow()
    }

    suspend fun updateProfile(
        request: ProfileUpdateRequest
    ): Result<Unit?> {
        return try {
            val response = userService.updateProfile(request)
            response.handleBaseResponse().getOrThrow()
            Result.success(Unit)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody != null) {
                try {
                    val baseResponse = json.decodeFromString<BaseResponse<Unit>>(errorBody)
                    Result.failure(ThipApiFailureException(baseResponse.code, baseResponse.message))
                } catch (jsonException: Exception) {
                    Result.failure(e) // JSON 파싱 실패 시 원래 HttpException 반환
                }
            } else {
                Result.failure(e) // 에러 본문이 없으면 원래 HttpException 반환
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun signup(
        request: SignupRequest
    ): Result<SignupResponse?> {
        Log.d("SignupDebug", "UserRepository.signup() 호출됨. 요청 닉네임: ${request.nickname}")

        val tempToken = tokenManager.getTempTokenOnce()

        Log.d("SignupDebug", "가져온 임시 토큰: $tempToken")

        if (tempToken.isNullOrBlank()) {
            return Result.failure(Exception("임시 토큰이 없습니다. 로그인을 다시 시도해주세요."))
        }
        return runCatching {
            userService.signup("Bearer $tempToken", request)
                .handleBaseResponse()
                .getOrThrow()
        }
    }

    suspend fun searchUsers(
        keyword: String,
        isFinalized: Boolean
    ): Result<UserSearchResponse?> = runCatching {
        userService.searchUsers(isFinalized = isFinalized, keyword = keyword)
            .handleBaseResponse()
            .getOrThrow()
    }

    suspend fun deleteAccount(): Result<Unit?> = runCatching {
        userService.deleteAccount()
            .handleBaseResponse()
            .getOrThrow()
    }
}