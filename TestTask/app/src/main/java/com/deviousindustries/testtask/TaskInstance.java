package com.deviousindustries.testtask;

import android.database.Cursor;

class TaskInstance {
    long mlngInstanceID;
    long mlngTaskID;
    private long mlngTaskDetailID;
    String mstrTitle;
    String mstrDescription;
    long mdtmFrom;
    long mdtmTo;
    Boolean mblnFromTime;
    Boolean mblnToTime;
    Boolean mblnToDate;
    private long mdtmCreated;
    private long mdtmCompleted;
    private long mdtmSystemCompleted;
    private long mdtmDeleted;
    private long mdtmEdited;
    private long mlngSessionDetailID;

    private TaskInstance(){
        mlngInstanceID = -1;
        mlngTaskID = -1;
        mlngTaskDetailID = -1;
        mstrTitle = "";
        mstrDescription = "";
        mdtmFrom = -1;
        mdtmTo = -1;
        mblnFromTime = false;
        mblnToTime = false;
        mblnToDate = false;
        mdtmCreated = -1;
        mdtmCompleted = -1;
        mdtmSystemCompleted = -1;
        mdtmDeleted = -1;
        mdtmEdited = -1;
        mlngSessionDetailID = -1;
    }

    TaskInstance(long plngInstanceID){
        this();
        try(Cursor tblTaskInstance = DatabaseAccess.getRecordsFromTable("tblTaskInstance", "flngInstanceID", plngInstanceID)){
            if(tblTaskInstance.moveToFirst()){
                mlngInstanceID = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("flngInstanceID"));
                mlngTaskID = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("flngTaskID"));
                mlngTaskDetailID = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("flngTaskDetailID"));
                TaskDetail td = new TaskDetail(mlngTaskDetailID);
                mstrTitle = td.mstrTitle;
                mstrDescription = td.mstrDescription;
                mdtmFrom = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmFrom"));
                mdtmTo = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmTo"));
                mblnFromTime = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fblnFromTime")) == 1;
                mblnToTime = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fblnToTime")) == 1;
                mblnToDate = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fblnToDate")) == 1;
                mdtmCreated = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmCreated"));
                mdtmCompleted = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmCompleted"));
                mdtmSystemCompleted = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmSystemCompleted"));
                mdtmDeleted = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmDeleted"));
                mdtmEdited = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("fdtmEdited"));
                mlngSessionDetailID = tblTaskInstance.getLong(tblTaskInstance.getColumnIndex("flngSessionDetailID"));
            }
        }
    }

    TaskInstance(long plngInstanceID,
                 long plngTaskID,
                 long plngTaskDetailID,
                 long pdtmFrom,
                 long pdtmTo,
                 Boolean pblnFromTime,
                 Boolean pblnToTime,
                 boolean pblnToDate,
                 long plngSessionDetailID){
        this();
        mlngInstanceID = plngInstanceID;
        mlngTaskID = plngTaskID;
        mlngTaskDetailID = plngTaskDetailID;
        mdtmFrom = pdtmFrom;
        mblnFromTime = pblnFromTime;
        mdtmTo = pdtmTo;
        mblnToTime = pblnToTime;
        mblnToDate = pblnToDate;
        if(mlngInstanceID == -1){
            mdtmCreated = Viewer_Tasklist.getCurrentCalendar().getTimeInMillis();
        } else {
            mdtmEdited = Viewer_Tasklist.getCurrentCalendar().getTimeInMillis();
        }
        mlngSessionDetailID = plngSessionDetailID;

        createTaskInstance();
    }

    private void createTaskInstance(){
        mlngInstanceID = DatabaseAccess.addRecordToTable("tblTaskInstance",
                new String[] {"flngTaskID", "flngTaskDetailID", "fdtmFrom", "fdtmTo", "fblnFromTime", "fblnToTime", "fblnToDate", "fdtmCreated", "fdtmEdited", "flngSessionDetailID"},
                new Object[] {mlngTaskID, mlngTaskDetailID, mdtmFrom, mdtmTo, mblnFromTime, mblnToTime, mblnToDate, mdtmCreated, mdtmEdited, mlngSessionDetailID},
                "flngInstanceID",
                mlngInstanceID);
    }

    void finishInstance(int pintCompleteType){
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
                mlngInstanceID,
                strCompleteType,
                new Object[] {Viewer_Tasklist.getCurrentCalendar().getTimeInMillis()});
    }
}
