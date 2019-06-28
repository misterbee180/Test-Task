package com.deviousindustries.testtask.instance_display;

import android.os.Bundle;

import com.deviousindustries.testtask.R;
import com.deviousindustries.testtask.Utilities;
import com.deviousindustries.testtask.session_viewer.SessionViewerFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Utilities.Companion.instantiate(getApplicationContext());
        setupViewFragment();
        //DatabaseAccess.forceWALCheckpoint();
    }

    private void setupViewFragment() {
        InstanceDisplayFragment sessionFragment =
                (InstanceDisplayFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (sessionFragment == null) {
            // Create the fragment
            sessionFragment = InstanceDisplayFragment.Companion.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.contentFrame, sessionFragment);
            transaction.commit();
        }
    }

}
