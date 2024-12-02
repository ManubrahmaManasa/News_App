package com.example.roomdb_exercise.models

data class NewsResponse(
    val articles:MutableList<Article>,
    val status:String,
    val totalResults:Int
)
