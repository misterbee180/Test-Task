package com.deviousindustries.testtask.Classes

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblEvent")
public class Event {
    @PrimaryKey(autoGenerate = true)
    public var flngEventID: Long = 0L

    @ColumnInfo
    @NonNull
    public var fstrTitle : String = ""

    @ColumnInfo
    @NonNull
    public var fstrDescription: String = ""
}