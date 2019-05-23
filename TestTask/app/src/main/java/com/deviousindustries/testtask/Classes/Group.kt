package com.deviousindustries.testtask.Classes

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblGroup")
public class Group {
    @PrimaryKey(autoGenerate = true)
    public var flngGroupID : Long = 0L

    @ColumnInfo
    @NonNull
    public var fstrTitle: String = ""
}