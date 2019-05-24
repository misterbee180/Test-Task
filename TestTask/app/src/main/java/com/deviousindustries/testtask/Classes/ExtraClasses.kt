package com.deviousindustries.testtask.Classes

import androidx.room.ColumnInfo

data class Session(
        @ColumnInfo(name="fstrTitle") var title: String,
        //@ColumnInfo(name="fstrDescription") var description: String,
        @ColumnInfo(name="flngTimeID") var timeID: Long
)