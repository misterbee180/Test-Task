package com.example.testtask;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Details_Instance extends AppCompatActivity {

    static TimeKeeper timeKeeper;
    static TextView mTitle;
    static TextView mDescription;
    static TaskInstance mInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_details);
        mTitle = (TextView) findViewById(R.id.txbTaskTitle);
        mDescription = (TextView) findViewById(R.id.txbTaskDescription);
        timeKeeper = (TimeKeeper) findViewById(R.id.timeKeeper);
        timeKeeper.setMode(3);
        retrieveExtras();
    }

    public String getTaskTitle(){
        return mTitle.getText().toString();
    }

    public void setTaskTitle(String pstrTitle){
        mTitle.setText(pstrTitle);
    }

    public String getTaskDesc(){
        return mDescription.getText().toString();
    }

    public void setTaskDesc(String pstrDescription){
        mDescription.setText(pstrDescription);
    }

    private void retrieveExtras() {
        long instance_id = getIntent().getLongExtra("EXTRA_INSTANCE_ID",-1);
        mInstance = new TaskInstance(instance_id);
        setTaskTitle(mInstance.mstrTitle);
        setTaskDesc(mInstance.mstrDescription);
        timeKeeper.loadTimeDetails(mInstance.mdtmFrom,
                mInstance.mdtmTo,
                mInstance.mblnFromTime,
                mInstance.mblnToTime,
                mInstance.mblnToDate);
    }

    public void ConfirmInstance(View view){
        DatabaseAccess.mDatabase.beginTransaction();
        try {
            mInstance.updateTaskInstance(getTaskTitle(),
                    getTaskDesc(),
                    TimeKeeper.getFromDate(),
                    TimeKeeper.getToDate(),
                    TimeKeeper.mblnFromTime,
                    TimeKeeper.mblnToTime,
                    TimeKeeper.mblnToDate,
                    Task_Display.getCurrentCalendar().getTimeInMillis());

            setResult(RESULT_OK);
            finish();
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseAccess.mDatabase.endTransaction();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_instance_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            case R.id.action_delete_instance:
                DialogFragment newFragment = new InstanceDeleteConfirmationFragment();
                newFragment.show(getSupportFragmentManager(), "Delete Instance");
                break;
            case R.id.action_view_task:
                Intent intent = new Intent(this, Details_Task.class);
                intent.putExtra("EXTRA_TASK_ID", mInstance.mlngTaskID);
                startActivityForResult(intent, 1);
            /*case R.id.action_delete_task:
                B*/
            /*case R.id.action_session:
                viewSessions();
                break;
            case R.id.action_task:
                viewTasks();
                break;
            case R.id.action_event  :
                viewEvents();
                break;*/
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            }
        }
    }

    public static class InstanceDeleteConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpInstanceID = getArguments().getLong("InstanceID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Instance?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mInstance.deleteInstance();
                            Intent intent = new Intent(getActivity(), Task_Display.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
