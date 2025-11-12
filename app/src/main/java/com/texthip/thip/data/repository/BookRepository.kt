package com.texthip.thip.data.repository

import com.texthip.thip.data.model.base.handleBaseResponse
import com.texthip.thip.data.model.book.request.BookSaveRequest
import com.texthip.thip.data.model.book.response.BookDetailResponse
import com.texthip.thip.data.model.book.response.BookListResponse
import com.texthip.thip.data.model.book.response.BookSaveResponse
import com.texthip.thip.data.model.book.response.BookSearchResponse
import com.texthip.thip.data.model.book.response.BookUserSaveResponse
import com.texthip.thip.data.model.book.response.MostSearchedBooksResponse
import com.texthip.thip.data.model.book.response.RecruitingRoomsResponse
import com.texthip.thip.data.service.BookService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val bookService: BookService
) {

    /** 저장된 책 또는 모임 책 목록 조회 */
    suspend fun getBooks(
        type: String,
        cursor: String? = null
    ): Result<BookListResponse?> =
        runCatching {
            bookService.getBooks(type, cursor)
                .handleBaseResponse()
                .getOrThrow()
        }

    /** 책 검색 */
    suspend fun searchBooks(
        keyword: String,
        page: Int = 1,
        isFinalized: Boolean = false
    ): Result<BookSearchResponse?> = runCatching {
        bookService.searchBooks(keyword, page, isFinalized)
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 인기 책 조회 */
    suspend fun getMostSearchedBooks(
    ): Result<MostSearchedBooksResponse?> = runCatching {
        bookService.getMostSearchedBooks()
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 책 상세 조회 */
    suspend fun getBookDetail(
        isbn: String
    ): Result<BookDetailResponse?> = runCatching {
        bookService.getBookDetail(isbn)
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 책 저장/저장취소 */
    suspend fun saveBook(
        isbn: String,
        type: Boolean
    ): Result<BookSaveResponse?> = runCatching {
        bookService.saveBook(isbn, BookSaveRequest(type))
            .handleBaseResponse()
            .getOrThrow()
    }

    /** 모집중인 방 조회 */
    suspend fun getRecruitingRooms(
        isbn: String,
        cursor: String? = null
    ): Result<RecruitingRoomsResponse?> = runCatching {
        bookService.getRecruitingRooms(isbn, cursor)
            .handleBaseResponse()
            .getOrThrow()
    }

    suspend fun getSavedBooks(
        cursor: String? = null
    ): Result<BookUserSaveResponse?> = runCatching {
        bookService.getSavedBooks(cursor)
            .handleBaseResponse()
            .getOrThrow()
    }
}