package com.satansoft.android.androidtaskmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;


public class StartFragment extends Fragment {

    private static final String TAG = "StartFragment";

    private TextView mNumberOfCpuTextView;
    private TextView mCpuUsageTextView;
    private TextView mMemoryUsageTextView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View viewRoot = inflater.inflate(R.layout.fragment_system_info, container, false);

        mNumberOfCpuTextView = (TextView) viewRoot.findViewById(R.id.number_of_cpu);

        int coreNumbers = getNumberOfCores();
        mNumberOfCpuTextView.setText("Cores number: " + coreNumbers);

        mCpuUsageTextView = (TextView) viewRoot.findViewById(R.id.cpu_usage);
        StringBuilder cpuUsage = new StringBuilder();
        for (int i = 0; i < coreNumbers; i++) {
            cpuUsage.append("Cpu").append(i).append(": ").append(readCore(i)).append("; ");
        }
        mCpuUsageTextView.setText(cpuUsage.toString());


        mMemoryUsageTextView = (TextView) viewRoot.findViewById(R.id.memory_usage);
        long totalMemory = getTotalMemory() / 1024;
        long availableMemory = getAvailableMemory() / 1024;
        long memoryUsage = totalMemory - availableMemory;
        mMemoryUsageTextView.setText("Total Memory: "
                + totalMemory + " MB; Available Memory: " + availableMemory + " MB; " +
                "Memory Usage: " + memoryUsage + " MB;");


        return viewRoot;
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



    //http://stackoverflow.com/questions/22405403/android-cpu-cores-reported-in-proc-stat
    //for multi core value
    private float readCore(int i) {
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
                catch (Exception e) {}

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
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
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
}
