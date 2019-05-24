package com.deviousindustries.testtask.SessionViewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import com.deviousindustries.testtask.Classes.Session;
import com.deviousindustries.testtask.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class Viewer_Session extends AppCompatActivity{

    //ArrayListContainer mSessionList;
    private ListView sessionList;
    private SessionViewerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_viewer_session);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.createSession(getBaseContext());
            }
        });

        Log.i("SessionViewer", "View Model Provider Called");
        viewModel = ViewModelProviders.of(this).get(SessionViewerViewModel.class);

        sessionList = findViewById(R.id.lsvSessionList);
        viewModel.getSessionList().observe(this, new Observer<List<Session>>() {
                @Override
                public void onChanged(List<Session> sessions) {
                    ArrayAdapter<Session> sessionAdapter = new ArrayAdapter<Session>(getBaseContext(),
                            R.layout.task_item1,
                            sessions){
                        @Override
                        public long getItemId(int position) {
                            return getItem(position).getTimeID();
                        }

                        @NonNull
                        @Override
                        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            if (convertView == null) {
                                convertView = ((LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.task_item1, null);
                            }

                            ((TextView)convertView.findViewById(R.id.title_text)).setText(getItem(position).getTitle());
                            return convertView;
                        }
                    };

                    sessionList.setAdapter(sessionAdapter);
                }
            }
        );

        sessionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewModel.viewSessionDetails(getBaseContext(),
                        sessionList.getItemIdAtPosition(position));
            }
        });

        sessionList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putLong("SessionID", sessionList.getItemIdAtPosition(position));
                DialogFragment newFragment = new Viewer_Session.DeleteSessionFragment();
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "Delete Session");

                return true;
            }
        });

//        mSessionList = new ArrayListContainer();
//        mSessionList.LinkArrayToListView(sessionList, this);
//        mSessionList.mListView.setOnItemClickListener(itemClickListener);
//        mSessionList.mListView.setOnItemLongClickListener(itemLongClickListener);
    }

    protected void onResume(){
        super.onResume();
        viewModel.loadSessionList();
    }

    public class DeleteSessionFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpSessionID = getArguments().getLong("SessionID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Session?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            viewModel.deleteSession(tmpSessionID);
                            viewModel.loadSessionList();
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
//
//    private void deleteSession(Long plngTimeId){
//        try{
//            DatabaseAccess.mDatabase.beginTransaction();
//            Time tempTime = new Time(plngTimeId);
//
//            try(Cursor cursor = DatabaseAccess.getTasksFromTime(tempTime.flngTimeID)){
//                while(cursor.moveToNext()){
//                    Task tempTask = new Task(cursor.getLong(cursor.getColumnIndex("flngTaskID")));
//                    //delete current task instances associated w/ session time
//                    tempTask.finishActiveInstances(3);
//                    //create new time that mimics deleted session and replace on task.
//                    tempTask.replaceTimeId(tempTime.getCopy().flngTimeID);
//                    //re-generation of instances will occur during task display logic.
//                }
//            }
//
//            tempTime.deleteTime();
//            DatabaseAccess.mDatabase.setTransactionSuccessful();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        DatabaseAccess.mDatabase.endTransaction();
//
//    }

}
