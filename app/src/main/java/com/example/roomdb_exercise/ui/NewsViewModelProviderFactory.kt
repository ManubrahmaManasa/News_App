package com.example.roomdb_exercise.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.roomdb_exercise.repository.NewsRepository

class NewsViewModelProviderFactory(val app:Application,val newsRepo:NewsRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(app,newsRepo) as T
    }
}