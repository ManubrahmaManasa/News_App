package com.example.roomdb_exercise.repository

import com.example.roomdb_exercise.api.RetrofitInstance
import com.example.roomdb_exercise.db.ArticleDatabase
import com.example.roomdb_exercise.models.Article

class NewsRepository(val db:ArticleDatabase) {
    suspend fun getHeadlines(countryCode:String,pageNumber:Int)=
        RetrofitInstance.api.getHeadlines(countryCode,pageNumber)

    suspend fun searchNews(searchQuery:String,pageNumber: Int)=
        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    fun getFavouriteNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticles(article: Article) = db.getArticleDao().deleteArticle(article)
}