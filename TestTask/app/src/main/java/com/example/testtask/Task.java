package com.example.testtask;

import android.database.Cursor;

public class Task {
    long mlngTaskID;
    long mlngTimeID;
    int mintTaskType;
    long mlngTaskTypeID;
    long mlngTaskDetailID;
    long mlngOneOff;
    //Create task detail object
    String mstrTitle;
    String mstrDescription;

    long mdtmCreated;
    long mdtmDeleted;

    public Task(){
        mlngTaskID = -1;
        mlngTimeID = -1;
        mlngTaskDetailID = -1;
        mdtmCreated = Task_Display.getCurrentCalendar().getTimeInMillis();
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
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblTask", "flngTaskID", plngTaskID);
        if(cursor.moveToFirst()){
            mlngTaskID = cursor.getLong(cursor.getColumnIndex("flngTaskID"));
            mlngTimeID = cursor.getLong(cursor.getColumnIndex("flngTimeID"));
            mlngTaskDetailID = cursor.getLong(cursor.getColumnIndex("flngTaskDetailID"));
            Cursor taskDetail = DatabaseAccess.getRecordsFromTable("tblTaskDetail", "flngTaskDetailID", mlngTaskDetailID);
            taskDetail.moveToFirst();
            mstrTitle = taskDetail.getString(taskDetail.getColumnIndex("fstrTitle"));
            mstrDescription = taskDetail.getString(taskDetail.getColumnIndex("fstrDescription"));
            mdtmCreated = cursor.getLong(cursor.getColumnIndex("fdtmCreated"));
            mdtmDeleted = cursor.getLong(cursor.getColumnIndex("fdtmDeleted"));
            mintTaskType = cursor.getInt(cursor.getColumnIndex("fintTaskType"));
            mlngTaskTypeID = cursor.getLong(cursor.getColumnIndex("flngTaskTypeID"));
            mlngOneOff = cursor.getLong(cursor.getColumnIndex("flngOneOff"));
        } else {
            System.out.println("Unable to find task for id " + plngTaskID);
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

        saveTask();
    }

    public boolean saveTask(){
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
        return true;
    }

    public void deleteTask(){
        DatabaseAccess.updateRecordFromTable("tblTask",
                "flngTaskID",
                mlngTaskID,
                new String[]{"fdtmDeleted"},
                new Object[]{Task_Display.getCurrentCalendar().getTimeInMillis()});

        Cursor curActiveInstances = DatabaseAccess.retrieveActiveTaskInstanceFromTask(mlngTaskID);
        while(curActiveInstances.moveToNext()){
            TaskInstance ti = new TaskInstance(curActiveInstances.getLong(curActiveInstances.getColumnIndex("flngInstanceID")));
            ti.deleteInstance();
        }

        Time tempTime = new Time(mlngTimeID);
        if(!tempTime.isSession()){
            tempTime.completeTime();
        }
    }

//    public void generateInstances(Boolean pblnInitial){
//        Time tempTime = new Time(mlngTimeID);
//        Cursor tblTimeInstance = getValidGenerationPoints();
//
//        while(tblTimeInstance.moveToNext()){
//            if(pblnInitial || //for initial generation, we want the instance to generate for all possible generation points
//                    tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID")) > tempTime.mlngGenerationID){ //after initial, we only want the instance generated when it hasn't already been generated
//                if(mdtmDeleted == -1){ //Add instance for all non deleted tasks
//                    TaskInstance ti = new TaskInstance(mlngTaskID,
//                            mlngTaskDetailID,
//                            tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")),
//                            tempTime.mdtmTo,
//                            tempTime.mblnFromTime,
//                            tempTime.mblnToTime,
//                            tempTime.mblnToDate,
//                            Task_Display.getCurrentCalendar().getTimeInMillis());
//                }
//                if(lngGenID < tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID"))){ //Updates
//                    lngGenID = tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID"));
//                }
//            }
//        }
//
//        if(lngGenID > mlngGenerationID) updateGenerationID(lngGenID);
//    }

    public void updateTaskDetails(String pstrTitle,
                                  String pstrDescription){
        DatabaseAccess.updateRecordFromTable("tblTaskDetail",
                "flngTaskDetailID",
                mlngTaskDetailID,
                new String[]{"fstrTitle","fstrDescription"},
                new Object[]{pstrTitle,pstrDescription});
    }

    public void replaceTimeId(long plngTimeId){
        DatabaseAccess.updateRecordFromTable("tblTask",
                "flngTaskID",
                mlngTaskID,
                new String[]{"flngTimeID"},
                new Object[]{plngTimeId});
    }

    public void updateOneOff(long plngOneOff){
        DatabaseAccess.updateRecordFromTable("tblTask",
                "flngTaskID",
                mlngTaskID,
                new String[]{"flngOneOff"},
                new Object[]{plngOneOff});
    }

    public void clearActiveInstances(){
        Cursor curInstances = DatabaseAccess.retrieveActiveTaskInstanceFromTask(mlngTaskID);
        while(curInstances.moveToNext()){
            TaskInstance ti = new TaskInstance(curInstances.getLong(curInstances.getColumnIndex("flngInstanceID")));
            ti.deleteInstance();
        }
    }
}
