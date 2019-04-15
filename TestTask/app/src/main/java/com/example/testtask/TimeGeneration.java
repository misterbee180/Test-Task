package com.example.testtask;

public class TimeGeneration{
    long mlngGenerationID;
    long mlngTimeID;
    long mdtmUpcoming;
    long mdtmPriority;

    public TimeGeneration(long plngTimeID){
        mlngGenerationID = -1;
        mlngTimeID = plngTimeID;
        mdtmUpcoming = -1;
        mdtmPriority = -1;
    }

    public void save(){
        if(mdtmUpcoming != -1){
            mlngGenerationID = DatabaseAccess.addRecordToTable("tblTimeGeneration",
                    new String[]{"flngTimeID","fdtmUpcoming","fdtmPriority"},
                    new Object[]{mlngTimeID, mdtmUpcoming, mdtmPriority});
        }
    }
}
