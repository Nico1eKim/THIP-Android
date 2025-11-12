package com.texthip.thip.data.manager


enum class Genre(
    val displayKey: String,
    val apiCategory: String,
    val networkApiCategory: String = apiCategory
) {
    LITERATURE("literature", "문학"),
    SCIENCE_IT("science_it", "과학·IT", "과학·IT"),
    SOCIAL_SCIENCE("social_science", "사회과학"),
    HUMANITIES("humanities", "인문학"),
    ART("art", "예술");

    companion object {
        fun getDefault() = LITERATURE

        fun fromDisplayKey(displayKey: String): Genre? {
            return entries.find { it.displayKey == displayKey }
        }
    }
}