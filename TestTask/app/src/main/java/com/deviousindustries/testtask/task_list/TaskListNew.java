package com.deviousindustries.testtask.task_list;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.deviousindustries.testtask.R;
import com.deviousindustries.testtask.Utilities;
import com.deviousindustries.testtask.session_viewer.SessionViewerFragment;

public class TaskListNew extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer_session);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Utilities.Companion.instantiate(getApplicationContext());
        setSupportActionBar(toolbar);
        setupViewFragment();
        //DatabaseAccess.forceWALCheckpoint();
    }

    private void setupViewFragment() {
        TaskListFragment sessionFragment =
                (TaskListFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (sessionFragment == null) {
            // Create the fragment
            sessionFragment = TaskListFragment.Companion.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.contentFrame, sessionFragment);
            transaction.commit();
        }
    }

}
