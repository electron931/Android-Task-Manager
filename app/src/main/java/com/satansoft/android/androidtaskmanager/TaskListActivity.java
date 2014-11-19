package com.satansoft.android.androidtaskmanager;

import android.support.v4.app.Fragment;


public class TaskListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new TaskListFragment();
    }

}
