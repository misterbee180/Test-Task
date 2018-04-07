package com.example.testtask;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.app.AlertDialog.Builder;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeKeeper extends ConstraintLayout implements View.OnClickListener {

    static Calendar mFromTime;
    static Calendar mToTime;
    static Calendar sToDate;
    static Calendar sFromDate;
    static int sSetIndicator = -1;
    static int[] arrSpecificDays;
    static int intArrayCounter;
    static ArrayListContainer repititionSpinner;

    static ConstraintLayout cLayMonthly;
    static ConstraintLayout cLayWeekly;
    static ConstraintLayout cLayNoRep;

    static Spinner timeRangeSpinner;
    //static Spinner repititionSpinner;
    static Button btnFromDate;
    static Button btnToDate;
    static Button btnFromTime;
    static Button btnToTime;
    static EditText txtMonthlyDays;


    Long mlngTimeID;
    Long mlngDayID;
    Long mlngWeekID;
    Long mlngMonthID;
    Long mlngYearID;
    String mstrOrigFrequency;

    public TimeKeeper(Context context){
        super(context);
    }

    public TimeKeeper(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(context, R.layout.activity_time_keeper, this); //This is super important. Inflate loads the screen from XML.
        //Set member access
        arrSpecificDays = new int[31];
        intArrayCounter = 0;
        mstrOrigFrequency = "";
        repititionSpinner = new ArrayListContainer();

        //Target View Groups
        cLayNoRep = (ConstraintLayout) findViewById(R.id.CLayNoFrequency);
        cLayWeekly = (ConstraintLayout) findViewById(R.id.CLayWeekly);
        cLayMonthly = (ConstraintLayout) findViewById(R.id.CLayMonthly);

        //Target Generic Fields
        timeRangeSpinner = (Spinner) findViewById(R.id.spnTimeRange);
        repititionSpinner.LinkArrayToSpinner((Spinner) findViewById(R.id.spnRepitition), getContext());
        repititionSpinner.mSpinner.setOnItemSelectedListener(selectedListener);
        btnFromTime = (Button) findViewById(R.id.Timekeeper_BtnFromTime);
        btnToTime = (Button) findViewById(R.id.Timekeeper_BtnToTime);

        //Target No Frequency and Year Fields
        btnFromDate = (Button) findViewById(R.id.Timekeeper_NoFreq_BtnFromDate);
        btnToDate = (Button) findViewById(R.id.Timekeeper_NoFreq_BtnToDate);

        //Target Month Fields
        txtMonthlyDays = (EditText) findViewById(R.id.TimeKeeper_Monthly_Txt_Display);

        //Set Listeners
        btnFromTime.setOnClickListener(this);
        btnToTime.setOnClickListener(this);
        btnFromDate.setOnClickListener(this);
        btnToDate.setOnClickListener(this);
        timeRangeSpinner.setOnItemSelectedListener(selectedListener);
        findViewById(R.id.TimeKeeper_Monthly_Gen).setOnClickListener(this);
        findViewById(R.id.TimeKeeper_Monthly_Spec).setOnClickListener(this);
        findViewById(R.id.TimeKeeper_Monthly_Btn_Add).setOnClickListener(this);

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

    Spinner.OnItemSelectedListener selectedListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selection = (String)parent.getItemAtPosition(position);
            evaluateTimeView(selection);
        }

        public void onNothingSelected(AdapterView parent){

        }
    };

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.Timekeeper_BtnFromTime:
                sSetIndicator = 1;
                instantiateTimeFragment();
                break;
            case R.id.Timekeeper_BtnToTime:
                sSetIndicator = 2;
                instantiateTimeFragment();
                break;
            case R.id.Timekeeper_NoFreq_BtnFromDate:
                sSetIndicator = 3;
                instantiateDateFragment();
                break;
            case R.id.Timekeeper_NoFreq_BtnToDate:
                sSetIndicator = 4;
                instantiateDateFragment();
                break;
            case R.id.TimeKeeper_Monthly_Gen:
                findViewById(R.id.TimeKeeper_Monthly_First).setVisibility(VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_Middle).setVisibility(VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_Last).setVisibility(VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_AfterWkn).setVisibility(VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_Btn_Add).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Txt_Display).setVisibility(GONE);
                break;
            case R.id.TimeKeeper_Monthly_Spec:
                findViewById(R.id.TimeKeeper_Monthly_First).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Middle).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Last).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_AfterWkn).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Btn_Add).setVisibility(VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_Txt_Display).setVisibility(VISIBLE);
                break;
            case R.id.TimeKeeper_Monthly_Btn_Add:
                AppCompatActivity context = (AppCompatActivity)getContext();
                DialogFragment newFragment = new TimeKeeper.NumberPickerDialog();
                newFragment.show(context.getSupportFragmentManager(), "numberPicker");
                break;
        }
    }

    public void loadTimeDetails(Long plngTimeID,
                                Boolean pblnSession) {
        mlngTimeID = plngTimeID;
        Cursor cursor;
        String rawGetSessions = "SELECT * \n" +
                "FROM tblTime tm \n" +
                "WHERE tm.flngTimeID = ?";
        String[] parameters = {Long.toString(mlngTimeID)};
        cursor = DatabaseAccess.mDatabase.rawQuery(rawGetSessions,parameters);

        while(cursor.moveToNext()){
            //Set to & from
            setFromTime(cursor.getLong(cursor.getColumnIndex("fdtmFrom")));
            setToTime(cursor.getLong(cursor.getColumnIndex("fdtmTo")));
            setsFromDate(cursor.getLong(cursor.getColumnIndex("fdtmFromDate")));
            setsToDate(cursor.getLong(cursor.getColumnIndex("fdtmToDate")));

            //Set repeat
            if (cursor .getInt(cursor.getColumnIndex("flngRepetition")) != -1){
                repititionSpinner.setIDSpinner(cursor.getLong(cursor.getColumnIndex("flngRepetition")));
            }

            //Set days
            mlngDayID = cursor .getLong(cursor.getColumnIndex("flngDayID"));
            mlngWeekID = cursor.getLong(cursor.getColumnIndex("flngWeekID"));
            mlngMonthID = cursor.getLong(cursor.getColumnIndex("flngMonthID"));
            mlngYearID = cursor.getLong(cursor.getColumnIndex("flngYearID"));
            if(mlngDayID != -1){
                mstrOrigFrequency = "D";
            } else if (mlngWeekID != -1){
                mstrOrigFrequency = "W";
            } else if (mlngMonthID != -1){
                mstrOrigFrequency = "M";
            } else if (mlngYearID != -1){
                mstrOrigFrequency = "Y";
            }

            if (mlngDayID != -1){
                setTimeRange("D");
            } else if (mlngWeekID != -1){
                setTimeRange("W");
                Cursor cursor2 = DatabaseAccess.getRecordsFromTable("tblWeek", "flngWeekID", mlngWeekID);

                while(cursor2.moveToNext()){
                    setDayOfWeek("Monday",cursor2.getLong(cursor2.getColumnIndex("fblnMonday")));
                    setDayOfWeek("Tuesday",cursor2.getLong(cursor2.getColumnIndex("fblnTuesday")));
                    setDayOfWeek("Wednesday",cursor2.getLong(cursor2.getColumnIndex("fblnWednesday")));
                    setDayOfWeek("Thursday",cursor2.getLong(cursor2.getColumnIndex("fblnThursday")));
                    setDayOfWeek("Friday",cursor2.getLong(cursor2.getColumnIndex("fblnFriday")));
                    setDayOfWeek("Saturday",cursor2.getLong(cursor2.getColumnIndex("fblnSaturday")));
                    setDayOfWeek("Sunday",cursor2.getLong(cursor2.getColumnIndex("fblnSunday")));
                }
            } else if (mlngMonthID != -1){
                setTimeRange("M");
                Cursor cursor2 = DatabaseAccess.getRecordsFromTable("tblMonth", "flngMonthID", mlngWeekID);

                while(cursor2.moveToNext()){
                    setMonthDetails((cursor2.getInt(cursor2.getColumnIndex("fblnFirst")) == 1)? true : false,
                            (cursor2.getInt(cursor2.getColumnIndex("fblnMiddle")) == 1)? true : false,
                            (cursor2.getInt(cursor2.getColumnIndex("fblnLast")) == 1)? true : false,
                            (cursor2.getInt(cursor2.getColumnIndex("fblnAfter")) == 1)? true : false,
                            cursor2.getString(cursor2.getColumnIndex("fstrSpecific")));
                }
            } else if (mlngYearID != -1){
                setTimeRange("Y");
            }
        }
    }

    public void setUpForSession(){
        repititionSpinner.Clear();
        repititionSpinner.Add("Repeat Every", (long)1);
        repititionSpinner.Add("Repeat Every Other", (long)2);
        repititionSpinner.Add("Repeat Every Thrid", (long)3);
        repititionSpinner.Add("Repeat Every Forth", (long)4);
        repititionSpinner.mAdapter.notifyDataSetChanged();
        repititionSpinner.setIDSpinner((long)1);

        evaluateTimeView(repititionSpinner.mSpinner.getSelectedItem().toString());
    }

    public void setUpForRegular(){
        repititionSpinner.Clear();
        repititionSpinner.Add("No Repetition", (long)0);
        repititionSpinner.Add("Repeat Every", (long)1);
        repititionSpinner.Add("Repeat Every Other", (long)2);
        repititionSpinner.Add("Repeat Every Thrid", (long)3);
        repititionSpinner.Add("Repeat Every Forth", (long)4);
        repititionSpinner.mAdapter.notifyDataSetChanged();
        repititionSpinner.setIDSpinner((long)0);

        evaluateTimeView(repititionSpinner.mSpinner.getSelectedItem().toString());
    }

    public void populateTimeFromSession(Long plngSessionID){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblSession", "flngSessionID", plngSessionID);
        cursor.moveToFirst();
        loadTimeDetails(cursor.getLong(cursor.getColumnIndex("flngTimeID")), true);
    }

    public void validateTimeDetails(){
        //if session, do not allow no timeframe

        //if no timeframe, only allow from date -- This can be handled by visibility rules

        //Do not allow to date without from date
    }

    public void createTimeDetails(){
        Long lngDayKey = (long)-1;
        Long lngWeekKey = (long)-1;
        Long lngMonthKey = (long)-1;
        Long lngYearKey = (long)-1;
        String[] arrColumns;
        Object[] arrValues;

        //Determine and create appropriate data element for repetition type
        switch (getTimeRange()){
            case "D":
                arrColumns = new String[]{};
                arrValues = new Object[]{};
                lngDayKey = DatabaseAccess.addRecordToTable("tblDay",
                        arrColumns,
                        arrValues,
                        "flngDayID",
                        (long)-1);
                break;
            case "W":
                arrColumns = new String[]{"fblnMonday","fblnTuesday","fblnWednesday","fblnThursday","fblnFriday","fblnSaturday","fblnSunday"};
                arrValues = new Object[]{getDayOfWeek("Monday"),
                        getDayOfWeek("Tuesday"),
                        getDayOfWeek("Wednesday"),
                        getDayOfWeek("Thursday"),
                        getDayOfWeek("Friday"),
                        getDayOfWeek("Saturday"),
                        getDayOfWeek("Sunday")};
                lngWeekKey = DatabaseAccess.addRecordToTable("tblWeek",
                        arrColumns,
                        arrValues);
                break;
            case "M":
                arrColumns = new String[]{"fblnFirst","fblnMiddle","fblnLast","fblnAfterWkn","fstrSpecific"};
                arrValues = new Object[]{((CheckBox)findViewById(R.id.TimeKeeper_Monthly_First)).isChecked(),
                        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_Middle)).isChecked(),
                        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_Last)).isChecked(),
                        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_AfterWkn)).isChecked(),
                        ((EditText)findViewById(R.id.TimeKeeper_Monthly_Txt_Display)).getText().toString()};
                lngMonthKey = DatabaseAccess.addRecordToTable("tblMonth",
                        arrColumns,
                        arrValues);
                break;
            case "Y":
                arrColumns = new String[]{};
                arrValues = new Object[]{};
                lngYearKey = DatabaseAccess.addRecordToTable("tblYear",
                        arrColumns,
                        arrValues,
                        "flngYearID",
                        (long)-1);
                break;
        }

        arrColumns = new String[]{"fdtmFrom","fdtmTo","fdtmFromDate","fdtmToDate","flngDayID","flngWeekID","flngMonthID","flngYearID","flngRepetition","fdtmEvaluated","fdtmCreated"};
        arrValues = new Object[]{getFromTime(),
                getToTime(),
                getFromDate(),
                getToDate(),
                lngDayKey,
                lngWeekKey,
                lngMonthKey,
                lngYearKey,
                repititionSpinner.GetID(repititionSpinner.mSpinner.getSelectedItemPosition()),
                Task_Display.getCurrentCalendar(getContext()).getTimeInMillis(),
                Task_Display.getCurrentCalendar( getContext()).getTimeInMillis()};
        mlngTimeID = DatabaseAccess.addRecordToTable("tblTime",
                arrColumns,
                arrValues,
                "flngTimeID",
                mlngTimeID);
    }

    public void updateTimeDetails(){
        //Look to see if the frequency changed and if it has, delete the original record
        if (mstrOrigFrequency != getTimeRange() && mstrOrigFrequency != ""){
            //Delete original frequency entry
            String strTable = "";
            String strColumn = "";
            Long lngID = (long)-1;
            switch(mstrOrigFrequency){
                case "D":
                    strTable = "tblDay";
                    strColumn = "flngDayID";
                    lngID = mlngDayID;
                    break;
                case "M":
                    strTable = "tblMonth";
                    strColumn = "flngMonthID";
                    lngID = mlngMonthID;
                    break;
                case "W":
                    strTable = "tblWeek";
                    strColumn = "flngWeekID";
                    lngID = mlngWeekID;
                    break;
                case "Y":
                    strTable = "tblYear";
                    strColumn = "flngYearID";
                    lngID = mlngYearID;
                    break;
            }
        }
        createTimeDetails();
    }

    public void resetTimeKeeper(){
        mlngTimeID = (long)-1;
        mlngDayID = (long) -1;
        mlngWeekID = (long)-1;
        mlngMonthID = (long)-1;
        mlngYearID = (long)-1;

        mFromTime = null;
        mToTime = null;
        sFromDate = null;
        sToDate = null;

        //Set visibility
    }

    public void evaluateTimeView(String pstrSelection) {
        switch (pstrSelection){
            case "No Repetition":
                cLayNoRep.setVisibility(View.VISIBLE);
                cLayWeekly.setVisibility(View.GONE);
                cLayMonthly.setVisibility(View.GONE);
                timeRangeSpinner.setVisibility(View.GONE);
                findViewById(R.id.Timekeeper_NoFreq_BtnToDate).setVisibility(View.GONE);
                break;
            case "Day":
                cLayNoRep.setVisibility(View.GONE);
                cLayWeekly.setVisibility(View.GONE);
                cLayMonthly.setVisibility(View.GONE);
                break;
            case "Week":
                cLayNoRep.setVisibility(View.GONE);
                cLayWeekly.setVisibility(View.VISIBLE);
                cLayMonthly.setVisibility(View.GONE);
                break;
            case "Month":
                cLayNoRep.setVisibility(View.GONE);
                cLayWeekly.setVisibility(View.GONE);
                cLayMonthly.setVisibility(View.VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_First).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Middle).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Last).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_AfterWkn).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Btn_Add).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Txt_Display).setVisibility(GONE);
                break;
            case "Year":
                //Using No Rep View for now until Year View becomes important
                cLayNoRep.setVisibility(View.VISIBLE);
                cLayWeekly.setVisibility(View.GONE);
                cLayMonthly.setVisibility(View.GONE);
                findViewById(R.id.Timekeeper_NoFreq_BtnToDate).setVisibility(View.VISIBLE);
                break;
            default:
                timeRangeSpinner.setVisibility(View.VISIBLE);
                break;
        }
    }

    //region Getters And Setters
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
        mFromTime = Calendar.getInstance();
        mFromTime.setTimeInMillis(pFromMili);
        btnFromTime.setText("From: " + dateFormat.format(mFromTime.getTime()));
    }

    public Long getToTime() {
        if (mToTime != null) {return mToTime.getTimeInMillis();}
        return null;
    }

    public void setToTime(Long pToMili){

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        mToTime = Calendar.getInstance();
        mToTime.setTimeInMillis(pToMili);
        btnToTime.setText("To: " + dateFormat.format(mToTime.getTime()));
    }

    public Long getFromDate() {
        if (sFromDate != null) {return sFromDate.getTimeInMillis();}
        return null;
    }

    public void setsFromDate(Long pToMili){

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
        sFromDate = Calendar.getInstance();
        sFromDate.setTimeInMillis(pToMili);
        btnFromDate.setText("From: " + dateFormat.format(sFromDate.getTime()));
    }

    public Long getToDate() {
        if (sToDate != null) {return sToDate.getTimeInMillis();}
        return null;
    }

    public void setsToDate(Long pToMili){

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
        sToDate = Calendar.getInstance();
        sToDate.setTimeInMillis(pToMili);
        btnToDate.setText("To: " + dateFormat.format(sFromDate.getTime()));
    }

    public Boolean getDayOfWeek(String pstrDow){
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
        return chbDow.isChecked();
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
        String result = "";
        if (!repititionSpinner.mSpinner.getSelectedItem().equals("No Repetition")){
            switch (timeRangeSpinner.getSelectedItem().toString()){
                case "Day":
                    result = "D";
                    break;
                case "Week":
                    result = "W";
                    break;
                case "Month":
                    result = "M";
                    break;
                case "Year":
                    result = "Y";
                    break;
            }
        } else {
            result = "";
        }
        return result;
    }

    public void setTimeRange(String pstrTimeRange) {
        switch (pstrTimeRange) {
            case "D": timeRangeSpinner.setSelection(0);
                evaluateTimeView("Day");
                break;
            case "W": timeRangeSpinner.setSelection(1);
                evaluateTimeView("Week");
                break;
            case "M": timeRangeSpinner.setSelection(2);
                evaluateTimeView("Month");
                break;
            case "Y": timeRangeSpinner.setSelection(3);
                evaluateTimeView("Year");
                break;
            default: evaluateTimeView("NoRepetition");
                break;
        }
    }

    public Integer getRepititon(){
        return repititionSpinner.mSpinner.getSelectedItemPosition();
    }

    public void setRepitition(int pintPosition){
        repititionSpinner.mSpinner.setSelection(pintPosition);
    }

    public void setMonthDetails(boolean pblnFirst,
                                boolean pblnMiddle,
                                boolean pblnLast,
                                boolean pblnAfter,
                                String pstrSpecific){
        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_First)).setChecked(pblnFirst);
        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_Middle)).setChecked(pblnMiddle);
        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_Last)).setChecked(pblnLast);
        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_AfterWkn)).setChecked(pblnAfter);
        String[] tmpArray = pstrSpecific.split(",");
        String value = "";
        for (int i = 0; i<tmpArray.length; i++){
            if (value != ""){
                value += ", ";
            }
            value += tmpArray[i];
            arrSpecificDays[i] = Integer.parseInt(tmpArray[i]);
        }
        ((EditText)findViewById(R.id.TimeKeeper_Monthly_Txt_Display)).setText(value);
    }
    //endregion

    private void instantiateTimeFragment(){
        AppCompatActivity context = (AppCompatActivity)getContext();
        DialogFragment newFragment = new TimeKeeper.TimePickerFragment();
        newFragment.show(context.getSupportFragmentManager(), "timePicker");
    }

    private void instantiateDateFragment(){
        AppCompatActivity context = (AppCompatActivity)getContext();
        DialogFragment newFragment = new TimeKeeper.DatePickerFragment();
        newFragment.show(context.getSupportFragmentManager(), "datePicker");
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);;
            int minute = Calendar.getInstance().get(Calendar.MINUTE);
            if (mFromTime != null){
                hour = mFromTime.get(Calendar.HOUR_OF_DAY);
                minute = mFromTime.get(Calendar.MINUTE);
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
            if (sSetIndicator == 1) {
                mFromTime = Calendar.getInstance();
                mFromTime .set(Calendar.HOUR_OF_DAY,hourOfDay);
                mFromTime .set(Calendar.MINUTE,minute);
                btnFromTime.setText("From Time: " + dateFormat.format(mFromTime.getTime()));
            } else if (sSetIndicator == 2) {
                mToTime = Calendar.getInstance();
                mToTime .set(Calendar.HOUR_OF_DAY,hourOfDay);
                mToTime .set(Calendar.MINUTE,minute);
                btnToTime.setText("To Time: " + dateFormat.format(mToTime.getTime()));
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener{

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH);
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

            if(sFromDate != null){
                year = sFromDate.get(Calendar.YEAR);
                month = sFromDate.get(Calendar.MONTH);
                day = sFromDate.get(Calendar.DAY_OF_MONTH);
            }

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the time chosen by the user
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
            switch (sSetIndicator) {
                case 3:
                    sFromDate = Calendar.getInstance();
                    sFromDate.set(Calendar.YEAR, year);
                    sFromDate.set(Calendar.MONTH, month);
                    sFromDate.set(Calendar.DAY_OF_MONTH, day);
                    btnFromDate.setText("From Date: " + dateFormat.format(sFromDate.getTime()));
                    break;

                case 4:
                    sToDate = Calendar.getInstance();
                    sToDate.set(Calendar.YEAR, year);
                    sToDate.set(Calendar.MONTH, month);
                    sToDate.set(Calendar.DAY_OF_MONTH, day);
                    btnToDate.setText("To Date: " + dateFormat.format(sToDate.getTime()));
                    break;
            }
        }
    }

    public static class NumberPickerDialog extends DialogFragment{
        private NumberPicker.OnValueChangeListener valueChangeListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final NumberPicker numberPicker = new NumberPicker(getActivity());

            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(31);

            Builder builder = new Builder(getActivity());
            builder.setTitle("Choose Value");
            builder.setMessage("Choose a number :");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    arrSpecificDays[intArrayCounter] = numberPicker.getValue();
                    intArrayCounter++;
                    String value = "";
                    for(int i=0; i< intArrayCounter; i++){
                        if (value != ""){
                            value = value + ", ";
                        }
                        value = value + arrSpecificDays[i];
                    }
                    txtMonthlyDays.setText(value);
                }
            });

            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setView(numberPicker);
            return builder.create();
        }
    }
}
