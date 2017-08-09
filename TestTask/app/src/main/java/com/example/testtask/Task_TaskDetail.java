package com.example.testtask;

/**
 * Created by Misterbee180 on 7/8/2017.
 */

public class Task_TaskDetail {
    Integer mId = 0;
    String mDescription = "";
    Integer mSessId; //If populated then it's tied to a session. Otherwise it's just set by time
    //For one off tasks
    Integer mFromHour = 12;
    Integer mFromMinute = 0;
    boolean mFromAm = false;
    Integer mToHour = 12;
    Integer mToMinute = 0;
    boolean mToAm = false;
    //
    boolean mRepeat = false; //Need to figure out how to do this repeat stuff

    public Task_TaskDetail(Integer pId,
                           String pDescription,
                           Integer pSessId,
                           Integer pFromHour,
                           Integer pFromMinute,
                           Boolean pFromAm,
                           Integer pToHour,
                           Integer pToMinute,
                           Boolean pToAm,
                           boolean pRepeat){
        mId = pId;
        mDescription = pDescription;
        mSessId = pSessId;
        mFromHour = pFromHour;
        mFromMinute = pFromMinute;
        mFromAm = pFromAm;
        mToHour  = pToHour;
        mToMinute = pToMinute;
        mToAm = pToAm;
        mRepeat = pRepeat;
    }
}
