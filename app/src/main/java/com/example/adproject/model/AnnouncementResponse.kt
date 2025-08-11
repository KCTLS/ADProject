// model/AnnouncementResponse.kt
package com.example.adproject.model

data class AnnouncementResponse(
    val code: Int,
    val msg: String?,
    val data: AnnouncementList?
)

data class AnnouncementList(
    val list: List<AnnouncementItem>
)

data class AnnouncementItem(
    val title: String,
    val content: String,
    val createTime: List<Int>, // [yyyy, M, d, HH, mm]
    val classId: Int? = null,        // ✅ 新增：所属班级 id（我们在拉取时填上）
    val className: String? = null
)

// 小工具：把 createTime 转成可读字符串
fun AnnouncementItem.displayTime(): String {
    if (createTime.size < 5) return ""
    val y = createTime[0]
    val m = createTime[1]
    val d = createTime[2]
    val hh = createTime[3]
    val mm = createTime[4]
    return String.format("%04d-%02d-%02d %02d:%02d", y, m, d, hh, mm)
}