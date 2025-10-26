package com.texthip.thip.data.service

import com.texthip.thip.data.model.base.BaseResponse
import com.texthip.thip.data.model.rooms.request.CreateRoomRequest
import com.texthip.thip.data.model.rooms.request.RoomJoinRequest
import com.texthip.thip.data.model.rooms.request.RoomSecretRoomRequest
import com.texthip.thip.data.model.rooms.request.RoomsCreateDailyGreetingRequest
import com.texthip.thip.data.model.rooms.request.RoomsCreateVoteRequest
import com.texthip.thip.data.model.rooms.request.RoomsPatchRecordRequest
import com.texthip.thip.data.model.rooms.request.RoomsPatchVoteRequest
import com.texthip.thip.data.model.rooms.request.RoomsPostsLikesRequest
import com.texthip.thip.data.model.rooms.request.RoomsRecordRequest
import com.texthip.thip.data.model.rooms.request.RoomsVoteRequest
import com.texthip.thip.data.model.rooms.response.CreateRoomResponse
import com.texthip.thip.data.model.rooms.response.JoinedRoomListResponse
import com.texthip.thip.data.model.rooms.response.MyRoomListResponse
import com.texthip.thip.data.model.rooms.response.RoomCloseResponse
import com.texthip.thip.data.model.rooms.response.RoomJoinResponse
import com.texthip.thip.data.model.rooms.response.RoomMainList
import com.texthip.thip.data.model.rooms.response.RoomRecruitingResponse
import com.texthip.thip.data.model.rooms.response.RoomSecretRoomResponse
import com.texthip.thip.data.model.rooms.response.RoomsAiUsageResponse
import com.texthip.thip.data.model.rooms.response.RoomsBookPageResponse
import com.texthip.thip.data.model.rooms.response.RoomsCreateDailyGreetingResponse
import com.texthip.thip.data.model.rooms.response.RoomsCreateVoteResponse
import com.texthip.thip.data.model.rooms.response.RoomsDailyGreetingResponse
import com.texthip.thip.data.model.rooms.response.RoomsDeleteDailyGreetingResponse
import com.texthip.thip.data.model.rooms.response.RoomsDeleteRecordResponse
import com.texthip.thip.data.model.rooms.response.RoomsDeleteVoteResponse
import com.texthip.thip.data.model.rooms.response.RoomsPatchRecordResponse
import com.texthip.thip.data.model.rooms.response.RoomsPatchVoteResponse
import com.texthip.thip.data.model.rooms.response.RoomsPlayingResponse
import com.texthip.thip.data.model.rooms.response.RoomsPostsLikesResponse
import com.texthip.thip.data.model.rooms.response.RoomsPostsResponse
import com.texthip.thip.data.model.rooms.response.RoomsRecordResponse
import com.texthip.thip.data.model.rooms.response.RoomsRecordsPinResponse
import com.texthip.thip.data.model.rooms.response.RoomsSearchResponse
import com.texthip.thip.data.model.rooms.response.RoomsUsersResponse
import com.texthip.thip.data.model.rooms.response.RoomsVoteResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomsService {

    /** 참여 중인 모임방 목록 조회 */
    @GET("rooms/home/joined")
    suspend fun getJoinedRooms(
        @Query("cursor") cursor: String? = null
    ): BaseResponse<JoinedRoomListResponse>

    /** 카테고리별 모임방 목록 조회 (마감임박/인기/최근 생성) */
    @GET("rooms")
    suspend fun getRooms(
        @Query("category") category: String = "문학"
    ): BaseResponse<RoomMainList>

    /** 내가 만든/참여한 모임방 목록 조회 */
    @GET("rooms/my")
    suspend fun getMyRooms(
        @Query("type") type: String? = null,
        @Query("cursor") cursor: String? = null
    ): BaseResponse<MyRoomListResponse>

    /** 모집중인 모임방 상세 정보 조회 */
    @GET("rooms/{roomId}/recruiting")
    suspend fun getRoomRecruiting(@Path("roomId") roomId: Int): BaseResponse<RoomRecruitingResponse>

    /** 새 모임방 생성 */
    @POST("rooms")
    suspend fun createRoom(
        @Body request: CreateRoomRequest
    ): BaseResponse<CreateRoomResponse>

    /** 모임방 참여/취소 */
    @POST("rooms/{roomId}/join")
    suspend fun joinOrCancelRoom(
        @Path("roomId") roomId: Int,
        @Body request: RoomJoinRequest
    ): BaseResponse<RoomJoinResponse>

    /** 비밀번호 입력 */
    @POST("rooms/{roomId}/password")
    suspend fun postParticipateSecretRoom(
        @Path("roomId") roomId: Int,
        @Body request: RoomSecretRoomRequest
    ): BaseResponse<RoomSecretRoomResponse>

    /** 모집 마감 */
    @POST("rooms/{roomId}/close")
    suspend fun closeRoom(
        @Path("roomId") roomId: Int
    ): BaseResponse<RoomCloseResponse>

    /** 모임방 검색 */
    @GET("rooms/search")
    suspend fun searchRooms(
        @Query("keyword") keyword: String,
        @Query("category") category: String,
        @Query("isAllCategory") isAllCategory: Boolean = false,
        @Query("sort") sort: String = "deadline",
        @Query("isFinalized") isFinalized: Boolean = false,
        @Query("cursor") cursor: String? = null
    ): BaseResponse<RoomsSearchResponse>



    /** 기록장 API들 */
    @GET("rooms/{roomId}")
    suspend fun getRoomsPlaying(
        @Path("roomId") roomId: Int
    ): BaseResponse<RoomsPlayingResponse>

    @GET("rooms/{roomId}/users")
    suspend fun getRoomsUsers(
        @Path("roomId") roomId: Int
    ): BaseResponse<RoomsUsersResponse>

    @GET("rooms/{roomId}/posts")
    suspend fun getRoomsPosts(
        @Path("roomId") roomId: Int,
        @Query("type") type: String = "group",
        @Query("sort") sort: String? = "latest",
        @Query("pageStart") pageStart: Int? = null,
        @Query("pageEnd") pageEnd: Int? = null,
        @Query("isOverview") isOverview: Boolean? = false,
        @Query("isPageFilter") isPageFilter: Boolean? = false,
        @Query("cursor") cursor: String? = null,
    ): BaseResponse<RoomsPostsResponse>

    @POST("rooms/{roomId}/record")
    suspend fun postRoomsRecord(
        @Path("roomId") roomId: Int,
        @Body request: RoomsRecordRequest
    ): BaseResponse<RoomsRecordResponse>

    @POST("rooms/{roomId}/vote")
    suspend fun postRoomsCreateVote(
        @Path("roomId") roomId: Int,
        @Body request: RoomsCreateVoteRequest
    ): BaseResponse<RoomsCreateVoteResponse>

    @GET("rooms/{roomId}/book-page")
    suspend fun getRoomsBookPage(
        @Path("roomId") roomId: Int,
    ): BaseResponse<RoomsBookPageResponse>

    @POST("rooms/{roomId}/vote/{voteId}")
    suspend fun postRoomsVote(
        @Path("roomId") roomId: Int,
        @Path("voteId") voteId: Int,
        @Body request: RoomsVoteRequest
    ): BaseResponse<RoomsVoteResponse>

    @DELETE("rooms/{roomId}/record/{recordId}")
    suspend fun deleteRoomsRecord(
        @Path("roomId") roomId: Int,
        @Path("recordId") recordId: Int
    ): BaseResponse<RoomsDeleteRecordResponse>

    @DELETE("rooms/{roomId}/vote/{voteId}")
    suspend fun deleteRoomsVote(
        @Path("roomId") roomId: Int,
        @Path("voteId") voteId: Int
    ): BaseResponse<RoomsDeleteVoteResponse>

    @POST("room-posts/{postId}/likes")
    suspend fun postRoomsPostsLikes(
        @Path("postId") postId: Int,
        @Body request: RoomsPostsLikesRequest
    ): BaseResponse<RoomsPostsLikesResponse>

    @GET("rooms/{roomId}/daily-greeting")
    suspend fun getRoomsDailyGreeting(
        @Path("roomId") roomId: Int,
        @Query("cursor") cursor: String? = null
    ): BaseResponse<RoomsDailyGreetingResponse>

    @POST("rooms/{roomId}/daily-greeting")
    suspend fun postRoomsDailyGreeting(
        @Path("roomId") roomId: Int,
        @Body request: RoomsCreateDailyGreetingRequest
    ): BaseResponse<RoomsCreateDailyGreetingResponse>

    @DELETE("rooms/{roomId}/daily-greeting/{attendanceCheckId}")
    suspend fun deleteRoomsDailyGreeting(
        @Path("roomId") roomId: Int,
        @Path("attendanceCheckId") attendanceCheckId: Int
    ): BaseResponse<RoomsDeleteDailyGreetingResponse>

    @GET("rooms/{roomId}/records/{recordId}/pin")
    suspend fun getRoomsRecordsPin(
        @Path("roomId") roomId: Int,
        @Path("recordId") recordId: Int
    ): BaseResponse<RoomsRecordsPinResponse>

    @DELETE("rooms/{roomId}/leave")
    suspend fun leaveRoom(
        @Path("roomId") roomId: Int
    ): BaseResponse<Unit>

    @PATCH("rooms/{roomId}/records/{recordId}")
    suspend fun patchRoomsRecord(
        @Path("roomId") roomId: Int,
        @Path("recordId") recordId: Int,
        @Body request: RoomsPatchRecordRequest
    ): BaseResponse<RoomsPatchRecordResponse>

    @PATCH("rooms/{roomId}/votes/{voteId}")
    suspend fun patchRoomsVote(
        @Path("roomId") roomId: Int,
        @Path("voteId") voteId: Int,
        @Body request: RoomsPatchVoteRequest
    ): BaseResponse<RoomsPatchVoteResponse>

    @GET("rooms/{roomId}/users/ai-usage")
    suspend fun getRoomsAiUsage(
        @Path("roomId") roomId: Int
    ): BaseResponse<RoomsAiUsageResponse>
}