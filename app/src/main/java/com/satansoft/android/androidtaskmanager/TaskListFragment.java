package com.satansoft.android.androidtaskmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class TaskListFragment extends ListFragment {

    private static final String TAG = "TaskListFragment";
    private static final int MAX_NUMBER_OF_TASKS = 100;

    private TextView mTaskTopActivityTextView;

    private List<ActivityManager.RunningTaskInfo> mRunningTasks;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager activityManager = (ActivityManager) getActivity()
                .getSystemService(Activity.ACTIVITY_SERVICE);
        mRunningTasks = activityManager.getRunningTasks(MAX_NUMBER_OF_TASKS);

        setListAdapter(new MyAdapter(mRunningTasks));
    }



    @Override
    public void onResume() {
        super.onResume();

        ActivityManager activityManager = (ActivityManager) getActivity()
                .getSystemService(Activity.ACTIVITY_SERVICE);
        mRunningTasks = activityManager.getRunningTasks(MAX_NUMBER_OF_TASKS);

        ((MyAdapter) getListAdapter()).notifyDataSetChanged();
    }



    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        ActivityManager.RunningTaskInfo runningTaskInfo
                = ((MyAdapter) listView.getAdapter()).getItem(position);
        Log.i(TAG, "task id " + runningTaskInfo.id);

        ActivityManager activityManager = (ActivityManager) getActivity()
                .getSystemService(Activity.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(runningTaskInfo.id, 0);
    }






    private class MyAdapter extends ArrayAdapter<ActivityManager.RunningTaskInfo> {

        public MyAdapter(List<ActivityManager.RunningTaskInfo> runningTaskInfos) {
            super(getActivity(), 0, runningTaskInfos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.fragment_list_item_task, null);
            }

            ActivityManager.RunningTaskInfo runningTaskInfo = getItem(position);



            mTaskTopActivityTextView = (TextView) convertView
                    .findViewById(R.id.task_top_activity_name);
            mTaskTopActivityTextView.setText(runningTaskInfo.topActivity.getPackageName());


            return convertView;
        }
    }

}
