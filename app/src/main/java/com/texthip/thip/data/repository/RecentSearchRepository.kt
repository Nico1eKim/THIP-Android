package com.texthip.thip.data.repository

import com.texthip.thip.data.model.base.handleBaseResponse
import com.texthip.thip.data.model.book.response.RecentSearchResponse
import com.texthip.thip.data.service.RecentSearchService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentSearchRepository @Inject constructor(
    private val recentSearchService: RecentSearchService
) {

    /** 최근 검색어 조회 */
    suspend fun getRecentSearches(
        type: String
    ): Result<RecentSearchResponse?> = runCatching {
        recentSearchService.getRecentSearches(type)
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 최근 검색어 삭제 */
    suspend fun deleteRecentSearch(
        recentSearchId: Int
    ): Result<Unit> = runCatching {
        recentSearchService.deleteRecentSearch(recentSearchId)
            .handleBaseResponse()
            .getOrThrow()
            ?: Unit
    }
}