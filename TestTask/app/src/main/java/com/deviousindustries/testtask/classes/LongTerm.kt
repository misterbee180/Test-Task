package com.deviousindustries.testtask.classes

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.deviousindustries.testtask.constants.NULL_OBJECT

@Entity(tableName = "tblLongTerm")
public class LongTerm {
    @PrimaryKey(autoGenerate = true)
    public var flngLongTermID: Long = NULL_OBJECT
    @NonNull
    public var fstrTitle : String = ""
    @NonNull
    public var fstrDescription: String = ""
}