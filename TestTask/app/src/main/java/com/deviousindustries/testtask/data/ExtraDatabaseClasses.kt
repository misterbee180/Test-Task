package com.deviousindustries.testtask.data

import androidx.room.ColumnInfo

data class Session(
        @ColumnInfo(name = "fstrTitle") var title: String,
        //@ColumnInfo(name="fstrDescription") var description: String,
        @ColumnInfo(name = "flngTimeID") var timeID: Long

) {
    override fun toString(): String {
        return title
    }
}

class TaskListInstances(
        val flngInstanceID: Long,
        val fdtmFrom: Long,
        val fdtmTo: Long,
        val fblnFromTime: Int,
        val fblnToTime: Int,
        val fblnToDate: Int,
        val fdtmCreated: Long,
        val fstrTitle: String,
        val flngGroupKey: Long,
        val fstrGroupType: String,
        val fstrGroupTitle: String) {

    val hasFromTime: Boolean
        get() {
            return fblnFromTime == 1
        }

    val hasToTime: Boolean
        get() {
            return fblnToTime == 1
        }

    val hasToDate: Boolean
        get() {
            return fblnToDate == 1
        }
}