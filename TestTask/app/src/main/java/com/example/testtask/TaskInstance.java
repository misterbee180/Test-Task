package com.example.testtask;

import android.database.Cursor;

public class TaskInstance {
    long mlngInstanceID = (long)-1;
    long mlngTaskID;
    long mlngTaskDetailID;
    String mstrTitle;
    String mstrDescription;
    long mdtmFrom;
    long mdtmTo;
    Boolean mblnFromTime;
    Boolean mblnToTime;
    Boolean mblnToDate;
    long mdtmCreated;
    long mdtmCompleted;
    long mdtmSystemCompleted;
    long mdtmDeleted;
    long mdtmEdited;
    long mlngSessionID;

    public TaskInstance(long plngInstanceID){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblTaskInstance", "flngInstanceID", plngInstanceID);
        if(cursor.moveToFirst()){
            mlngInstanceID = cursor.getLong(cursor.getColumnIndex("flngInstanceID"));
            mlngTaskID = cursor.getLong(cursor.getColumnIndex("flngTaskID"));
            mlngTaskDetailID = cursor.getLong(cursor.getColumnIndex("flngTaskDetailID"));
            Cursor taskDetail = DatabaseAccess.getRecordsFromTable("tblTaskDetail", "flngTaskDetailID", mlngTaskDetailID);
            taskDetail.moveToFirst();
            mstrTitle = taskDetail.getString(taskDetail.getColumnIndex("fstrTitle"));
            mstrDescription = taskDetail.getString(taskDetail.getColumnIndex("fstrDescription"));
            mdtmFrom = cursor.getLong(cursor.getColumnIndex("fdtmFrom"));
            mdtmTo = cursor.getLong(cursor.getColumnIndex("fdtmTo"));
            mblnFromTime = cursor.getLong(cursor.getColumnIndex("fblnFromTime")) == 1;
            mblnToTime = cursor.getLong(cursor.getColumnIndex("fblnToTime")) == 1;
            mblnToDate = cursor.getLong(cursor.getColumnIndex("fblnToDate")) == 1;
            mdtmCreated = cursor.getLong(cursor.getColumnIndex("fdtmCreated"));
            mdtmCompleted = cursor.getLong(cursor.getColumnIndex("fdtmCompleted"));
            mdtmSystemCompleted = cursor.getLong(cursor.getColumnIndex("fdtmSystemCompleted"));
            mdtmDeleted = cursor.getLong(cursor.getColumnIndex("fdtmDeleted"));
            mdtmEdited = cursor.getLong(cursor.getColumnIndex("fdtmEdited"));
            mlngSessionID = cursor.getLong(cursor.getColumnIndex("flngSessionID"));
        } else {
            System.out.println("Unable to find instance for id " + plngInstanceID);
        }
    }

    public TaskInstance(long plngTaskID,
                        long plngTaskDetailID,
                        long pdtmFrom,
                        long pdtmTo,
                        Boolean pblnFromTime,
                        Boolean pblnToTime,
                        boolean pblnToDate,
                        long plngSessionID){
        this(-1,
                plngTaskID,
                plngTaskDetailID,
                pdtmFrom,
                pdtmTo,
                pblnFromTime,
                pblnToTime,
                pblnToDate,
                plngSessionID);
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
        mlngInstanceID = plngInstanceID;
        mlngTaskID = plngTaskID;
        mlngTaskDetailID = plngTaskDetailID;
        mdtmFrom = pdtmFrom;
        mblnFromTime = pblnFromTime;
        mdtmTo = pdtmTo;
        mblnToTime = pblnToTime;
        mblnToDate = pblnToDate;
        if(mlngInstanceID == -1){
            mdtmCreated = Task_Display.getCurrentCalendar().getTimeInMillis();
        } else {
            mdtmEdited = Task_Display.getCurrentCalendar().getTimeInMillis();
        }
        mlngSessionID = plngSessionID;

        createTaskInstance();
    }

    public void createTaskInstance(){
        mlngInstanceID = DatabaseAccess.addRecordToTable("tblTaskInstance",
                new String[] {"flngTaskID", "flngTaskDetailID", "fdtmFrom", "fdtmTo", "fblnFromTime", "fblnToTime", "fblnToDate", "fdtmCreated", "fdtmEdited"},
                new Object[] {mlngTaskID, mlngTaskDetailID, mdtmFrom, mdtmTo, mblnFromTime, mblnToTime, mblnToDate, mdtmCreated, mdtmEdited},
                "flngInstanceID",
                mlngInstanceID);
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
                mlngInstanceID,
                strCompleteType,
                new Object[] {Task_Display.getCurrentCalendar().getTimeInMillis()});
    }
}
