package com.example.testtask;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Task_Event extends AppCompatActivity {

    ArrayListContainer mEventTasks = new ArrayListContainer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeginAddTaskToEvent();
            }
        });

        ListView eventTaskView = (ListView) findViewById(R.id.lsvEventTaskList);
        mEventTasks.LinkArrayToListView(eventTaskView, this);
        //mEventTasks.mListView.setOnItemClickListener(itemClickListener);
    }

    public void confirmActivity(View view){
        CreateEvent();

        setResult(RESULT_OK);
        finish();
    }

    private void CreateEvent() {
        ContentValues values = new ContentValues();
        values.put("fstrTitle",getEventTitle());
        values.put("fstrDescription", getEventDescription());
        Task_Display.mDataBase.insertOrThrow("tblEvent",null,values);


    }

    public void cancelActivity(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    public  void BeginAddTaskToEvent() {
        Intent intent = new Intent(this, Viewer_Task.class);
        startActivity(intent);
    }

    public String getEventTitle() {
        return ((TextView) findViewById(R.id.txbEventTitle)).getText().toString();
    }

    public void setEventTitle(String pstrTitle) {
        ((TextView) findViewById(R.id.txbEventTitle)).setText(pstrTitle);
    }

    public String getEventDescription() {
        return ((TextView) findViewById(R.id.txbEventDesc)).getText().toString();
    }

    public void setEventDescription(String pstrTitle) {
        ((TextView) findViewById(R.id.txbEventDesc)).setText(pstrTitle);
    }
}
