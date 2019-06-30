package com.deviousindustries.testtask.classes

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.deviousindustries.testtask.DatabaseAccess
import com.deviousindustries.testtask.constants.NULL_OBJECT

@Entity(tableName = "tblMonth")
class Month : Cloneable {
    @PrimaryKey(autoGenerate = true)
    var flngMonthID: Long = NULL_OBJECT
    var fblnFirst: Boolean = false
    var fblnMiddle: Boolean = false
    var fblnLast: Boolean = false
    var fblnAfterWkn: Boolean = false
    @NonNull
    var fstrSpecific: String = ""

    companion object{
        fun getInstance(monthId: Long): Month{
            if(monthId != NULL_OBJECT){
                return DatabaseAccess.taskDatabaseDao.loadMonth(monthId)
            } else {
                return Month()
            }
        }

        fun delete(id: Long){
            DatabaseAccess.taskDatabaseDao.deleteMonth(Month().apply{ flngMonthID = id })
        }
    }

    public override fun clone(): Any {
        return super.clone()
    }
}

@Entity(tableName = "tblWeek")
class Week : Cloneable {
    @PrimaryKey(autoGenerate = true)
    var flngWeekID: Long = NULL_OBJECT
    var fblnMonday: Boolean = false
    var fblnTuesday: Boolean = false
    var fblnWednesday: Boolean = false
    var fblnThursday: Boolean = false
    var fblnFriday: Boolean = false
    var fblnSaturday: Boolean = false
    var fblnSunday: Boolean = false

    companion object{
        fun getInstance(weekId: Long): Week{
            if(weekId != NULL_OBJECT){
                return DatabaseAccess.taskDatabaseDao.loadWeek(weekId)
            } else {
                return Week()
            }
        }

        fun delete(id: Long){
            DatabaseAccess.taskDatabaseDao.deleteWeek(Week().apply{ flngWeekID = id })
        }
    }

    public override fun clone(): Any {
        return super.clone()
    }
}

@Entity(tableName = "tblDay")
class Day {
    @PrimaryKey(autoGenerate = true)
    var flngDayID: Long = NULL_OBJECT

    companion object{
        fun delete(id: Long){
            DatabaseAccess.taskDatabaseDao.deleteDay(Day().apply{ flngDayID = id })
        }
    }

}

@Entity(tableName = "tblYear")
class Year {
    @PrimaryKey(autoGenerate = true)
    var flngYearID: Long = NULL_OBJECT

    companion object{
        fun delete(id: Long){
            DatabaseAccess.taskDatabaseDao.deleteYear(Year().apply{ flngYearID = id })
        }
    }
}