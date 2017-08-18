package com.example.testtask;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeKeeper extends ConstraintLayout implements View.OnClickListener {

    static Calendar mFromTime;
    static Calendar mToTime;
    static boolean fromTime = true;
    static TextView sFromTextView;
    static TextView sToTextView;

    CalendarView dayOfMonthCalendar;
    ConstraintLayout dayOfWeekLayout;
    ConstraintLayout dayOfSectionLayout;
    Spinner frequencySpinner;
    Spinner timeRangeSpinner;
    Button buttonFrom;
    Button buttonTo;

    Long mlngTimeID;
    Long mlngWeekID;

    public TimeKeeper(Context context){
        super(context);
    }

    public TimeKeeper(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(context, R.layout.activity_time_keeper, this); //This is super important. Inflate loads the screen from XML.
        //Set member access
        mFromTime = Calendar.getInstance();
        mToTime = Calendar.getInstance();
        sFromTextView = (TextView) findViewById(R.id.textSessFrom);
        sToTextView = (TextView) findViewById(R.id.textSessTo);
        dayOfMonthCalendar = (CalendarView) findViewById(R.id.calMonthlyDays);
        dayOfWeekLayout = (ConstraintLayout) findViewById(R.id.CLayWeeklyDays);
        dayOfSectionLayout = (ConstraintLayout) findViewById(R.id.CLayDayOfGroup);
        frequencySpinner = (Spinner) findViewById(R.id.spnFrequency);
        timeRangeSpinner = (Spinner) findViewById(R.id.spnTimeRange);
        buttonFrom = (Button) findViewById(R.id.btnSessFrom);
        buttonTo = (Button) findViewById(R.id.btnSessTo);

        //Set Listeners
        buttonFrom.setOnClickListener(this);
        buttonTo.setOnClickListener(this);

        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                String selection = (String)parent.getItemAtPosition(position);
                evaluateTimeView(selection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){
            }
        });

        resetTimeKeeper(); //This may not be necessary as right now the id's and such are not static, however they might become necessary in the future...
    }

    public TimeKeeper(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    //If you want to pass in a pre-defined function you can either do this or implement the onCallListener and do it the way you're doing w/o this.
    /*View.OnClickListener clickListener = new OnClickListener() {
        public void onClick(View v) {
            // do something here
        }
    };*/

    @Override
    public void onClick(View view) {
        //Set which clock we're updating
        if (view.getId() == R.id.btnSessFrom) {
            fromTime = true;
        } else {
            fromTime = false;
        }

        AppCompatActivity context = (AppCompatActivity)getContext();
        DialogFragment newFragment = new TimeKeeper.TimePickerFragment();
        newFragment.show(context.getSupportFragmentManager(), "timePicker");
    }

    public void resetTimeKeeper(){
        mlngTimeID = (long)-1;
        mlngWeekID = (long)-1;

        //Set visibility
        dayOfSectionLayout.setVisibility(View.GONE);
        frequencySpinner.setVisibility(View.GONE);
    }

    public void evaluateTimeView(String pstrSelection) {
        if (pstrSelection.equals("Weekly")){
            dayOfSectionLayout.setVisibility(View.VISIBLE);
            dayOfMonthCalendar.setVisibility(View.GONE);
            dayOfWeekLayout.setVisibility(View.VISIBLE);
            frequencySpinner.setVisibility(View.VISIBLE);
        } else if (pstrSelection.equals("Monthly")){
            dayOfSectionLayout.setVisibility(View.VISIBLE);
            dayOfMonthCalendar.setVisibility(View.VISIBLE);
            dayOfWeekLayout.setVisibility(View.GONE);
            frequencySpinner.setVisibility(View.VISIBLE);
        } else {
            dayOfSectionLayout.setVisibility(View.GONE);
            frequencySpinner.setVisibility(View.GONE);
        }
    }

    public Long getTimeID(){
        return mlngTimeID;
    }

    public Boolean blnTimeDetailsExist(){
        if (getFromTime() != null || getToTime() != null || getTimeRange() != ""){
          return true;
        }
        return false;
    }

    public Long getFromTime() {
        if (mFromTime != null) {return mFromTime.getTimeInMillis();}
        return null;
    }

    public void setFromTime(Long pFromMili){
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        mFromTime.setTimeInMillis(pFromMili);
        sFromTextView.setText("From: " + dateFormat.format(mFromTime.getTime()));
    }

    public Long getToTime() {
        if (mToTime != null) {return mToTime.getTimeInMillis();}
        return null;
    }

    public void setToTime(Long pToMili){

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        mToTime.setTimeInMillis(pToMili);
        sToTextView.setText("To: " + dateFormat.format(mToTime.getTime()));
    }

    public Long getDayOfWeek(String pstrDow){
        CheckBox chbDow = null;
        switch (pstrDow){
            case "Monday":
                chbDow = (CheckBox) findViewById(R.id.chkMonday);
                break;
            case "Tuesday":
                chbDow = (CheckBox) findViewById(R.id.chkTuesday);
                break;
            case "Wednesday":
                chbDow = (CheckBox) findViewById(R.id.chkWednesday);
                break;
            case "Thursday":
                chbDow = (CheckBox) findViewById(R.id.chkThursday);
                break;
            case "Friday":
                chbDow = (CheckBox) findViewById(R.id.chkFriday);
                break;
            case "Saturday":
                chbDow = (CheckBox) findViewById(R.id.chkSaturday);
                break;
            case "Sunday":
                chbDow = (CheckBox) findViewById(R.id.chkSunday);
                break;
        }
        return chbDow.isChecked() ? (long)1:(long)0;
    }

    public void setDayOfWeek(String pstrDow, Long pblnOn){
        CheckBox chbDow = null;
        switch (pstrDow){
            case "Monday":
                chbDow = (CheckBox) findViewById(R.id.chkMonday);
                break;
            case "Tuesday":
                chbDow = (CheckBox) findViewById(R.id.chkTuesday);
                break;
            case "Wednesday":
                chbDow = (CheckBox) findViewById(R.id.chkWednesday);
                break;
            case "Thursday":
                chbDow = (CheckBox) findViewById(R.id.chkThursday);
                break;
            case "Friday":
                chbDow = (CheckBox) findViewById(R.id.chkFriday);
                break;
            case "Saturday":
                chbDow = (CheckBox) findViewById(R.id.chkSaturday);
                break;
            case "Sunday":
                chbDow = (CheckBox) findViewById(R.id.chkSunday);
                break;
        }
        chbDow.setChecked(pblnOn == 1 ? true: false);
    }

    public String getTimeRange(){
        switch (timeRangeSpinner.getSelectedItem().toString()){
            case "Weekly":
                return "W";
            case "Monthly":
                return "M";
            default:
                return "";
        }
    }

    public void setTimeRange(String pstrTimeRange) {
        switch (pstrTimeRange) {
            case "W": timeRangeSpinner.setSelection(1);
                evaluateTimeView("Weekly");
                break;
            default: evaluateTimeView("");
                break;
        }
    }

    public void loadTimeDetails(Long plngTimeID) {
        mlngTimeID = plngTimeID;
        Cursor cursor;
        String rawGetSessions = "SELECT * \n" +
                "FROM tblTime tm \n" +
                "WHERE tm.flngID = ?";
        String[] parameters = {Long.toString(mlngTimeID)};
        cursor = DatabaseAccess.mDatabase.rawQuery(rawGetSessions,parameters);

        while(cursor.moveToNext()){
            //Set to
            //Set from
            setFromTime(cursor.getLong(cursor.getColumnIndex("fdtmFrom")));
            setToTime(cursor.getLong(cursor.getColumnIndex("fdtmTo")));
            //Set repeat
            //Set days
            mlngWeekID = cursor.getLong(cursor.getColumnIndex("flngWeekID"));
            if (mlngWeekID != -1){
                setTimeRange("W");
                Cursor cursor2;
                String rawGetWeek = "SELECT * \n" +
                        "FROM tblWeek w \n" +
                        "WHERE w.flngID = ?";
                String[] parameters2 = {Long.toString(mlngWeekID)};
                cursor2 = DatabaseAccess.mDatabase.rawQuery(rawGetWeek,parameters2);

                while(cursor2.moveToNext()){
                    setDayOfWeek("Monday",cursor2.getLong(cursor2.getColumnIndex("fblnMonday")));
                    setDayOfWeek("Tuesday",cursor2.getLong(cursor2.getColumnIndex("fblnTuesday")));
                    setDayOfWeek("Wednesday",cursor2.getLong(cursor2.getColumnIndex("fblnWednesday")));
                    setDayOfWeek("Thursday",cursor2.getLong(cursor2.getColumnIndex("fblnThursday")));
                    setDayOfWeek("Friday",cursor2.getLong(cursor2.getColumnIndex("fblnFriday")));
                    setDayOfWeek("Saturday",cursor2.getLong(cursor2.getColumnIndex("fblnSaturday")));
                    setDayOfWeek("Sunday",cursor2.getLong(cursor2.getColumnIndex("fblnSunday")));
                }
            }
        }
    }

    public void updateTimeRecord() {
        String rawUpdateTimeRecord = "UPDATE tblTime \n" +
                "SET fdtmFrom = " + getFromTime() + ", \n" +
                "fdtmTo = "  + getToTime() + " \n" +
                "WHERE flngID = " + Long.toString(mlngTimeID);
        Cursor c = DatabaseAccess.mDatabase.rawQuery(rawUpdateTimeRecord,null);
        c.moveToFirst();
        c.close();
        //todo: check for a change of timerange and update. Right now we only have week so it's okay.
        if (mlngWeekID != -1){
            updateWeekRecord();
        }
    }

    public void updateWeekRecord() {
        String rawUpdateWeekRecord = "UPDATE tblWeek \n" +
                "SET fblnMonday = " + getDayOfWeek("Monday") + ", \n" +
                "fblnTuesday = " + getDayOfWeek("Tuesday") + ", \n" +
                "fblnWednesday = " + getDayOfWeek("Wednesday") + ", \n" +
                "fblnThursday = " + getDayOfWeek("Thursday") + ", \n" +
                "fblnFriday = " + getDayOfWeek("Friday") + ", \n" +
                "fblnSaturday = " + getDayOfWeek("Saturday") + ", \n" +
                "fblnSunday = " + getDayOfWeek("Sunday") + " \n" +
                "WHERE flngID = " + Long.toString(mlngWeekID);
        Cursor c = DatabaseAccess.mDatabase.rawQuery(rawUpdateWeekRecord,null);
        c.moveToFirst();
        c.close();
    }

    public void createTime(){
        Long lngWeekKey = (long)-1;
        if (getTimeRange() != "") {
            ContentValues weekValues = new ContentValues();
            weekValues.put("fblnMonday",getDayOfWeek("Monday"));
            weekValues.put("fblnTuesday",getDayOfWeek("Tuesday"));
            weekValues.put("fblnWednesday",getDayOfWeek("Wednesday"));
            weekValues.put("fblnThursday",getDayOfWeek("Thursday"));
            weekValues.put("fblnFriday",getDayOfWeek("Friday"));
            weekValues.put("fblnSaturday",getDayOfWeek("Saturday"));
            weekValues.put("fblnSunday",getDayOfWeek("Sunday"));
            lngWeekKey = DatabaseAccess.mDatabase.insertOrThrow("tblWeek",null,weekValues);
        }
        ContentValues timeValues = new ContentValues();
        timeValues.put("fdtmFrom",getFromTime());
        timeValues.put("fdtmTo",getToTime());
        timeValues.put("flngWeekID",lngWeekKey);
        timeValues.put("fdtmEvaluated",Calendar.getInstance().getTimeInMillis());
        mlngTimeID = DatabaseAccess.mDatabase.insertOrThrow("tblTime",null,timeValues);
    }

    public void populateTimeFromSession(Long plngSessionID){
        Cursor cursor = DatabaseAccess.getRecordFromTable("tblSession", plngSessionID);
        cursor.moveToFirst();
        cursor = DatabaseAccess.getRecordFromTable("tblTime", cursor.getLong(cursor.getColumnIndex("flngTimeID")));
        cursor.moveToFirst();
        setFromTime(cursor.getLong(cursor.getColumnIndex("fdtmFrom")));
        setToTime(cursor.getLong(cursor.getColumnIndex("fdtmTo")));
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            int hour = mFromTime.get(Calendar.HOUR_OF_DAY);
            int minute = mFromTime.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
            if (fromTime) {
                mFromTime = Calendar.getInstance();
                mFromTime .set(Calendar.HOUR_OF_DAY,hourOfDay);
                mFromTime .set(Calendar.MINUTE,minute);
                sFromTextView.setText("From: " + dateFormat.format(mFromTime.getTime()));
            } else {
                mToTime = Calendar.getInstance();
                mToTime .set(Calendar.HOUR_OF_DAY,hourOfDay);
                mToTime .set(Calendar.MINUTE,minute);
                sToTextView.setText("From: " + dateFormat.format(mToTime.getTime()));

            }
        }
    }
}
