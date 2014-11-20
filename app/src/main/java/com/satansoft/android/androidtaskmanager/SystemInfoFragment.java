package com.satansoft.android.androidtaskmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class SystemInfoFragment extends Fragment {

    private static final String TAG = "StartFragment";

    private boolean mIsExpanded;

    private long mTotalMemory;
    private long mAvailableMemory;
    private long mMemoryUsage;

    private TextView mCpuExtendedInfoToggleTextView;
    private TextView mCpuExtendedInfoTextView;
    private TextView mMemoryUsageTextView;
    private ArcProgress mMemoryUsageArcProgress;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTotalMemory = getTotalMemory();
        mAvailableMemory = getAvailableMemory();
        mMemoryUsage = mTotalMemory - mAvailableMemory;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_system_info, container, false);

        mCpuExtendedInfoTextView = (TextView)
                rootView.findViewById(R.id.cpu_extended_info);
        mCpuExtendedInfoTextView.setVisibility(View.GONE);
        mCpuExtendedInfoTextView.setText(getCPUInfo());


        mCpuExtendedInfoToggleTextView = (TextView)
                rootView.findViewById(R.id.cpu_extended_info_toggle);
        mCpuExtendedInfoToggleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsExpanded = !mIsExpanded;
                if (mIsExpanded) {
                    collapse(mCpuExtendedInfoTextView);
                }
                else {
                    expand(mCpuExtendedInfoTextView);
                }
            }
        });



        mMemoryUsageArcProgress = (ArcProgress) rootView
                .findViewById(R.id.memory_usage_arc_progress);
        int memoryUsageInPercents = (int) ( ( (float) mMemoryUsage
                / (float) mTotalMemory) * 100 );
        mMemoryUsageArcProgress.setProgress(memoryUsageInPercents);
        mMemoryUsageArcProgress.setBottomText(mMemoryUsage / 1024 + "MB / " + mTotalMemory / 1024 + "MB");


        return rootView;
    }



    private long getAvailableMemory() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager)
                getActivity().getSystemService(Activity.ACTIVITY_SERVICE);

        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem / 1024L;     //bytes to kB
    }



    private long getTotalMemory() {
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        long totalMemory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();              //meminfo
            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }
            //total Memory
            totalMemory = Integer.valueOf(arrayOfString[1]);            //kB
            localBufferedReader.close();
            return totalMemory;
        }
        catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }



    private void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }



    private String getCPUInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("abi: ").append(Build.CPU_ABI).append("\n");
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    stringBuilder.append(aLine).append("\n");
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }
}
