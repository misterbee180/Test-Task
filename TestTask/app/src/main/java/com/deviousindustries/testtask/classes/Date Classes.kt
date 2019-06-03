package com.deviousindustries.testtask.classes

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.deviousindustries.testtask.constants.NULL_OBJECT

@Entity(tableName = "tblMonth")
class Month {
    @PrimaryKey(autoGenerate = true)
    var flngMonthID: Long = NULL_OBJECT
    var fblnFirst: Boolean = false
    var fblnMiddle: Boolean = false
    var fblnLast: Boolean = false
    var fblnAfterWkn: Boolean = false
    @NonNull
    var fstrSpecific: String = ""
}

@Entity(tableName = "tblWeek")
class Week {
    @PrimaryKey(autoGenerate = true)
    var flngWeekID: Long = NULL_OBJECT
    var fblnMonday: Boolean = false
    var fblnTuesday: Boolean = false
    var fblnWednesday: Boolean = false
    var fblnThursday: Boolean = false
    var fblnFriday: Boolean = false
    var fblnSaturday: Boolean = false
    var fblnSunday: Boolean = false
}

@Entity(tableName = "tblDay")
class Day {
    @PrimaryKey(autoGenerate = true)
    var flngDayID: Long = NULL_OBJECT
}

@Entity(tableName = "tblYear")
class Year {
    @PrimaryKey(autoGenerate = true)
    var flngYearID: Long = NULL_OBJECT
}