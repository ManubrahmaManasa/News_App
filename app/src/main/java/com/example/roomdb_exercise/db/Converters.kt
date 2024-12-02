package com.example.roomdb_exercise.db

import androidx.room.TypeConverter
import com.example.roomdb_exercise.models.Sources
import javax.xml.transform.Source

class Converters {
    @TypeConverter
    fun fromSource(source: Sources):String {
        return source.name
    }

    @TypeConverter
    fun toSource(name:String) :Sources{
        return Sources(name,name)
    }
}