package com.deviousindustries.testtask.classes;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.deviousindustries.testtask.DatabaseAccess;
import static com.deviousindustries.testtask.constants.ConstantsKt.*;


@Entity(tableName = "tblTaskDetail")
public class TaskDetail {
    @PrimaryKey(autoGenerate = true)
    public long flngTaskDetailID;
    @NonNull
    public String fstrTitle;
    @NonNull
    public String fstrDescription;

    public TaskDetail(){
        flngTaskDetailID = NULL_OBJECT;
        fstrTitle = "";
        fstrDescription = "";
    }

    public TaskDetail(long plngTaskDetailId){
        Cursor taskDetail = DatabaseAccess.getRecordsFromTable("tblTaskDetail", "flngTaskDetailID", plngTaskDetailId);
        if(taskDetail.moveToNext()){
            fstrTitle = taskDetail.getString(taskDetail.getColumnIndex("fstrTitle"));
            fstrDescription = taskDetail.getString(taskDetail.getColumnIndex("fstrDescription"));
        }
    }

    public TaskDetail(long plngTaskDetailId,
               String pstrTitle,
               String pstrDescription){
        flngTaskDetailID = plngTaskDetailId;
        fstrTitle = pstrTitle;
        fstrDescription = pstrDescription;

        flngTaskDetailID = DatabaseAccess.addRecordToTable("tblTaskDetail",
                new String[]{"fstrTitle","fstrDescription"},
                new Object[]{fstrTitle, fstrDescription},
                "flngTaskDetailID",
                flngTaskDetailID);
    }
}
