package com.example.adproject.model

data class JoinClassResponse(
    val code: Int,
    val msg: String?,
    val data: StudentClass? = null // 现在后端是 null；以后若返回班级信息可直接用
)
