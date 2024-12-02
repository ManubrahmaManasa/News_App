package com.example.roomdb_exercise.api

import com.example.roomdb_exercise.models.NewsResponse
import com.example.roomdb_exercise.util.Constants.Companion.API_KEY
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("country") countryCode:String = "us",
        @Query("page") pageNumber: Int = 1,
        @Query("apiKey") apiKey:String = API_KEY):NewsResponse

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q") searchQuery:String,
        @Query("page") pageNumber:Int = 1,
        @Query("apiKey")apiKey:String = API_KEY
    ):NewsResponse
}