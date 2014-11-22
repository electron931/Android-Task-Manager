package com.satansoft.android.androidtaskmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;


public class MemoryInfoFragment extends Fragment {

    private static final String TAG = "StartFragment";

    private boolean mIsExpanded;

    private long mTotalMemory;
    private long mAvailableMemory;
    private long mMemoryUsage;

    private long mTotalInternalMemorySize;
    private long mAvailableInternalMemorySize;
    private long mInternalMemoryUsageSize;


    private volatile boolean mIsActivityActive;

    private TextView mCpuExtendedInfoToggleTextView;
    private TextView mCpuExtendedInfoTextView;

    private ArcProgress mMemoryUsageArcProgress;
    private ArcProgress mInternalMemoryUsageArcProgress;

    private Button mClearCacheButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        updateMemoryInfo();
        updateInternalMemoryInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_memory_info, container, false);


        mMemoryUsageArcProgress = (ArcProgress) rootView
                .findViewById(R.id.memory_usage_arc_progress);
        int memoryUsageInPercents = (int) ( ( (float) mMemoryUsage
                / (float) mTotalMemory) * 100 );
        mMemoryUsageArcProgress.setProgress(memoryUsageInPercents);
        mMemoryUsageArcProgress.setArcAngle(240);
        mMemoryUsageArcProgress.setBottomText(
                mMemoryUsage / 1024 + "MB / " + mTotalMemory / 1024 + "MB");

        mInternalMemoryUsageArcProgress = (ArcProgress) rootView
                .findViewById(R.id.internal_memory_usage_arc_progress);
        updateInternalMemoryArcProgress();


        mClearCacheButton = (Button) rootView.findViewById(R.id.clear_cache_button);
        mClearCacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCache();
            }
        });


        return rootView;
    }



    @Override
    public void onResume() {
        super.onResume();

        mIsActivityActive = true;

        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (mIsActivityActive) {

                    updateMemoryInfo();

                    // Update the progress bar
                    mMemoryUsageArcProgress.post(new Runnable() {
                        public void run() {
                            int memoryUsageInPercents = (int) ( ( (float) mMemoryUsage
                                    / (float) mTotalMemory) * 100 );

                            mMemoryUsageArcProgress.setProgress(memoryUsageInPercents);
                            mMemoryUsageArcProgress.setBottomText(mMemoryUsage / 1024 + "MB / " + mTotalMemory / 1024 + "MB");
                        }
                    });
                }
            }
        }).start();
    }



    @Override
    public void onPause() {
        super.onPause();
        mIsActivityActive = false;
    }



    private void updateMemoryInfo() {
        mTotalMemory = getTotalMemory();
        mAvailableMemory = getAvailableMemory();
        mMemoryUsage = mTotalMemory - mAvailableMemory;
    }



    private void updateInternalMemoryInfo() {
        mTotalInternalMemorySize = getTotalInternalMemorySize();
        mAvailableInternalMemorySize = getAvailableInternalMemorySize();
        mInternalMemoryUsageSize = mTotalInternalMemorySize - mAvailableInternalMemorySize;
    }



    private long getAvailableMemory() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

        ActivityManager activityManager = (ActivityManager)
                getActivity().getSystemService(Activity.ACTIVITY_SERVICE);

        activityManager.getMemoryInfo(memoryInfo);

        return memoryInfo.availMem / 1024L;
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



    public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }



    public long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }



    private void clearCache() {
        PackageManager pm = getActivity().getPackageManager();
        // Get all methods on the PackageManager
        Method[] methods = pm.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals("freeStorage")) {
                // Found the method I want to use
                try {
                    long desiredFreeStorage = 8 * 1024 * 1024 * 1024; // Request for 8GB of free space
                    m.invoke(pm, Long.MAX_VALUE , null);
                } catch (Exception e) {
                    // Method invocation failed. Could be a permission problem
                    Log.e(TAG, e.getMessage());
                }
                break;
            }
        }

        long oldAvailableInternalMemorySize = mAvailableInternalMemorySize;

        updateInternalMemoryInfo();

        Toast.makeText(getActivity(), getString(R.string.cache_deleted_toast) + ": " +
                (mAvailableInternalMemorySize - oldAvailableInternalMemorySize) / 1048576 + " MB",
                Toast.LENGTH_SHORT).show();

        updateInternalMemoryArcProgress();
    }



    private void updateInternalMemoryArcProgress() {
        int internalMemoryUsageInPercents = (int) ( ( (float) mInternalMemoryUsageSize
                / (float) mTotalInternalMemorySize) * 100 );
        mInternalMemoryUsageArcProgress.setProgress(internalMemoryUsageInPercents);
        mInternalMemoryUsageArcProgress.setArcAngle(250);
        mInternalMemoryUsageArcProgress.setBottomText(
                String.format("%.2f", (float) (mInternalMemoryUsageSize) / 1073741824L)  + "GB / "+
                        String.format("%.2f", (float) (mTotalInternalMemorySize) / 1073741824L) + "GB");
    }

}
