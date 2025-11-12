package com.texthip.thip.data.repository

import android.net.Uri
import com.texthip.thip.data.model.base.handleBaseResponse
import com.texthip.thip.data.model.feed.request.CreateFeedRequest
import com.texthip.thip.data.model.feed.request.FeedLikeRequest
import com.texthip.thip.data.model.feed.request.FeedSaveRequest
import com.texthip.thip.data.model.feed.request.UpdateFeedRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import com.texthip.thip.data.model.feed.response.AllFeedResponse
import com.texthip.thip.data.model.feed.response.CreateFeedResponse
import com.texthip.thip.data.model.feed.response.FeedDetailResponse
import com.texthip.thip.data.model.feed.response.FeedLikeResponse
import com.texthip.thip.data.model.feed.response.FeedMineInfoResponse
import com.texthip.thip.data.model.feed.response.FeedSaveResponse
import com.texthip.thip.data.model.feed.response.FeedWriteInfoResponse
import com.texthip.thip.data.model.feed.response.MyFeedResponse
import com.texthip.thip.data.model.feed.response.RelatedBooksResponse
import com.texthip.thip.data.service.FeedService
import com.texthip.thip.ui.feed.mock.FeedStateUpdateResult
import com.texthip.thip.utils.image.ImageUploadHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val feedService: FeedService,
    private val imageUploadHelper: ImageUploadHelper
) {
    companion object {
        private const val MAX_CONCURRENT_UPLOADS = 3 // 동시 업로드 제한
    }
    private val _feedStateUpdateResult = MutableSharedFlow<FeedStateUpdateResult>()
    val feedStateUpdateResult: Flow<FeedStateUpdateResult> = _feedStateUpdateResult.asSharedFlow()

    /** 피드 작성에 필요한 카테고리 및 태그 목록 조회 */
    suspend fun getFeedWriteInfo(): Result<FeedWriteInfoResponse?> = runCatching {
        val response = feedService.getFeedWriteInfo()
            .handleBaseResponse()
            .getOrThrow()

        // 카테고리 순서 조정
        val orderedCategories = response?.categoryList?.sortedBy { category ->
            when (category.category) {
                "문학" -> 0
                "과학·IT" -> 1
                "사회과학" -> 2
                "인문학" -> 3
                "예술" -> 4
                else -> 999
            }
        } ?: emptyList()

        response?.copy(categoryList = orderedCategories)
    }

    /** 피드 생성 */
    suspend fun createFeed(
        isbn: String,
        contentBody: String,
        isPublic: Boolean,
        tagList: List<String>,
        imageUris: List<Uri>
    ): Result<CreateFeedResponse?> = runCatching {
        val imageUrls = if (imageUris.isNotEmpty()) {
            uploadImagesToS3(imageUris)
        } else {
            emptyList()
        }

        val request = CreateFeedRequest(
            isbn = isbn,
            contentBody = contentBody,
            isPublic = isPublic,
            tagList = tagList,
            imageUrls = imageUrls
        )

        feedService.createFeed(request)
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 이미지들을 S3에 업로드하고 CloudFront URL 목록 반환 */
    private suspend fun uploadImagesToS3(
        imageUris: List<Uri>
    ): List<String> =
        withContext(Dispatchers.IO) {
            val validImagePairs = imageUris.map { uri ->
                async {
                    imageUploadHelper.getImageMetadata(uri)?.let { metadata ->
                        uri to metadata
                    }
                }
            }.awaitAll().filterNotNull()

            if (validImagePairs.isEmpty()) return@withContext emptyList()

            val presignedUrlRequest = validImagePairs.map { it.second }

            val presignedResponse = feedService.getPresignedUrls(presignedUrlRequest)
                .handleBaseResponse()
                .getOrThrow() ?: throw Exception("Failed to get presigned URLs")

            // 개수 검증
            if (validImagePairs.size != presignedResponse.presignedUrls.size) {
                throw Exception("개수가 올바르지 않습니다: expected ${validImagePairs.size}, got ${presignedResponse.presignedUrls.size}")
            }

            // 세마포어를 사용한 제한 병렬 업로드
            val semaphore = Semaphore(MAX_CONCURRENT_UPLOADS)
            
            validImagePairs.mapIndexed { index, (uri, _) ->
                async {
                    semaphore.withPermit {
                        val presignedInfo = presignedResponse.presignedUrls[index]
                        
                        imageUploadHelper.uploadImageToS3(
                            uri = uri,
                            presignedUrl = presignedInfo.presignedUrl
                        ).fold(
                            onSuccess = { 
                                index to presignedInfo.fileUrl // 인덱스와 URL을 함께 반환
                            },
                            onFailure = { exception ->
                                throw Exception("Failed to upload image ${index + 1}: ${exception.message}")
                            }
                        )
                    }
                }
            }.awaitAll()
                .sortedBy { it.first } // 원래 순서대로 정렬
                .map { it.second } // URL만 추출
        }


    /** 전체 피드 목록 조회 */
    suspend fun getAllFeeds(cursor: String? = null): Result<AllFeedResponse?> = runCatching {
        feedService.getAllFeeds(cursor)
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 내 피드 목록 조회 */
    suspend fun getMyFeeds(
        cursor: String? = null
    ): Result<MyFeedResponse?> = runCatching {
        feedService.getMyFeeds(cursor)
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 내 피드 정보 조회 */
    suspend fun getMyFeedInfo(): Result<FeedMineInfoResponse?> = runCatching {
        feedService.getMyFeedInfo()
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 특정 책과 관련된 피드 목록 조회 */
    suspend fun getRelatedBookFeeds(
        isbn: String,
        sort: String? = null,
        cursor: String? = null
    ): Result<RelatedBooksResponse?> = runCatching {
        feedService.getRelatedBookFeeds(isbn, sort, cursor)
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 피드 상세 조회 */
    suspend fun getFeedDetail(
        feedId: Long
    ): Result<FeedDetailResponse?> = runCatching {
        feedService.getFeedDetail(feedId)
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 피드 수정 */
    suspend fun updateFeed(
        feedId: Long,
        contentBody: String? = null,
        isPublic: Boolean? = null,
        tagList: List<String>? = null,
        remainImageUrls: List<String>? = null
    ): Result<CreateFeedResponse?> = runCatching {
        val request = UpdateFeedRequest(
            contentBody = contentBody,
            isPublic = isPublic,
            tagList = tagList,
            remainImageUrls = remainImageUrls
        )

        feedService.updateFeed(feedId, request)
            .handleBaseResponse()
            .getOrThrow()
    }


    suspend fun getFeedUsersInfo(
        userId: Long
    ) = runCatching {
        feedService.getFeedUsersInfo(userId)
            .handleBaseResponse()
            .getOrThrow()
    }

    suspend fun getFeedUsers(userId: Long) = runCatching {
        feedService.getFeedUsers(userId)
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 피드 삭제 */
    suspend fun deleteFeed(feedId: Long): Result<String?> = runCatching {
        feedService.deleteFeed(feedId)
            .handleBaseResponse()
            .getOrThrow()
    }

    /*suspend fun changeFeedLike(feedId: Long, newLikeStatus: Boolean): Result<FeedLikeResponse?> = runCatching {
        val request = FeedLikeRequest(type = newLikeStatus)
        feedService.changeFeedLike(feedId, request)
            .handleBaseResponse()
            .getOrThrow()
    }*/
    suspend fun changeFeedLike(
        feedId: Long, newLikeStatus: Boolean,
        currentLikeCount: Int,
        currentIsSaved: Boolean
    ): Result<FeedLikeResponse?> {
        // 👈 3. 기존 로직을 수정하여 성공 시 방송(emit)하도록 변경
        return runCatching {
            val request = FeedLikeRequest(type = newLikeStatus)
            feedService.changeFeedLike(feedId, request)
                .handleBaseResponse()
                .getOrThrow()
        }.onSuccess { response ->
            // API 호출 성공 및 응답 데이터가 있을 경우
            response?.let {
                // 변경된 상태를 객체로 만들어 방송(emit)
                val newLikeCount = if (it.isLiked) currentLikeCount + 1 else currentLikeCount - 1
                val update = FeedStateUpdateResult(
                    feedId = feedId,
                    isLiked = it.isLiked,
                    likeCount = newLikeCount,
                    isSaved = currentIsSaved, // isSaved 상태는 그대로 유지
                    commentCount = 0 // 좋아요 함수에서는 댓글 수 정보 없음
                )
                _feedStateUpdateResult.emit(update)
            }
        }
    }

    /** 피드 저장 */
    suspend fun changeFeedSave(
        feedId: Long, newSaveStatus: Boolean, currentIsLiked: Boolean,
        currentLikeCount: Int
    ): Result<FeedSaveResponse?> =
        runCatching {
            val request = FeedSaveRequest(type = newSaveStatus)
            feedService.changeFeedSave(feedId, request)
                .handleBaseResponse()
                .getOrThrow()
        }.onSuccess { response ->
            response?.let {
                // API 응답(isSaved)과 파라미터로 받은 값들을 조합
                val update = FeedStateUpdateResult(
                    feedId = feedId,
                    isLiked = currentIsLiked, // isLiked 상태는 그대로 유지
                    likeCount = currentLikeCount,
                    isSaved = it.isSaved,
                    commentCount = 0 // 저장 함수에서는 댓글 수 정보 없음
                )
                _feedStateUpdateResult.emit(update)
            }
        }

    suspend fun getSavedFeeds(cursor: String? = null): Result<AllFeedResponse?> = runCatching {
        feedService.getSavedFeeds(cursor)
            .handleBaseResponse()
            .getOrThrow()
    }
}