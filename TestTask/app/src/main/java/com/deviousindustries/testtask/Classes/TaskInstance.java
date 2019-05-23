package com.deviousindustries.testtask.Classes;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.deviousindustries.testtask.DatabaseAccess;
import com.deviousindustries.testtask.Viewer_Tasklist;

@Entity(tableName = "tblTaskInstance")
public class TaskInstance {
    @PrimaryKey(autoGenerate = true)
    public long flngInstanceID;

    @ColumnInfo
    public long flngTaskID;

    @ColumnInfo
    public long flngTaskDetailID;

    @ColumnInfo
    public long fdtmFrom;

    @ColumnInfo
    public long fdtmTo;

    @ColumnInfo
    @NonNull
    public Boolean fblnFromTime;

    @ColumnInfo
    @NonNull
    public Boolean fblnToTime;

    @ColumnInfo
    @NonNull
    public Boolean fblnToDate;

    @ColumnInfo
    public long fdtmCreated;

    @ColumnInfo
    public long fdtmCompleted;

    @ColumnInfo
    public long fdtmSystemCompleted;

    @ColumnInfo
    public long fdtmDeleted;

    @ColumnInfo
    public long fdtmEdited;

    @ColumnInfo
    public long flngSessionID;

    @Ignore
    public String fstrTitle;
    @Ignore
    public String fstrDescription;

    public TaskInstance(){
        flngInstanceID = -1;
        flngTaskID = -1;
        flngTaskDetailID = -1;
        fstrTitle = "";
        fstrDescription = "";
        fdtmFrom = -1;
        fdtmTo = -1;
        fblnFromTime = false;
        fblnToTime = false;
        fblnToDate = false;
        fdtmCreated = -1;
        fdtmCompleted = -1;
        fdtmSystemCompleted = -1;
        fdtmDeleted = -1;
        fdtmEdited = -1;
        flngSessionID = -1;
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
        if(flngInstanceID == -1){
            fdtmCreated = Viewer_Tasklist.getCurrentCalendar().getTimeInMillis();
        } else {
            fdtmEdited = Viewer_Tasklist.getCurrentCalendar().getTimeInMillis();
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
                new Object[] {Viewer_Tasklist.getCurrentCalendar().getTimeInMillis()});
    }
}
