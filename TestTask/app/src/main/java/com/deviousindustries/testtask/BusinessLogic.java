package com.deviousindustries.testtask;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class BusinessLogic {

    Context mContext;
    SharedPreferences mPrefs;

    public BusinessLogic(Context context){
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void generateTaskInstances() {
        DatabaseAccess.setContext(mContext);
        DatabaseAccess.mDatabase.beginTransaction();
        try{
            try(Cursor tblTime = DatabaseAccess.getRecordsFromTable("tblTime","fblnComplete = 0", null)){
                while (tblTime.moveToNext()) {
                    Time tempTime = new Time(tblTime.getLong(tblTime.getColumnIndex("flngTimeID")));
                    tempTime.buildTimeInstances(); //build generation points
                }

                try(Cursor tblTimeInstance = getValidGenerationPoints()){
                    while (tblTimeInstance.moveToNext()) {
                        Time tempTime = new Time(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngTimeID")));
                        long tiGenerationID = tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID"));
                        if (tiGenerationID > tempTime.mlngGenerationID) {
                            Calendar tempTo = getCalendar(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")));
                            if (tempTime.mblnThru) {
                                tempTo.add(Calendar.DAY_OF_YEAR, tblTimeInstance.getInt(tblTimeInstance.getColumnIndex("fintThru")));
                            }
                            tempTime.generateInstance(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")),
                                    tempTo.getTimeInMillis()); //Add any new instances that need adding
                            tempTime.updateGenerationID(tiGenerationID);
                        }
                    }
                }
            }
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseAccess.mDatabase.endTransaction();
    }

    public Cursor getValidGenerationPoints(){

        //NOTE: I was forced to "inline" all of the arguments because when doing match in android queries sometimes bugs are produced.
        String strSelection = "fdtmUpcoming <= " + getEndCurrentDay().getTimeInMillis();
        strSelection += " and fdtmPriority + 86400000 * fintThru >= " + getBeginningCurentDay().getTimeInMillis();

        return DatabaseAccess.mDatabase.query("tblTimeInstance",
                null,
                strSelection,
                null,
                null,
                null,
                null);
    }

    public Calendar getBeginningCurentDay(){
        Calendar temp = getCurrentCalendar();
        temp.set(Calendar.HOUR_OF_DAY,0);
        temp.set(Calendar.MINUTE,0);
        temp.set(Calendar.SECOND,0);
        temp.set(Calendar.MILLISECOND,0);

        return temp;
    }

    public Calendar getEndCurrentDay(){
        Calendar temp = getCurrentCalendar();
        temp.set(Calendar.HOUR_OF_DAY,23);
        temp.set(Calendar.MINUTE,59);
        temp.set(Calendar.SECOND,59);
        temp.set(Calendar.MILLISECOND,999);

        return temp;
    }

    public Calendar getCalendar(long plngMiliDate){
        Calendar tempCal = getCurrentCalendar();
        tempCal.setTimeInMillis(plngMiliDate);
        return tempCal;
    }

    public Calendar getCalendar(long plngMiliDate,
                                       boolean pblnBeginning,
                                       boolean pblnEnd){
        Calendar tempCal = getCurrentCalendar();
        tempCal.setTimeInMillis(plngMiliDate);
        if(pblnBeginning){
            tempCal.set(Calendar.HOUR_OF_DAY,0);
            tempCal.set(Calendar.MINUTE,0);
            tempCal.set(Calendar.SECOND,0);
            tempCal.set(Calendar.MILLISECOND,0);
        }
        if(pblnEnd){
            tempCal.set(Calendar.HOUR_OF_DAY,23);
            tempCal.set(Calendar.MINUTE,59);
            tempCal.set(Calendar.SECOND,59);
            tempCal.set(Calendar.MILLISECOND,999);
        }
        return tempCal;
    }

    public Calendar getCurrentCalendar(){
        Calendar currentCalendar = Calendar.getInstance();
        if (mPrefs.getBoolean("enable_debug", false)) {
            //calNow.set(mPrefs.getString())
            String strDatePref = mPrefs.getString("DatePref", "");
            String strTimePref = mPrefs.getString("TimePref", "");
            if (!strDatePref.equals("")) {
                String[] datePieces = strDatePref.split("-");
                currentCalendar.set(Calendar.YEAR, Integer.parseInt(datePieces[0]));
                currentCalendar.set(Calendar.MONTH, Integer.parseInt(datePieces[1]) - 1);
                currentCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(datePieces[2]));

                if (mPrefs.getBoolean("enable_time", false) && !strTimePref.equals("")) {
                    String[] timePieces = strTimePref.split(":");
                    currentCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timePieces[0]));
                    currentCalendar.set(Calendar.MINUTE, Integer.parseInt(timePieces[1]));
                } else {
                    currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    currentCalendar.set(Calendar.MINUTE, 0);
                }
            }
            currentCalendar.set(Calendar.SECOND,0);
            currentCalendar.set(Calendar.MILLISECOND,0);
        }
        return currentCalendar;
    }
}
