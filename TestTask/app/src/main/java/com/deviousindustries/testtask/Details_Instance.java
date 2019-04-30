package com.deviousindustries.testtask;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Details_Instance extends AppCompatActivity {

    TimeKeeper timeKeeper;
    TextView mTitle;
    TextView mDescription;
    TaskInstance mInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_instance_details);
        mTitle = findViewById(R.id.txbTaskTitle);
        mDescription = findViewById(R.id.txbTaskDescription);
        timeKeeper = findViewById(R.id.timeKeeper);
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
                mInstance.mblnToDate,
                false);
    }

    public void ConfirmInstance(View view){
        DatabaseAccess.mDatabase.beginTransaction();
        try {
            //Todo: Only create a new task detail ID once to differentiate it from the task. Then just replace new detail id.
            //You can probably simply use the edited date to do know if this is already done or not.
            long lngTaskDetailID = DatabaseAccess.addRecordToTable("tblTaskDetail",
                    new String[] {"fstrTitle", "fstrDescription"},
                    new Object[] {getTaskTitle(), getTaskDesc()});

            //As the instance is being updated we are assuming new title / desc and new time.
            //This is logic that in the future might want to be made more robust.
            mInstance = new TaskInstance(mInstance.mlngInstanceID,
                    mInstance.mlngTaskID,
                    lngTaskDetailID,
                    timeKeeper.getFromDate(),
                    timeKeeper.getToDate(),
                    timeKeeper.mblnFromTime,
                    timeKeeper.mblnToTime,
                    timeKeeper.mblnToDate,
                    -1);

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
                //Todo: Delete task if instance is associated w/ non-repeating task. Possibly show additional fragment
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
        if(requestCode == 1 && resultCode == RESULT_OK)
            setResult(RESULT_OK);
            finish();
    }

    public static class InstanceDeleteConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Instance?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((Details_Instance)getActivity()).mInstance.finishInstance(3);
                            Intent intent = new Intent(getActivity(), Viewer_Tasklist.class);
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