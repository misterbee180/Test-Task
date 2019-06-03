package com.deviousindustries.testtask.session_viewer;

import android.os.Bundle;

import com.deviousindustries.testtask.DatabaseAccess;
import com.deviousindustries.testtask.R;
import com.deviousindustries.testtask.Utilities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

public class SessionViewer extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_viewer_session);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Utilities.Companion.instantiate(getApplicationContext());
        setSupportActionBar(toolbar);
        setupViewFragment();
        //DatabaseAccess.forceWALCheckpoint();
    }

    private void setupViewFragment() {
        SessionViewerFragment sessionFragment =
                (SessionViewerFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (sessionFragment == null) {
            // Create the fragment
            sessionFragment = SessionViewerFragment.Companion.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.contentFrame, sessionFragment);
            transaction.commit();
        }
    }

}
