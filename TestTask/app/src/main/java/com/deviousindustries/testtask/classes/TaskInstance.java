package com.deviousindustries.testtask.classes;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.deviousindustries.testtask.DatabaseAccess;
import com.deviousindustries.testtask.Utilities;

import static com.deviousindustries.testtask.constants.ConstantsKt.*;

@Entity(tableName = "tblTaskInstance")
public class TaskInstance {
    @PrimaryKey(autoGenerate = true)
    public long flngInstanceID;
    public long flngTaskID;
    public long flngTaskDetailID;
    public long fdtmFrom;
    public long fdtmTo;
    @NonNull
    public Boolean fblnFromTime;
    @NonNull
    public Boolean fblnToTime;
    @NonNull
    public Boolean fblnToDate;
    public long fdtmCreated;
    public long fdtmCompleted;
    public long fdtmSystemCompleted;
    public long fdtmDeleted;
    public long fdtmEdited;
    public long flngSessionID;

    @Ignore
    public String fstrTitle;
    @Ignore
    public String fstrDescription;

    public TaskInstance(){
        flngInstanceID = NULL_OBJECT;
        flngTaskID = NULL_OBJECT;
        flngTaskDetailID = NULL_OBJECT;
        fstrTitle = "";
        fstrDescription = "";
        fdtmFrom = NULL_DATE;
        fdtmTo = NULL_DATE;
        fblnFromTime = false;
        fblnToTime = false;
        fblnToDate = false;
        fdtmCreated = NULL_DATE;
        fdtmCompleted = NULL_DATE;
        fdtmSystemCompleted = NULL_DATE;
        fdtmDeleted = NULL_DATE;
        fdtmEdited = NULL_DATE;
        flngSessionID = NULL_OBJECT;
    }

    public TaskInstance(long plngInstanceID){
        this();
        try(Cursor tblTaskInstance = DatabaseAccess.getRecordsFromTable("tblTaskInstance", "flngInstanceID", plngInstanceID)){
            if(tblTaskInstance.moveToFirst()){
                flngInstanceID = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("flngInstanceID"));
                flngTaskID = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("flngTaskID"));
                flngTaskDetailID = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("flngTaskDetailID"));
                TaskDetail td = new TaskDetail(flngTaskDetailID);
                fstrTitle = td.fstrTitle;
                fstrDescription = td.fstrDescription;
                fdtmFrom = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmFrom"));
                fdtmTo = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmTo"));
                fblnFromTime = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fblnFromTime")) == 1;
                fblnToTime = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fblnToTime")) == 1;
                fblnToDate = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fblnToDate")) == 1;
                fdtmCreated = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmCreated"));
                fdtmCompleted = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmCompleted"));
                fdtmSystemCompleted = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmSystemCompleted"));
                fdtmDeleted = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmDeleted"));
                fdtmEdited = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmEdited"));
                flngSessionID = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("flngSessionID"));
            }
        }
    }

    public TaskInstance(long plngInstanceID,
                 long plngTaskID,
                 long plngTaskDetailID,
                 long pdtmFrom,
                 long pdtmTo,
                 Boolean pblnFromTime,
                 Boolean pblnToTime,
                 boolean pblnToDate,
                 long plngSessionID){
        this();
        flngInstanceID = plngInstanceID;
        flngTaskID = plngTaskID;
        flngTaskDetailID = plngTaskDetailID;
        fdtmFrom = pdtmFrom;
        fblnFromTime = pblnFromTime;
        fdtmTo = pdtmTo;
        fblnToTime = pblnToTime;
        fblnToDate = pblnToDate;
        if(flngInstanceID == NULL_OBJECT){
            fdtmCreated = Utilities.Companion.getCurrentCalendar().getTimeInMillis();
        } else {
            fdtmEdited = Utilities.Companion.getCurrentCalendar().getTimeInMillis();
        }
        flngSessionID = plngSessionID;

        createTaskInstance();
    }

    private void createTaskInstance(){
        flngInstanceID = DatabaseAccess.addRecordToTable("tblTaskInstance",
                new String[] {"flngTaskID", "flngTaskDetailID", "fdtmFrom", "fdtmTo", "fblnFromTime", "fblnToTime", "fblnToDate", "fdtmCreated", "fdtmEdited", "flngSessionID"},
                new Object[] {flngTaskID, flngTaskDetailID, fdtmFrom, fdtmTo, fblnFromTime, fblnToTime, fblnToDate, fdtmCreated, fdtmEdited, flngSessionID},
                "flngInstanceID",
                flngInstanceID);
    }

    public void finishInstance(int pintCompleteType){
        String[] strCompleteType = new String[1];
        switch (pintCompleteType){
            case 1:
                strCompleteType[0] = "fdtmCompleted";
                break;
            case 2:
                strCompleteType[0] = "fdtmSystemCompleted";
                break;
            case 3:
                strCompleteType[0] = "fdtmDeleted";
                break;
        }
        DatabaseAccess.updateRecordFromTable("tblTaskInstance",
                "flngInstanceID",
                flngInstanceID,
                strCompleteType,
                new Object[] {Utilities.Companion.getCurrentCalendar().getTimeInMillis()});
    }
}
