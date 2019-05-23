package com.deviousindustries.testtask.Data

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.deviousindustries.testtask.Classes.*

@Dao
interface TaskDatabaseDao {

    //region Class Inserts
    @Insert
    fun insertTask(task: Task)

    @Insert
    fun insertTaskInstance(taskInstance: TaskInstance)

    @Insert
    fun insertTaskDetail(taskDetail: TaskDetail)

    @Insert
    fun insertTime(time: Time)

    @Insert
    fun insertTimeInstance(timeInstance: TimeInstance)

    @Insert
    fun insertLongTerm(longTerm: LongTerm)

    @Insert
    fun insertGroup(group: Group)

    @Insert
    fun insertEvent(event: Event)

    @Insert
    fun insertDay(day: Day)

    @Insert
    fun insertWeek(week: Week)

    @Insert
    fun insertMonth(month: Month)

    @Insert
    fun insertYear(year: Year)
    //endregion

    //region Class Deletes
    @Delete
    fun deleteTask(task: Task)

    @Delete
    fun deleteTaskInstance(taskInstance: TaskInstance)

    @Delete
    fun deleteTaskDetail(taskDetail: TaskDetail)

    @Delete
    fun deleteTime(time: Time)

    @Delete
    fun deleteTimeInstance(timeInstance: TimeInstance)

    @Delete
    fun deleteLongTerm(longTerm: LongTerm)

    @Delete
    fun deleteGroup(group: Group)

    @Delete
    fun deleteEvent(event: Event)

    @Delete
    fun deleteDay(day: Day)

    @Delete
    fun deleteWeek(week: Week)

    @Delete
    fun deleteMonth(month: Month)

    @Delete
    fun deleteYear(year: Year)
    //endregion

    //region Selects
    @Query("SELECT * FROM tblTask WHERE flngTaskID = :ID")
    fun loadTask(ID: Long): Cursor

    @Query("SELECT * FROM tblTaskDetail WHERE flngTaskDetailID = :ID")
    fun loadTaskDetail(ID: Long): Cursor

    @Query("SELECT * FROM tblTaskInstance WHERE flngInstanceID = :ID")
    fun loadTaskInstance(ID: Long): Cursor

    @Query("SELECT * FROM tblTime WHERE flngTimeID = :ID") fun loadTime(ID: Long): Cursor

    @Query("SELECT * FROM tblTimeInstance WHERE flngGenerationID = :ID")
    fun loadTimeInstance(ID: Long): Cursor

    @Query("SELECT * FROM tblEvent WHERE flngEventID = :ID")
    fun loadEvent(ID: Long): Cursor

    @Query("SELECT * FROM tblGroup WHERE flngGroupID = :ID")
    fun loadGroup(ID: Long): Cursor

    @Query("SELECT * FROM tblLongTerm WHERE flngLongTermID = :ID")
    fun loadLongTerm(ID: Long): Cursor

    @Query("SELECT * FROM tblDay WHERE flngDayID = :ID")
    fun loadDay(ID: Long): Cursor

    @Query("SELECT * FROM tblWeek WHERE flngWeekID = :ID")
    fun loadWeek(ID: Long): Cursor

    @Query("SELECT * FROM tblMonth WHERE flngMonthID = :ID")
    fun loadMonth(ID: Long): Cursor

    @Query("SELECT * FROM tblYear WHERE flngYearID = :ID")
    fun loadYear(ID: Long): Cursor

    //endregion
}