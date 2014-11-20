package com.satansoft.android.androidtaskmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class CPUInfoFragment extends Fragment {

    private static final String TAG = "StartFragment";
    private static final int ARC_PROGRESS_SIZE = 300;

    private int mCoresNumber;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoresNumber = getNumberOfCores();
    }



    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LinearLayout cpuInsertionPointLayout = new LinearLayout(getActivity());
        cpuInsertionPointLayout.setOrientation(LinearLayout.VERTICAL);
        cpuInsertionPointLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));


        List<ArcProgress> arcProgressList = new ArrayList<ArcProgress>(mCoresNumber);

        for (int i = 0; i < mCoresNumber; i++) {
            int coreUsage = Math.round(getCoreUsage(i) * 100);
            ArcProgress arcProgress = (ArcProgress) getActivity()
                    .getLayoutInflater()
                    .inflate(R.layout.arcprogress_template, null);
            arcProgress.setBottomText(getString(R.string.cpu_number, i + 1));
            arcProgress.setProgress(coreUsage);

            arcProgressList.add(arcProgress);
        }

        fillLinearLayout(cpuInsertionPointLayout, arcProgressList);

        return cpuInsertionPointLayout;
    }





    private void fillLinearLayout(LinearLayout linearLayout, List<ArcProgress> collection) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int maxWidth = display.getWidth() - 10;

        ViewGroup.LayoutParams arcProgressLayoutParams =
                new ViewGroup.LayoutParams(ARC_PROGRESS_SIZE, ARC_PROGRESS_SIZE);


        if (collection.size() > 0) {
            LinearLayout insideLinearLayout = new LinearLayout(getActivity());
            insideLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            insideLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            insideLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);


            int widthSoFar = 0;

            for (ArcProgress item : collection) {
                widthSoFar += ARC_PROGRESS_SIZE;

                if (widthSoFar >= maxWidth) {
                    linearLayout.addView(insideLinearLayout);

                    insideLinearLayout = new LinearLayout(getActivity());
                    insideLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    insideLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    insideLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

                    insideLinearLayout.addView(item, arcProgressLayoutParams);
                    widthSoFar = 0;
                } else {
                    insideLinearLayout.addView(item, arcProgressLayoutParams);
                }
            }

            linearLayout.addView(insideLinearLayout);
        }
    }




    //http://stackoverflow.com/questions/22405403/android-cpu-cores-reported-in-proc-stat
    //for multi core value
    private float getCoreUsage(int i) {
        /*
        * how to calculate multicore
        * this function reads the bytes from a logging file in the android system (/proc/stat for cpu values)
        * then puts the line into a string
        * then splits up each individual part into an array
        * then(since he know which part represents what) we are able to determine each cpu total and work
        * then combine it together to get a single float for overall cpu usage
        */
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            //skip to the line we need
            for(int ii = 0; ii < i + 1; ++ii) {
                reader.readLine();
            }
            String load = reader.readLine();

            //cores will eventually go offline, and if it does, then it is at 0% because it is not being
            //used. so we need to do check if the line we got contains cpu, if not, then this core = 0
            if(load.contains("cpu")) {
                String[] toks = load.split(" ");

                //we are recording the work being used by the user and system(work) and the total info
                //of cpu stuff (total)
                //http://stackoverflow.com/questions/3017162/how-to-get-total-cpu-usage-in-linux-c/3017438#3017438

                long work1 = Long.parseLong(toks[1])+ Long.parseLong(toks[2]) + Long.parseLong(toks[3]);
                long total1 = Long.parseLong(toks[1])+ Long.parseLong(toks[2]) + Long.parseLong(toks[3]) +
                        Long.parseLong(toks[4]) + Long.parseLong(toks[5])
                        + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

                try {
                    //short sleep time = less accurate. But android devices typically don't have more than
                    //4 cores, and I'n my app, I run this all in a second. So, I need it a bit shorter
                    Thread.sleep(200);
                }
                catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

                reader.seek(0);
                //skip to the line we need
                for(int ii = 0; ii < i + 1; ++ii) {
                    reader.readLine();
                }
                load = reader.readLine();
                //cores will eventually go offline, and if it does, then it is at 0% because it is not being
                //used. so we need to do check if the line we got contains cpu, if not, then this core = 0%
                if(load.contains("cpu")) {
                    reader.close();
                    toks = load.split(" ");

                    long work2 = Long.parseLong(toks[1])+ Long.parseLong(toks[2]) + Long.parseLong(toks[3]);
                    long total2 = Long.parseLong(toks[1])+ Long.parseLong(toks[2]) + Long.parseLong(toks[3]) +
                            Long.parseLong(toks[4]) + Long.parseLong(toks[5])
                            + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);



                    //here we find the change in user work and total info, and divide by one another to get our total
                    //seems to be accurate need to test on quad core
                    //http://stackoverflow.com/questions/3017162/how-to-get-total-cpu-usage-in-linux-c/3017438#3017438

                    return (float)(work2 - work1) / ((total2 - total1));
                }
                else {
                    reader.close();
                    return 0;
                }

            }
            else {
                reader.close();
                return 0;
            }

        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }



    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    private int getNumberOfCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
            //Default to return 1 core
            return 1;
        }
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
