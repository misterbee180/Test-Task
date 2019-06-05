package com.deviousindustries.testtask.classes

import androidx.room.ColumnInfo

data class Session(
        @ColumnInfo(name="fstrTitle") var title: String,
        //@ColumnInfo(name="fstrDescription") var description: String,
        @ColumnInfo(name="flngTimeID") var timeID: Long
)

class SpinnerElement(val text: String, val id: Int){
    override fun toString(): String {
        return text
    }
}