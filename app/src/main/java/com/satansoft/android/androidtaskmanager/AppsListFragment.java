package com.satansoft.android.androidtaskmanager;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class AppsListFragment extends ListFragment {

    private static final String TAG = "AppsListFragment";

    private TextView mAppLabelTextView;
    private ImageView mAppIconImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);

        Collections.sort(activities, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo resolveInfo1, ResolveInfo resolveInfo2) {
                PackageManager pm = getActivity().getPackageManager();
                return String.CASE_INSENSITIVE_ORDER.compare(
                        resolveInfo1.loadLabel(pm).toString(),
                        resolveInfo2.loadLabel(pm).toString()
                );
            }
        });


        setListAdapter(new MyAdapter(activities));

        Log.i(TAG, "I've found " + activities.size() + " activities.");
    }



    @Override
    public void onResume() {
        super.onResume();
        ((MyAdapter) getListAdapter()).notifyDataSetChanged();
    }



    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ResolveInfo resolveInfo = ((MyAdapter) l.getAdapter()).getItem(position);
        ActivityInfo activityInfo = resolveInfo.activityInfo;

        if (activityInfo == null) return;

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(activityInfo.packageName, activityInfo.name);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    private class MyAdapter extends ArrayAdapter<ResolveInfo> {

        public MyAdapter(List<ResolveInfo> objects) {
            super(getActivity(), 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.fragment_list_item_app, null);
            }

            PackageManager pm = getActivity().getPackageManager();
            ResolveInfo resolveInfo = getItem(position);

            mAppLabelTextView = (TextView) convertView
                    .findViewById(R.id.application_label);

            mAppLabelTextView.setText(resolveInfo.loadLabel(pm));

            mAppIconImageView = (ImageView) convertView
                    .findViewById(R.id.application_icon);

            mAppIconImageView.setImageDrawable(resolveInfo.loadIcon(pm));

            return convertView;
        }
    }

}
