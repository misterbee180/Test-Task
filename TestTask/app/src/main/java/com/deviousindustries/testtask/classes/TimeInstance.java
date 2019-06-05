package com.deviousindustries.testtask.classes;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.deviousindustries.testtask.DatabaseAccess;
import static com.deviousindustries.testtask.constants.ConstantsKt.*;

@Entity(tableName = "tblTimeInstance")
public class TimeInstance {
    @PrimaryKey(autoGenerate = true)
    public long flngGenerationID;
    public long flngTimeID;
    public long fdtmUpcoming;
    public long fdtmPriority;
    public int fintThru;

    public TimeInstance(){
        flngGenerationID = NULL_OBJECT;
        flngTimeID = NULL_OBJECT;
        fdtmUpcoming = NULL_DATE;
        fdtmPriority = NULL_DATE;
        fintThru = 0;
    }

    public TimeInstance(long plngTimeID){
        this();
        flngTimeID = plngTimeID;
    }

    void save(){
        if(fdtmUpcoming != NULL_DATE){
            DatabaseAccess.addRecordToTable("tblTimeInstance",
                    new String[]{"flngTimeID","fdtmUpcoming","fdtmPriority","fintThru"},
                    new Object[]{flngTimeID, fdtmUpcoming, fdtmPriority, fintThru});
        }
    }

    void delete(){
        DatabaseAccess.taskDatabaseDao.deleteTimeInstance(this);
    }
}
