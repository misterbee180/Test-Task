package com.deviousindustries.testtask.classes

import androidx.room.ColumnInfo

data class Session(
        @ColumnInfo(name="fstrTitle") var title: String,
        //@ColumnInfo(name="fstrDescription") var description: String,
        @ColumnInfo(name="flngTimeID") var timeID: Long
)

interface TaskListRecord{
    companion object{
        const val NULL_SECTION = -1
        const val NULL_ID = -1L
    }

    fun getRecordID(): Long
    fun getElementType(): Int
    fun getItemTitle(): String
    fun getPriority(): Int
    fun getGroup(): Int
}