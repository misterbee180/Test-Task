package com.deviousindustries.testtask.Classes

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblMonth")
class Month {
    @PrimaryKey(autoGenerate = true)
    var flngMonthID: Long = 0L

    @ColumnInfo
    var fblnFirst: Boolean = false

    @ColumnInfo
    var fblnMiddle: Boolean = false

    @ColumnInfo
    var fblnLast: Boolean = false

    @ColumnInfo
    var fblnAfterWkn: Boolean = false

    @ColumnInfo
    @NonNull
    var fstrSpecific: String? = null
}

@Entity(tableName = "tblWeek")
class Week {
    @PrimaryKey(autoGenerate = true)
    var flngWeekID: Long = 0L

    @ColumnInfo
    var fblnMonday: Boolean = false

    @ColumnInfo
    var fblnTuesday: Boolean = false

    @ColumnInfo
    var fblnWednesday: Boolean = false

    @ColumnInfo
    var fblnThursday: Boolean = false

    @ColumnInfo
    var fblnFriday: Boolean = false

    @ColumnInfo
    var fblnSaturday: Boolean = false

    @ColumnInfo
    var fblnSunday: Boolean = false
}

@Entity(tableName = "tblDay")
class Day {
    @PrimaryKey
    var flngDayID: Long = 0L
}

@Entity(tableName = "tblYear")
class Year {
    @PrimaryKey
    var flngYearID: Long = 0L
}