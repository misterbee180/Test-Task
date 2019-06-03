package com.deviousindustries.testtask;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.deviousindustries.testtask.classes.Task;
import com.deviousindustries.testtask.classes.TaskInstance;
import com.deviousindustries.testtask.classes.Time;
import static com.deviousindustries.testtask.constants.ConstantsKt.*;

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
        Utilities.Companion.instantiate(getApplicationContext());
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
        long instance_id = getIntent().getLongExtra("EXTRA_INSTANCE_ID",NULL_OBJECT);
        mInstance = new TaskInstance(instance_id);
        setTaskTitle(mInstance.fstrTitle);
        setTaskDesc(mInstance.fstrDescription);
        timeKeeper.loadTimeDetails(mInstance.fdtmFrom,
                mInstance.fdtmTo,
                mInstance.fblnFromTime,
                mInstance.fblnToTime,
                mInstance.fblnToDate,
                false);
    }

    public void ConfirmInstance(View view){
        DatabaseAccess.mDatabase.beginTransaction();
        try {
            long lngTaskDetailID = DatabaseAccess.addRecordToTable("tblTaskDetail",
                    new String[] {"fstrTitle", "fstrDescription"},
                    new Object[] {getTaskTitle(), getTaskDesc()});

            //As the instance is being updated we are assuming new title / desc and new mTime.
            //This is logic that in the future might want to be made more robust.
            mInstance = new TaskInstance(mInstance.flngInstanceID,
                    mInstance.flngTaskID,
                    lngTaskDetailID,
                    timeKeeper.getFromDate(),
                    timeKeeper.getToDate(),
                    timeKeeper.fblnFromTime,
                    timeKeeper.fblnToTime,
                    timeKeeper.fblnToDate,
                    NULL_OBJECT);

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
                Bundle args = new Bundle();
                args.putLong("instance",mInstance.flngInstanceID);
                args.putLong("task",mInstance.flngTaskID);
                DialogFragment newFragment = new InstanceDeleteConfirmationFragment();
                newFragment.setArguments(args);
                newFragment.show(getSupportFragmentManager(), "Delete Instance");
                break;
            case R.id.action_view_task:
                Intent intent = new Intent(this, Details_Task.class);
                intent.putExtra("EXTRA_TASK_ID", mInstance.flngTaskID);
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
            Bundle args = getArguments();
            final long lngInstanceID = args.getLong("instance",NULL_OBJECT);
            final long lngTaskID = args.getLong("task",NULL_OBJECT);
            builder.setMessage("Delete Instance?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try{
                                DatabaseAccess.mDatabase.beginTransaction();
                                Task tt = new Task(lngTaskID);
                                Time tm = new Time(tt.flngTimeID);
                                if(tm.fintRepetition <= BASE_POSITION){
                                    tt.deleteTask();
                                } else {
                                    TaskInstance ti = new TaskInstance(lngInstanceID);
                                    ti.finishInstance(3);
                                }

                                DatabaseAccess.mDatabase.setTransactionSuccessful();
                                Intent intent = new Intent(getActivity(), Viewer_Tasklist.class);
                                startActivity(intent);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            DatabaseAccess.mDatabase.endTransaction();
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
