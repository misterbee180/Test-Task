package com.deviousindustries.testtask;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.deviousindustries.testtask.classes.Task;
import com.deviousindustries.testtask.classes.TaskInstance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import static com.deviousindustries.testtask.constants.ConstantsKt.*;

//Todo: add option to make long term task out of task display instances
public class Details_LongTerm extends AppCompatActivity {
    ArrayListContainer mLongTermTasksUnc = new ArrayListContainer();
    ArrayListContainer mLongTermTasksCmp = new ArrayListContainer();
    Long mlngTaskCount = (long)0;
    Long mlngLongTermID = (long)NULL_OBJECT;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_task_long_term);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Utilities.Companion.instantiate(getApplicationContext());

        fab = findViewById(R.id.AddSession_FAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeginAddTaskToLongTerm();
            }
        });

        //set up listviewer for later task insersion
        ListView LongTermTaskViewUnc = findViewById(R.id.lsvLongTermTaskListUnc);
        mLongTermTasksUnc.LinkArrayToListView(LongTermTaskViewUnc, this);
        mLongTermTasksUnc.mListView.setOnItemClickListener(itemClickListener);
        mLongTermTasksUnc.mListView.setOnItemLongClickListener(itemLongClickListener);
        ListView LongTermTaskViewCmp = findViewById(R.id.lsvLongTermTaskListCmp);
        mLongTermTasksCmp.LinkArrayToListView(LongTermTaskViewCmp, this);
        mLongTermTasksCmp.mListView.setOnItemClickListener(uncompleteTask);

        retrieveExtras();
    }

    @Override
    protected void onResume(){
        super.onResume();
        retrieveLongTermTasks();
        setupInitialVisibility();
        setupViews();
    }


    public  void BeginAddTaskToLongTerm() {
        Intent intent = new Intent(this, Details_Task.class);
        intent.putExtra("EXTRA_LONGTERM_ID", mlngLongTermID);
        startActivity(intent);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mLongTermTasksUnc.getID(position));
            DialogFragment newFragment = new TaskCompleteConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Complete Task");
        }
    };

    AdapterView.OnItemClickListener uncompleteTask = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mLongTermTasksCmp.getID(position));
            DialogFragment newFragment = new TaskUnCompleteConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Un-Complete Task");
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getBaseContext(), Details_Task.class);
            intent.putExtra("EXTRA_LONGTERM_ID", mlngLongTermID);
            switch(parent.getId()){
                case R.id.lsvLongTermTaskListUnc:
                    intent.putExtra("EXTRA_TASK_ID", mLongTermTasksUnc.getID(position));
                    break;
                case R.id.lsvLongTermTaskListCmp:
                    intent.putExtra("EXTRA_TASK_ID", mLongTermTasksCmp.getID(position));
                    break;
            }
            startActivity(intent);
            return true;
        }
    };

    public static class TaskCompleteConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long lngTaskId = getArguments().getLong("TaskID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Complete Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try{
                                DatabaseAccess.mDatabase.beginTransaction();
                                Task tempTask = new Task(lngTaskId);
                                TaskInstance ti = tempTask.generateInstance(NULL_DATE, NULL_DATE, false, false, false, NULL_OBJECT);
                                ti.finishInstance(2);

                                DatabaseAccess.mDatabase.setTransactionSuccessful();
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                            DatabaseAccess.mDatabase.endTransaction();
                            ((Details_LongTerm)getActivity()).retrieveLongTermTasks();
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

    public static class TaskUnCompleteConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long lngTaskId = getArguments().getLong("TaskID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Un-Complete Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try{
                                DatabaseAccess.mDatabase.beginTransaction();
                                Task tempTask = new Task(lngTaskId);
                                tempTask.clearInstances();
                                DatabaseAccess.mDatabase.setTransactionSuccessful();
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                            DatabaseAccess.mDatabase.endTransaction();
                            ((Details_LongTerm)getActivity()).retrieveLongTermTasks();
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

    private void retrieveLongTermTasks() {

        try(Cursor curRawUncomplete = DatabaseAccess.getTasksFromLongTerm(false,mlngLongTermID)){
            mLongTermTasksUnc.Clear();
            while (curRawUncomplete.moveToNext()){
                mlngTaskCount++;
                mLongTermTasksUnc.Add(curRawUncomplete.getString(curRawUncomplete.getColumnIndex("fstrTitle")),curRawUncomplete.getLong(curRawUncomplete.getColumnIndex("flngTaskID")));
            }
            mLongTermTasksUnc.mAdapter.notifyDataSetChanged();

        }

        try(Cursor curRawComplete = DatabaseAccess.getTasksFromLongTerm(true,mlngLongTermID)){
            mLongTermTasksCmp.Clear();
            while (curRawComplete.moveToNext()){
                mlngTaskCount++;
                mLongTermTasksCmp.Add(curRawComplete.getString(curRawComplete.getColumnIndex("fstrTitle")),curRawComplete.getLong(curRawComplete.getColumnIndex("flngTaskID")));
            }
            mLongTermTasksCmp.mAdapter.notifyDataSetChanged();
        }
    }

    private void retrieveExtras(){
        mlngLongTermID = getIntent().getLongExtra("EXTRA_LONGTERM_ID",NULL_OBJECT);
    }

    private void setupInitialVisibility() {
        //New LongTerm Add - Force adding the LongTerm before the ability to add tasks is available
        if (mlngLongTermID == NULL_OBJECT){
            fab.setVisibility(View.GONE);
            findViewById(R.id.lsvLongTermTaskListUnc).setVisibility(View.GONE);
            findViewById(R.id.lsvLongTermTaskListCmp).setVisibility(View.GONE);
            findViewById(R.id.txtLongTermAddReq).setVisibility(View.GONE);
        } else {
            //No Tasks Associated with LongTerm
            fab.setVisibility(View.VISIBLE);
            if (mlngTaskCount == 0){
                findViewById(R.id.txtLongTermAddReq).setVisibility(View.VISIBLE);
                findViewById(R.id.txtLongTermUnc).setVisibility(View.GONE);
                findViewById(R.id.txtLongTermCmp).setVisibility(View.GONE);
                findViewById(R.id.lsvLongTermTaskListUnc).setVisibility(View.GONE);
                findViewById(R.id.lsvLongTermTaskListCmp).setVisibility(View.GONE);
            } else {
                findViewById(R.id.txtLongTermAddReq).setVisibility(View.GONE);
                findViewById(R.id.txtLongTermUnc).setVisibility(View.VISIBLE);
                findViewById(R.id.txtLongTermCmp).setVisibility(View.VISIBLE);
                findViewById(R.id.lsvLongTermTaskListUnc).setVisibility(View.VISIBLE);
                findViewById(R.id.lsvLongTermTaskListCmp).setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupViews() {
        if (mlngLongTermID != NULL_OBJECT){
            Cursor cursor = DatabaseAccess.getRecordsFromTable("tblLongTerm", "flngLongTermID", mlngLongTermID);
            cursor.moveToFirst();
            setLongTermTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
            setLongTermDescription(cursor.getString(cursor.getColumnIndex("fstrDescription")));
        }
    }

    public void confirmActivity(View view){
        boolean blnContinue = false;
        if(mlngLongTermID == NULL_OBJECT){
            blnContinue = true;
        }
        mlngLongTermID = DatabaseAccess.addRecordToTable("tblLongTerm",
                new String[] {"fstrTitle","fstrDescription"},
                new String[] {getLongTermTitle(), getLongTermDescription()},
                "flngLongTermID",
                mlngLongTermID);
        if(blnContinue) setupInitialVisibility();
        else {
            setResult(RESULT_OK);
            finish();
        }
    }

    public String getLongTermTitle() {
        return ((TextView) findViewById(R.id.txbLongTermTitle)).getText().toString();
    }

    public void setLongTermTitle(String pstrTitle) {
        ((TextView) findViewById(R.id.txbLongTermTitle)).setText(pstrTitle);
    }

    public String getLongTermDescription() {
        return ((TextView) findViewById(R.id.txbLongTermDesc)).getText().toString();
    }

    public void setLongTermDescription(String pstrTitle) {
        ((TextView) findViewById(R.id.txbLongTermDesc)).setText(pstrTitle);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mlngLongTermID != NULL_OBJECT){
            getMenuInflater().inflate(R.menu.longterm_edit_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Bundle bundle;
        DialogFragment newFragment;

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            case R.id.action_delete_longterm:
                bundle = new Bundle();
                bundle.putLong("LongTermID", mlngLongTermID);
                newFragment = new LongTermDeleteConfirmationFragment();
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "Delete Task");
                break;
            case R.id.action_clear_longterm:
                bundle = new Bundle();
                bundle.putLong("LongTermID", mlngLongTermID);
                newFragment = new LongTermClearConfirmationFragment();
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "Clear Task");
            /*case R.id.action_session:
                viewSessions();
                break;
            case R.id.action_task:
                viewTasks();
                break;
            case R.id.action_LongTerm  :
                viewLongTerms();
                break;*/
        }

        return super.onOptionsItemSelected(item);
    }

    public static class LongTermDeleteConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpLongTermId = getArguments().getLong("LongTermID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete LongTerm? This will also delete all tasks associated with LongTerm.")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((Details_LongTerm)getActivity()).deleteLongTerm(tmpLongTermId);
                            getActivity().setResult(RESULT_OK);
                            getActivity().finish();
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

    public static class LongTermClearConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpLongTermId = getArguments().getLong("LongTermID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Clear LongTerm")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((Details_LongTerm)getActivity()).clearLongTerm(tmpLongTermId);
                            ((Details_LongTerm)getActivity()).retrieveLongTermTasks();
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

    public void deleteLongTerm(Long plngLongTermId){
        DatabaseAccess.deleteRecordFromTable("tblLongTerm",
                "flngLongTermID",
                plngLongTermId);

        clearLongTerm(plngLongTermId);
    }

    public void clearLongTerm(Long plngLongTermId){
        Cursor cursor = DatabaseAccess.retrieveTasksAssociatedWithLongTerm(plngLongTermId);
        while(cursor.moveToNext()){
            Task tempTask = new Task(cursor.getLong(cursor.getColumnIndex("flngTaskID")));
            tempTask.deleteTask();
        }
    }
}
