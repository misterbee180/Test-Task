package com.deviousindustries.testtask.classes;

import android.database.Cursor;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.deviousindustries.testtask.DatabaseAccess;
import com.deviousindustries.testtask.Utilities;

import static com.deviousindustries.testtask.constants.ConstantsKt.*;

@Entity(tableName = "tblTask")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public long flngTaskID;
    public long flngTaskDetailID;
    public long flngTimeID;
    public int fintTaskType;
    public long flngTaskTypeID;
    public long fdtmCreated;
    public long fdtmDeleted;
    public long flngOneOff;

    //Create task detail object
    @Ignore
    public String fstrTitle;
    @Ignore
    public String fstrDescription;

    public Task(){
        flngTaskID = NULL_OBJECT;
        flngTimeID = NULL_OBJECT;
        flngTaskDetailID = NULL_OBJECT;
        fdtmCreated = Utilities.Companion.getCurrentCalendar().getTimeInMillis();
        fdtmDeleted = NULL_DATE;
        fintTaskType = 0;
        flngTaskTypeID = NULL_OBJECT;
        flngOneOff = NULL_OBJECT;;

        //Create task detail object
        fstrTitle = "";
        fstrDescription ="";
    }

    public Task(Long plngTaskID){
        this();
        try(Cursor tblTask = DatabaseAccess.getRecordsFromTable("tblTask", "flngTaskID", plngTaskID)) {
            if(tblTask.moveToFirst()){
                flngTaskID = tblTask.getLong(tblTask.getColumnIndex("flngTaskID"));
                flngTimeID = tblTask.getLong(tblTask.getColumnIndex("flngTimeID"));
                flngTaskDetailID = tblTask.getLong(tblTask.getColumnIndex("flngTaskDetailID"));
                TaskDetail td = new TaskDetail(flngTaskDetailID);
                fstrTitle = td.fstrTitle;
                fstrDescription = td.fstrDescription;
                fdtmCreated = tblTask.getLong(tblTask.getColumnIndex("fdtmCreated"));
                fdtmDeleted = tblTask.getLong(tblTask.getColumnIndex("fdtmDeleted"));
                fintTaskType = tblTask.getInt(tblTask.getColumnIndex("fintTaskType"));
                flngTaskTypeID = tblTask.getLong(tblTask.getColumnIndex("flngTaskTypeID"));
                flngOneOff = tblTask.getLong(tblTask.getColumnIndex("flngOneOff"));
            }
        }
    }

    public Task(long plngTaskID,
                long plngTimeID,
                long pdtmCreated,
                String pstrTitle,
                String pstrDescription,
                Long pdtmDeleted,
                int pintTaskType,
                long plngTaskTypeID,
                long plngOneOff){
        flngTaskID = plngTaskID;
        flngTimeID = plngTimeID;
        fdtmCreated = pdtmCreated;
        fdtmDeleted = pdtmDeleted;
        fstrTitle = pstrTitle;
        fstrDescription = pstrDescription;
        fintTaskType = pintTaskType;
        flngTaskTypeID = plngTaskTypeID;
        flngOneOff = plngOneOff;

        //Handles updates and initial creates
        flngTaskDetailID = DatabaseAccess.addRecordToTable("tblTaskDetail",
                new String[] {"fstrTitle", "fstrDescription"},
                new Object[] {fstrTitle, fstrDescription});

        flngTaskID = DatabaseAccess.addRecordToTable("tblTask",
                new String[] {"flngTaskDetailID", "flngTimeID", "fintTaskType", "flngTaskTypeID", "fdtmCreated","fdtmDeleted","flngOneOff"},
                new Object[] {flngTaskDetailID,
                        flngTimeID,
                        fintTaskType,
                        flngTaskTypeID,
                        fdtmCreated,
                        fdtmDeleted,
                        flngOneOff},
                "flngTaskID",
                flngTaskID);
    }

    public void deleteTask(){
        try{
            DatabaseAccess.mDatabase.beginTransaction();
            DatabaseAccess.updateRecordFromTable("tblTask",
                    "flngTaskID",
                    flngTaskID,
                    new String[]{"fdtmDeleted"},
                    new Object[]{Utilities.Companion.getCurrentCalendar().getTimeInMillis()});

            finishActiveInstances(3);

            if(flngTimeID != NULL_OBJECT){ //For events and scenarios where there's no mTime associated to task
                Time tempTime = Time.getInstance(flngTimeID);
                if(tempTime.fblnSession == false){
                    tempTime.deleteTime();
                }
            }

            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        }
        DatabaseAccess.mDatabase.endTransaction();
    }

    public TaskInstance generateInstance(long pdtmFrom,
                                  long pdtmTo,
                                  boolean pblnFromTime,
                                  boolean pblnToTime,
                                  boolean pblnToDate,
                                  long plngSessionID){

        return new TaskInstance(NULL_OBJECT,
                flngTaskID,
                flngTaskDetailID,
                pdtmFrom,
                pdtmTo,
                pblnFromTime,
                pblnToTime,
                pblnToDate,
                flngOneOff == NULL_OBJECT ? plngSessionID : flngOneOff); //replace NULL_OBJECT w/ proper session detail id if one off
    }

    public void clearInstances(){
        DatabaseAccess.deleteRecordFromTable("tblTaskInstance", "flngTaskID", flngTaskID);
    }

    public void updateTaskDetails(String pstrTitle,
                                  String pstrDescription){
        DatabaseAccess.updateRecordFromTable("tblTaskDetail",
                "flngTaskDetailID",
                flngTaskDetailID,
                new String[]{"fstrTitle","fstrDescription"},
                new Object[]{pstrTitle,pstrDescription});
    }

    public void replaceTimeId(long plngTimeId){
        flngTimeID = plngTimeId;
        DatabaseAccess.taskDatabaseDao.updateTask(this);
    }

    public void updateOneOff(long plngOneOff){
        DatabaseAccess.updateRecordFromTable("tblTask",
                "flngTaskID",
                flngTaskID,
                new String[]{"flngOneOff"},
                new Object[]{plngOneOff});
    }

    public void finishActiveInstances(int pintCompleteType){
        try(Cursor curInstances = DatabaseAccess.retrieveActiveTaskInstanceFromTask(flngTaskID)) {
            while (curInstances.moveToNext()) {
                TaskInstance ti = new TaskInstance(curInstances.getLong(curInstances.getColumnIndex("flngInstanceID")));
                ti.finishInstance(pintCompleteType);
            }
        }
    }
}
