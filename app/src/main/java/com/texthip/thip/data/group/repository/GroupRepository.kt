package com.texthip.thip.data.group.repository

import com.texthip.thip.R
import com.texthip.thip.ui.group.myroom.mock.GroupBookData
import com.texthip.thip.ui.group.myroom.mock.GroupCardData
import com.texthip.thip.ui.group.myroom.mock.GroupCardItemRoomData
import com.texthip.thip.ui.group.myroom.mock.GroupRoomData
import com.texthip.thip.ui.group.myroom.mock.GroupRoomSectionData
import kotlinx.coroutines.delay

// 그룹 데이터를 제공하는 Repository
// 실제로는 서버의 API와 통신할 거라서 다 삭제하고 함수 구조만 유지한 채 수정하면 될 듯 합니다.

class GroupRepository {
    
    private val genres = listOf("문학", "과학·IT", "사회과학", "인문학", "예술")
    private val roomDetailsCache = mutableMapOf<Int, GroupRoomData>()
    
    suspend fun getUserName(): Result<String> {
        return try {
            Result.success("규빈")    // 임시 이름
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMyGroups(): Result<List<GroupCardData>> {
        return try {
            delay(200)
            val myGroups = listOf(
                GroupCardData(23, "호르몬 체인지 완독하는 방", 22, R.drawable.bookcover_sample, 40, "uibowl1"),
                GroupCardData(24, "명작 읽기방", 10, R.drawable.bookcover_sample, 70, "joyce"),
                GroupCardData(25, "또 다른 방", 13, R.drawable.bookcover_sample, 10, "other")
            )
            Result.success(myGroups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRoomSections(): Result<List<GroupRoomSectionData>> {
        return try {

            // 마감 임박한 독서 모임방
            val deadlineRooms = listOf(
                GroupCardItemRoomData(1, "시집만 읽는 사람들 3월", 22, 30, true, 3, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(2, "일본 소설 좋아하는 사람들", 15, 20, true, 2, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(3, "명작 같이 읽기방", 22, 30, true, 3, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(4, "물리책 읽는 방", 13, 20, true, 1, R.drawable.bookcover_sample, 1),
                GroupCardItemRoomData(5, "코딩 과학 동아리", 12, 15, true, 5, R.drawable.bookcover_sample, 1),
                GroupCardItemRoomData(6, "사회과학 인문 탐구", 8, 12, true, 4, R.drawable.bookcover_sample, 2)
            )
            
            // 인기 있는 독서 모임방
            val popularRooms = listOf(
                GroupCardItemRoomData(7, "베스트셀러 토론방", 28, 30, true, 7, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(8, "인기 소설 완독방", 25, 25, false, 5, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(9, "트렌드 과학서 읽기", 20, 25, true, 10, R.drawable.bookcover_sample, 1),
                GroupCardItemRoomData(10, "화제의 경영서", 18, 20, true, 8, R.drawable.bookcover_sample, 2),
                GroupCardItemRoomData(11, "인기 철학서 모임", 15, 18, true, 12, R.drawable.bookcover_sample, 3),
                GroupCardItemRoomData(12, "예술서 베스트", 12, 15, true, 6, R.drawable.bookcover_sample, 4)
            )
            
            // 인플루언서, 작가 독서 모임방
            val influencerRooms = listOf(
                GroupCardItemRoomData(13, "작가와 함께하는 독서방", 30, 30, false, 14, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(14, "유명 북튜버와 읽기", 18, 20, true, 8, R.drawable.bookcover_sample, 2),
                GroupCardItemRoomData(15, "작가 초청 인문학방", 15, 20, true, 12, R.drawable.bookcover_sample, 3),
                GroupCardItemRoomData(16, "인플루언서 과학책", 22, 25, true, 9, R.drawable.bookcover_sample, 1),
                GroupCardItemRoomData(17, "유명작가 예술론", 16, 18, true, 11, R.drawable.bookcover_sample, 4)
            )
            
            val sections = listOf(
                GroupRoomSectionData(
                    title = "마감 임박한 독서 모임방",
                    rooms = deadlineRooms,
                    genres = genres
                ),
                GroupRoomSectionData(
                    title = "인기 있는 독서 모임방",
                    rooms = popularRooms,
                    genres = genres
                ),
                GroupRoomSectionData(
                    title = "인플루언서·작가 독서 모임방",
                    rooms = influencerRooms,
                    genres = genres
                )
            )

            // 상세 데이터 캐시에 저장
            (deadlineRooms + popularRooms + influencerRooms).forEach { room ->
                initializeRoomDetail(room)
            }
            
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDoneGroups(): Result<List<GroupCardItemRoomData>> {
        return try {
            val doneGroups = listOf(
                GroupCardItemRoomData(18, "완료된 독서 모임방 1", 15, 20, false, null, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(19, "완료된 독서 모임방 2", 25, 30, false, null, R.drawable.bookcover_sample, 1),
                GroupCardItemRoomData(20, "완료된 독서 모임방 3", 12, 15, false, null, R.drawable.bookcover_sample, 2),
                GroupCardItemRoomData(21, "호르몬 체인지 완독한 방", 22, 22, false, null, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(22, "명작 읽기방 완료", 10, 10, false, null, R.drawable.bookcover_sample, 0)
            )

            // 상세 데이터 캐시에 저장
            doneGroups.forEach { room ->
                initializeRoomDetail(room)
            }
            
            Result.success(doneGroups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMyRoomGroups(): Result<List<GroupCardItemRoomData>> {
        return try {
            val myRoomGroups = listOf(
                GroupCardItemRoomData(23, "호르몬 체인지 완독하는 방", 22, 30, true, 5, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(24, "명작 읽기방", 10, 20, true, 3, R.drawable.bookcover_sample, 0),
                GroupCardItemRoomData(25, "또 다른 방", 13, 25, false, 10, R.drawable.bookcover_sample, 1),
                GroupCardItemRoomData(26, "내가 참여한 과학책방", 18, 25, true, 7, R.drawable.bookcover_sample, 1),
                GroupCardItemRoomData(27, "인문학 토론방", 12, 20, true, 2, R.drawable.bookcover_sample, 3)
            )

            // 상세 데이터 캐시에 저장
            myRoomGroups.forEach { room ->
                initializeRoomDetail(room)
            }
            
            Result.success(myRoomGroups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSearchGroups(): Result<List<GroupCardItemRoomData>> {
        return try {
            // 기존에 로드된 섹션 데이터들을 합쳐서 반환
            val sectionsResult = getRoomSections()
            if (sectionsResult.isSuccess) {
                val allRooms = sectionsResult.getOrThrow().flatMap { it.rooms }
                Result.success(allRooms)
            } else {
                Result.failure(sectionsResult.exceptionOrNull() ?: Exception("Failed to load search groups"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRoomDetail(roomId: Int): Result<GroupRoomData> {
        return try {
            delay(150)
            val roomDetail = roomDetailsCache[roomId]
            if (roomDetail != null) {
                Result.success(roomDetail)
            } else {
                Result.failure(Exception("Room not found: $roomId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchRooms(query: String): Result<List<GroupCardItemRoomData>> {
        return try {
            val searchResult = getSearchGroups()
            if (searchResult.isSuccess) {
                val filteredRooms = searchResult.getOrThrow().filter { room ->
                    room.title.contains(query, ignoreCase = true)
                }
                Result.success(filteredRooms)
            } else {
                searchResult
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getGenres(): Result<List<String>> {
        return try {
            delay(50)
            Result.success(genres)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    private fun initializeRoomDetail(room: GroupCardItemRoomData) {
        val bookData = GroupBookData(
            title = "심장보다 단단한 토마토 한 알",
            author = "고선지",
            publisher = "푸른출판사",
            description = "${room.title}에서 읽는 책입니다. 감동적인 이야기로 가득한 작품입니다.",
            imageRes = room.imageRes ?: R.drawable.bookcover_sample
        )
        
        val recommendations = getRecommendations(room.id)
        
        val roomDetail = GroupRoomData(
            id = room.id,
            title = room.title,
            isSecret = room.isSecret,
            description = "${room.title} 모임입니다. 함께 책을 읽고 토론해요.",
            startDate = "2025.01.12",
            endDate = "2025.02.12",
            members = room.participants,
            maxMembers = room.maxParticipants,
            daysLeft = room.endDate ?: 0,
            genre = genres[room.genreIndex],
            bookData = bookData,
            recommendations = recommendations
        )
        
        roomDetailsCache[room.id] = roomDetail
        
        // 추천 모임방들의 상세 정보도 캐시에 추가
        recommendations.forEach { recommendedRoom ->
            if (!roomDetailsCache.containsKey(recommendedRoom.id)) {
                initializeRecommendedRoomDetail(recommendedRoom)
            }
        }
    }

    // 추천 모임방 예시 by gpt
    private fun initializeRecommendedRoomDetail(room: GroupCardItemRoomData) {
        val bookTitles = listOf(
            "데미안", "1984", "노인과 바다", "위대한 개츠비", "햄릿",
            "코스모스", "이기적 유전자", "블랙홀과 시간여행", "총균쇠",
            "국부론", "자본론", "사피엔스", "총균쇠", "정의란 무엇인가",
            "예술의 역사", "음악의 역사", "미학 오디세이"
        )
        
        val authors = listOf(
            "헤르만 헤세", "조지 오웰", "어니스트 헤밍웨이", "스콧 피츠제럴드",
            "칼 세이건", "리처드 도킨스", "킵 손", "재레드 다이아몬드",
            "아담 스미스", "칼 마르크스", "유발 하라리", "마이클 샌델"
        )
        
        val publishers = listOf("푸른출판사", "문학동네", "민음사", "창비", "열린책들", "김영사")
        
        val bookData = GroupBookData(
            title = bookTitles.random(),
            author = authors.random(),
            publisher = publishers.random(),
            description = "${room.title}에서 읽는 흥미로운 책입니다. 함께 읽으며 깊이 있는 토론을 나눠보세요.",
            imageRes = room.imageRes ?: R.drawable.bookcover_sample
        )
        
        val roomDetail = GroupRoomData(
            id = room.id,
            title = room.title,
            isSecret = room.isSecret,
            description = "${room.title} 모임입니다. 다양한 관점으로 책을 읽고 의견을 나눠보세요.",
            startDate = "2025.01.15",
            endDate = "2025.02.15",
            members = room.participants,
            maxMembers = room.maxParticipants,
            daysLeft = room.endDate ?: 0,
            genre = genres.getOrElse(room.genreIndex) { genres[0] },
            bookData = bookData,
            recommendations = getRecommendations(room.id) // 추천 모임방에도 추천 제공
        )
        
        roomDetailsCache[room.id] = roomDetail
    }

    private fun getRecommendations(roomId: Int): List<GroupCardItemRoomData> {
        // 추천 모임방 더미데이터 풀
        val recommendationPool = listOf(
            // 문학 관련 추천
            GroupCardItemRoomData(1001, "한국 근현대 소설 읽기", 18, 25, true, 3, R.drawable.bookcover_sample, 0),
            GroupCardItemRoomData(1002, "일본 문학 애호가들", 22, 30, true, 1, R.drawable.bookcover_sample, 0),
            GroupCardItemRoomData(1003, "시 읽기 모임", 16, 25, true, 2, R.drawable.bookcover_sample, 0),
            GroupCardItemRoomData(1004, "해외문학 번역서 읽기", 15, 22, true, 3, R.drawable.bookcover_sample, 0, true),
            GroupCardItemRoomData(1005, "고전 문학 탐구", 20, 25, true, 5, R.drawable.bookcover_sample, 0),
            
            // 과학·IT 관련 추천  
            GroupCardItemRoomData(1006, "SF 소설 탐험대", 12, 20, true, 7, R.drawable.bookcover_sample, 1),
            GroupCardItemRoomData(1007, "과학도서 함께 읽기", 7, 15, true, 9, R.drawable.bookcover_sample, 1),
            GroupCardItemRoomData(1008, "컴퓨터 과학 스터디", 14, 18, true, 4, R.drawable.bookcover_sample, 1),
            GroupCardItemRoomData(1009, "물리학 입문서 모임", 10, 16, true, 6, R.drawable.bookcover_sample, 1),
            
            // 사회과학 관련 추천
            GroupCardItemRoomData(1010, "경제경영서 스터디", 9, 12, true, 6, R.drawable.bookcover_sample, 2),
            GroupCardItemRoomData(1011, "사회학 도서 토론", 13, 18, true, 4, R.drawable.bookcover_sample, 2),
            GroupCardItemRoomData(1012, "정치학 입문 모임", 11, 15, true, 8, R.drawable.bookcover_sample, 2),
            
            // 인문학 관련 추천
            GroupCardItemRoomData(1013, "철학 에세이 읽기 모임", 8, 15, true, 5, R.drawable.bookcover_sample, 3),
            GroupCardItemRoomData(1014, "인문학 고전 읽기", 20, 25, true, 5, R.drawable.bookcover_sample, 3, true),
            GroupCardItemRoomData(1015, "심리학 도서 스터디", 10, 16, true, 7, R.drawable.bookcover_sample, 3),
            GroupCardItemRoomData(1016, "역사서 탐구 모임", 11, 16, true, 8, R.drawable.bookcover_sample, 3),
            
            // 예술 관련 추천
            GroupCardItemRoomData(1017, "미술사 도서 읽기", 14, 20, true, 3, R.drawable.bookcover_sample, 4),
            GroupCardItemRoomData(1018, "음악 관련 서적 모임", 12, 18, true, 5, R.drawable.bookcover_sample, 4),
            
            // 기타 장르
            GroupCardItemRoomData(1019, "로맨스 소설 감상회", 14, 20, true, 4, R.drawable.bookcover_sample, 0),
            GroupCardItemRoomData(1020, "미스터리 소설 동호회", 15, 18, true, 2, R.drawable.bookcover_sample, 0, true),
            GroupCardItemRoomData(1021, "자기계발서 함께 읽기", 25, 30, true, 3, R.drawable.bookcover_sample, 2, true),
            GroupCardItemRoomData(1022, "판타지 소설 동호회", 24, 30, true, 1, R.drawable.bookcover_sample, 0),
            GroupCardItemRoomData(1023, "여행 에세이 모임", 13, 18, true, 4, R.drawable.bookcover_sample, 3),
            GroupCardItemRoomData(1024, "추리소설 마니아", 19, 24, true, 6, R.drawable.bookcover_sample, 0)
        )
        
        // 현재 방과 관련 없는 추천을 제공하기 위해 현재 roomId와 다른 것들만 필터링
        val filteredRecommendations = recommendationPool.filter { it.id != roomId }
        
        // 랜덤하게 3-5개의 추천 반환
        return filteredRecommendations.shuffled().take(5)
    }
}