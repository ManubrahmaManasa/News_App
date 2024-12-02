package com.example.roomdb_exercise.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomdb_exercise.models.Article
import com.example.roomdb_exercise.models.NewsResponse
import com.example.roomdb_exercise.repository.NewsRepository
import com.example.roomdb_exercise.util.Resource
import kotlinx.coroutines.launch
import okhttp3.Response
import okio.IOException

class NewsViewModel(app:Application,val newsRepository:NewsRepository):AndroidViewModel(app) {
    val headLines:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headLinesPage = 1
    var headLineResponse:NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse:NewsResponse? = null
    var newSearchQuery:String? = null
    var oldSearchQuery:String? = null

    init {
        getHeadlines("us")
    }

    fun getHeadlines(countryCode:String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery:String) = viewModelScope.launch {
        searchInternet(searchQuery)
    }

    private fun handleHeadlinesResponse(response: retrofit2.Response<NewsResponse>):Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?.let { resultResponse ->
                headLinesPage++
                if(headLineResponse == null){
                    headLineResponse = resultResponse
                }else{
                    val oldArticles  = headLineResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headLineResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
    private fun handleSearchNewsResponse(response: retrofit2.Response<NewsResponse>):Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?.let { resultResponse ->
                if(searchNewsResponse == null || newSearchQuery != oldSearchQuery){
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponse
                }else{
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun addToFavourites(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getFavouriteNews() = newsRepository.getFavouriteNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticles(article)
    }

    fun internetConnection(context: Context):Boolean{
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->true
                    else -> false
                }
            }?:false
        }
    }

    private suspend fun headlinesInternet(countryCode:String){
        headLines.postValue(Resource.Loading())
        try{
            if(internetConnection(this.getApplication())){
                val response= newsRepository.getHeadlines(countryCode,headLinesPage)
                headLines.postValue(handleHeadlinesResponse(response as retrofit2.Response<NewsResponse>))
            }else{
                headLines.postValue(Resource.Error("No Internet connection"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException -> headLines.postValue(Resource.Error("Unable to connect"))
                else -> headLines.postValue(Resource.Error("No Signal"))
            }
        }
    }

    private suspend fun searchInternet(searchQuery:String){
        newSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())

        try{
            if(internetConnection(this.getApplication())){
                val response = newsRepository.searchNews(searchQuery,headLinesPage)
                searchNews.postValue(handleSearchNewsResponse(response as retrofit2.Response<NewsResponse>))
            }else{
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException -> searchNews.postValue(Resource.Error("Unable to connect"))
                else -> searchNews.postValue(Resource.Error("No Signal"))
            }
        }
    }
}