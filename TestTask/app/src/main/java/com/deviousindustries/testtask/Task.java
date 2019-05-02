package com.deviousindustries.testtask;

import android.database.Cursor;

public class Task {
    long mlngTaskID;
    long mlngTimeID;
    int mintTaskType;
    long mlngTaskTypeID;
    private long mlngTaskDetailID;
    long mlngOneOff;
    //Create task detail object
    String mstrTitle;
    String mstrDescription;

    private long mdtmCreated;
    long mdtmDeleted;

    public Task(){
        mlngTaskID = -1;
        mlngTimeID = -1;
        mlngTaskDetailID = -1;
        mdtmCreated = Viewer_Tasklist.getCurrentCalendar().getTimeInMillis();
        mdtmDeleted = -1;
        mintTaskType = 0;
        mlngTaskTypeID = -1;
        mlngOneOff = -1;

        //Create task detail object
        mstrTitle = "";
        mstrDescription ="";
    }

    public Task(Long plngTaskID){
        this();
        try(Cursor tblTask = DatabaseAccess.getRecordsFromTable("tblTask", "flngTaskID", plngTaskID)) {
            if(tblTask.moveToFirst()){
                mlngTaskID = tblTask.getLong(tblTask.getColumnIndex("flngTaskID"));
                mlngTimeID = tblTask.getLong(tblTask.getColumnIndex("flngTimeID"));
                mlngTaskDetailID = tblTask.getLong(tblTask.getColumnIndex("flngTaskDetailID"));
                TaskDetail td = new TaskDetail(mlngTaskDetailID);
                mstrTitle = td.mstrTitle;
                mstrDescription = td.mstrDescription;
                mdtmCreated = tblTask.getLong(tblTask.getColumnIndex("fdtmCreated"));
                mdtmDeleted = tblTask.getLong(tblTask.getColumnIndex("fdtmDeleted"));
                mintTaskType = tblTask.getInt(tblTask.getColumnIndex("fintTaskType"));
                mlngTaskTypeID = tblTask.getLong(tblTask.getColumnIndex("flngTaskTypeID"));
                mlngOneOff = tblTask.getLong(tblTask.getColumnIndex("flngOneOff"));
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
        mlngTaskID = plngTaskID;
        mlngTimeID = plngTimeID;
        mdtmCreated = pdtmCreated;
        mdtmDeleted = pdtmDeleted;
        mstrTitle = pstrTitle;
        mstrDescription = pstrDescription;
        mintTaskType = pintTaskType;
        mlngTaskTypeID = plngTaskTypeID;
        mlngOneOff = plngOneOff;

        //Handles updates and initial creates
        mlngTaskDetailID = DatabaseAccess.addRecordToTable("tblTaskDetail",
                new String[] {"fstrTitle", "fstrDescription"},
                new Object[] {mstrTitle, mstrDescription});

        mlngTaskID = DatabaseAccess.addRecordToTable("tblTask",
                new String[] {"flngTaskDetailID", "flngTimeID", "fintTaskType", "flngTaskTypeID", "fdtmCreated","fdtmDeleted","flngOneOff"},
                new Object[] {mlngTaskDetailID,
                        mlngTimeID,
                        mintTaskType,
                        mlngTaskTypeID,
                        mdtmCreated,
                        mdtmDeleted,
                        mlngOneOff},
                "flngTaskID",
                mlngTaskID);
    }

    void deleteTask(){
        try{
            DatabaseAccess.mDatabase.beginTransaction();
            DatabaseAccess.updateRecordFromTable("tblTask",
                    "flngTaskID",
                    mlngTaskID,
                    new String[]{"fdtmDeleted"},
                    new Object[]{Viewer_Tasklist.getCurrentCalendar().getTimeInMillis()});

            finishActiveInstances(3);

            if(mlngTimeID != -1){ //For events and scenarios where there's no time associated to task
                Time tempTime = new Time(mlngTimeID);
                if(tempTime.mblnSession == false){
                    tempTime.deleteTime();
                }
            }

            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        }
        DatabaseAccess.mDatabase.endTransaction();
    }

    TaskInstance generateInstance(long pdtmFrom,
                                  long pdtmTo,
                                  boolean pblnFromTime,
                                  boolean pblnToTime,
                                  boolean pblnToDate,
                                  long plngSessionID){

        return new TaskInstance(-1,
                mlngTaskID,
                mlngTaskDetailID,
                pdtmFrom,
                pdtmTo,
                pblnFromTime,
                pblnToTime,
                pblnToDate,
                mlngOneOff == -1 ? plngSessionID : mlngOneOff); //replace -1 w/ proper session detail id if one off
    }

    void updateTaskDetails(String pstrTitle,
                                  String pstrDescription){
        DatabaseAccess.updateRecordFromTable("tblTaskDetail",
                "flngTaskDetailID",
                mlngTaskDetailID,
                new String[]{"fstrTitle","fstrDescription"},
                new Object[]{pstrTitle,pstrDescription});
    }

    void replaceTimeId(long plngTimeId){
        DatabaseAccess.updateRecordFromTable("tblTask",
                "flngTaskID",
                mlngTaskID,
                new String[]{"flngTimeID"},
                new Object[]{plngTimeId});
    }

    void updateOneOff(long plngOneOff){
        DatabaseAccess.updateRecordFromTable("tblTask",
                "flngTaskID",
                mlngTaskID,
                new String[]{"flngOneOff"},
                new Object[]{plngOneOff});
    }

    void finishActiveInstances(int pintCompleteType){
        try(Cursor curInstances = DatabaseAccess.retrieveActiveTaskInstanceFromTask(mlngTaskID)) {
            while (curInstances.moveToNext()) {
                TaskInstance ti = new TaskInstance(curInstances.getLong(curInstances.getColumnIndex("flngInstanceID")));
                ti.finishInstance(pintCompleteType);
            }
        }
    }
}
