package com.satansoft.android.androidtaskmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class ProcessListFragment extends ListFragment {

    private static final String TAG = "ProcessListFragment";

    private TextView mProcessNameTextView;
    private TextView mProcessIdTextView;
    private TextView mProcessTotalPrivateDirtyTextView;
    private TextView mProcessTotalSharedDirtyTextView;
    private TextView mProcessTotalPSSTextView;

    List<ActivityManager.RunningAppProcessInfo> mRunningProcesses;

    private List<Debug.MemoryInfo> mMemoryInfos;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        updateProcessesInfo();

        setListAdapter(new MyAdapter(mRunningProcesses));
    }



    @Override
    public void onResume() {
        super.onResume();

        updateProcessesInfo();

        ((MyAdapter) getListAdapter()).notifyDataSetChanged();
    }



    private void updateProcessesInfo() {
        ActivityManager activityManager = (ActivityManager) getActivity()
                .getSystemService(Activity.ACTIVITY_SERVICE);


        mRunningProcesses = activityManager.getRunningAppProcesses();

        int[] pids = new int [mRunningProcesses.size()];

        for (int i = 0; i < mRunningProcesses.size(); i++) {
            pids[i] = mRunningProcesses.get(i).pid;
        }

        mMemoryInfos = Arrays.asList(activityManager.getProcessMemoryInfo(pids));
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

            mProcessTotalPrivateDirtyTextView = (TextView) convertView
                    .findViewById(R.id.process_memory_total_private_dirty);
            mProcessTotalSharedDirtyTextView = (TextView) convertView
                    .findViewById(R.id.process_memory_total_shared_dirty);
            mProcessTotalPSSTextView = (TextView) convertView
                    .findViewById(R.id.process_memory_total_pss);

            mProcessTotalPrivateDirtyTextView.setText("totalPrivateDirty: "
                    + mMemoryInfos.get(position).getTotalPrivateDirty() /1024 + " MB");
            mProcessTotalSharedDirtyTextView.setText("totalSharedDirty: "
                    + mMemoryInfos.get(position).getTotalSharedDirty() / 1024 + " MB");
            mProcessTotalPSSTextView.setText("totalPSS: "
                    + mMemoryInfos.get(position).getTotalPss() / 1024 + " MB");


            return convertView;
        }
    }

}
