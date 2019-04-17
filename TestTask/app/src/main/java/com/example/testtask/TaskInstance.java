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
                        long pdtmCreated){
        mlngTaskID = plngTaskID;
        mlngTaskDetailID = plngTaskDetailID;
        mdtmFrom = pdtmFrom;
        mblnFromTime = pblnFromTime;
        mdtmTo = pdtmTo;
        mblnToTime = pblnToTime;
        mblnToDate = pblnToDate;
        mdtmCreated = pdtmCreated;

        createTaskInstance();
    }

    public void updateTaskInstance(String pstrTitle,
                        String pstrDescription,
                        long pdtmFrom,
                        long pdtmTo,
                        Boolean pblnFromTime,
                        Boolean pblnToTime,
                        boolean pblnToDate,
                        long pdtmEdited){
        mstrTitle = pstrTitle;
        mstrDescription = pstrDescription;
        mdtmFrom = pdtmFrom;
        mblnFromTime = pblnFromTime;
        mdtmTo = pdtmTo;
        mblnToTime = pblnToTime;
        mblnToDate = pblnToDate;
        mdtmEdited = pdtmEdited;

        DatabaseAccess.addRecordToTable("tblTaskInstance",
                new String[] {"flngTaskID", "flngTaskDetailID", "fdtmFrom", "fdtmTo", "fblnFromTime", "fblnToTime", "fblnToDate", "fdtmEdited"},
                new Object[] {mlngTaskID,
                        DatabaseAccess.addRecordToTable("tblTaskDetail",
                                new String[] {"fstrTitle", "fstrDescription"},
                                new Object[] {mstrTitle, mstrDescription}),
                        mdtmFrom,
                        mdtmTo,
                        mblnFromTime,
                        mblnToTime,
                        mblnToDate,
                        mdtmEdited},
                "flngInstanceID",
                mlngInstanceID);
    }

    public boolean createTaskInstance(){

        long flngInstanceID = DatabaseAccess.addRecordToTable("tblTaskInstance",
                new String[] {"flngTaskID", "flngTaskDetailID", "fdtmFrom", "fdtmTo", "fblnFromTime", "fblnToTime", "fblnToDate", "fdtmCreated"},
                new Object[] {mlngTaskID, mlngTaskDetailID, mdtmFrom, mdtmTo, mblnFromTime, mblnToTime, mblnToDate, mdtmCreated});

        if (flngInstanceID == -1) {
            System.out.println("Unable to create instance id");
            return false;
        }

        return true;
    }

    public void deleteInstance(){
        DatabaseAccess.updateRecordFromTable("tblTaskInstance",
                "flngInstanceID",
                mlngInstanceID,
                new String[] {"fdtmRemoved"},
                new Object[] {Task_Display.getCurrentCalendar()});
    }
}
