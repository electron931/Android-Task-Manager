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

public class ProcessListFragment extends ListFragment {

    private static final String TAG = "ProcessListFragment";

    private TextView mProcessNameTextView;
    private TextView mProcessIdTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager activityManager = (ActivityManager) getActivity()
                .getSystemService(Activity.ACTIVITY_SERVICE);


        List<ActivityManager.RunningAppProcessInfo> runningProcesses
                = activityManager.getRunningAppProcesses();

        setListAdapter(new MyAdapter(runningProcesses));
    }



    @Override
    public void onResume() {
        super.onResume();
        ((MyAdapter) getListAdapter()).notifyDataSetChanged();
    }



    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        /*ActivityManager.RunningAppProcessInfo runningProcessInfo
                = ((MyAdapter) listView.getAdapter()).getItem(position);
        ActivityManager activityManager = (ActivityManager) getActivity()
                .getSystemService(Activity.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(runningProcessInfo.processName);

        ((MyAdapter) getListAdapter()).notifyDataSetChanged();*/
    }






    private class MyAdapter extends ArrayAdapter<ActivityManager.RunningAppProcessInfo> {

        public MyAdapter(List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos) {
            super(getActivity(), 0, runningAppProcessInfos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.fragment_list_item_process, null);
            }

            ActivityManager.RunningAppProcessInfo runningProcessInfo = getItem(position);

            mProcessNameTextView = (TextView) convertView
                    .findViewById(R.id.process_name);
            mProcessNameTextView.setText(runningProcessInfo.processName);

            mProcessIdTextView = (TextView) convertView
                    .findViewById(R.id.process_id);
            mProcessIdTextView.setText("pid: " + runningProcessInfo.pid);


            return convertView;
        }
    }

}
