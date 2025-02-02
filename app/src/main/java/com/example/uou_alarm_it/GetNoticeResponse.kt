package com.example.uou_alarm_it

data class GetNoticeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Result
) {
    data class Result(
        val content: ArrayList<Notice>,
        val pageable: Pageable,
        val totalPages: Int,
        val totalElements: Int,
        val last: Boolean,
        val size: Int,
        val number: Int,
        val sort: Sort,
        val numberOfElements: Int,
        val first: Boolean,
        val empty: Boolean
    ) {
        data class Pageable(
            val pageNumber: Int,
            val pageSize: Int,
            val sort: Sort,
            val offset: Int,
            val paged: Boolean,
            val unpaged: Boolean
        ) {
            data class Sort(
                val sorted: Boolean,
                val empty: Boolean,
                val unsorted: Boolean
            )
        }

        data class Sort(
            val sorted: Boolean,
            val empty: Boolean,
            val unsorted: Boolean
        )
    }
}