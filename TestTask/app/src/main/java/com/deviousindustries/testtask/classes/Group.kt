package com.deviousindustries.testtask.classes

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.deviousindustries.testtask.constants.NULL_OBJECT

@Entity(tableName = "tblGroup")
public class Group {
    @PrimaryKey(autoGenerate = true)
    public var flngGroupID : Long = NULL_OBJECT
    @NonNull
    public var fstrTitle: String = ""
}