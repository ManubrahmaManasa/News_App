package com.example.roomdb_exercise.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import javax.xml.transform.Source

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true)
    var id:Int? = null,
    val author:String,
    val content:String,
    val description:String,
    val publishedAt:String,
    val source:Sources,
    val title:String,
    val url:String,
    val urlToImage:String
):Serializable
