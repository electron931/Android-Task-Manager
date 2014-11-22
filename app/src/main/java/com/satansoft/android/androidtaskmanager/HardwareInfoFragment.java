package com.satansoft.android.androidtaskmanager;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HardwareInfoFragment extends Fragment {

    private static final String TAG = "HardwareInfoFragment";

    private TextView mGeneralInfoToggleTextView;
    private TextView mGeneralInfoTextView;

    private TextView mDisplayInfoToggleTextView;
    private TextView mDisplayInfoTextView;

    private TextView mCpuInfoToggleTextView;
    private TextView mCpuInfoTextView;

    private TextView mSensorsInfoToggleTextView;
    private TextView mSensorsInfoTextView;

    private TextView mCameraInfoToggleTextView;
    private TextView mCameraInfoTextView;

    private boolean mIsGeneralExpanded;
    private boolean mIsDisplayExpanded;
    private boolean mIsCpuExpanded;
    private boolean mIsSensorsExpanded;
    private boolean mIsCameraExpanded;


    private int mWidthPixels;
    private int mHeightPixels;
    private double mWidthInches;
    private double mHeightInches;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRealDeviceSizeInPixels();
        calculateDeviceSizeInInches();
    }



    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hardware_info, container, false);

        mGeneralInfoTextView = (TextView)
                rootView.findViewById(R.id.general_info);
        mGeneralInfoTextView.setVisibility(View.GONE);
        mGeneralInfoTextView.setText(getGeneralInfo());


        mGeneralInfoToggleTextView = (TextView)
                rootView.findViewById(R.id.general_info_toggle);
        mGeneralInfoToggleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsGeneralExpanded) {
                    collapse(mGeneralInfoTextView);
                }
                else {
                    expand(mGeneralInfoTextView);
                }

                mIsGeneralExpanded = !mIsGeneralExpanded;
            }
        });



        mDisplayInfoTextView = (TextView)
                rootView.findViewById(R.id.display_info);
        mDisplayInfoTextView.setVisibility(View.GONE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mDisplayInfoTextView.setText("Diagonal (inches) : " + String.format("%.2f",
                Math.sqrt(mWidthInches*mWidthInches + mHeightInches * mHeightInches)) + "\n\n" +
                                        "Width (inches) : " + mWidthInches + "\n" +
                                        "Height (inches) : " + mHeightInches + "\n\n" +
                                        "Width (pixels) : " + mWidthPixels + "\n" +
                                        "Height (pixels) : " + mHeightPixels + "\n\n" +
                                        "DPI (dots per inch) : " + displayMetrics.densityDpi);


        mDisplayInfoToggleTextView = (TextView)
                rootView.findViewById(R.id.display_info_toggle);
        mDisplayInfoToggleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsDisplayExpanded) {
                    collapse(mDisplayInfoTextView);
                }
                else {
                    expand(mDisplayInfoTextView);
                }

                mIsDisplayExpanded = !mIsDisplayExpanded;
            }
        });



        mCpuInfoTextView = (TextView)
                rootView.findViewById(R.id.cpu_info);
        mCpuInfoTextView.setVisibility(View.GONE);
        mCpuInfoTextView.setText(getCPUInfo());


        mCpuInfoToggleTextView = (TextView)
                rootView.findViewById(R.id.cpu_info_toggle);
        mCpuInfoToggleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsCpuExpanded) {
                    collapse(mCpuInfoTextView);
                }
                else {
                    expand(mCpuInfoTextView);
                }

                mIsCpuExpanded = !mIsCpuExpanded;
            }
        });





        mSensorsInfoTextView = (TextView)
                rootView.findViewById(R.id.sensors_info);
        mSensorsInfoTextView.setVisibility(View.GONE);
        mSensorsInfoTextView.setText(getSenorsInfo());


        mSensorsInfoToggleTextView = (TextView)
                rootView.findViewById(R.id.sensors_info_toggle);
        mSensorsInfoToggleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsSensorsExpanded) {
                    collapse(mSensorsInfoTextView);
                }
                else {
                    expand(mSensorsInfoTextView);
                }

                mIsSensorsExpanded = !mIsSensorsExpanded;
            }
        });

        mCameraInfoToggleTextView = (TextView)
                rootView.findViewById(R.id.camera_info_toggle);
        mCameraInfoToggleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsCameraExpanded) {
                    mCameraInfoTextView.setVisibility(View.GONE);
                }
                else {
                    mCameraInfoTextView.setVisibility(View.VISIBLE);
                }

                mIsCameraExpanded = !mIsCameraExpanded;
            }
        });


        mCameraInfoTextView = (TextView)
                rootView.findViewById(R.id.camera_info);

        StringBuilder stringBuilder = new StringBuilder();

        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                stringBuilder.append("Front Camera");
            }
            else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                stringBuilder.append("Back Camera");
            }

            try {
                cam = Camera.open(camIdx);
                Map<String, String> cameraParameters = getFullCameraParameters(cam);
                cam.release();

                stringBuilder.append("\n\n")
                             .append(getStringFromMap(cameraParameters))
                             .append("\n\n");

            }
            catch (RuntimeException e) {
                Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
            }
        }

        mCameraInfoTextView.setText(stringBuilder.toString());



        return rootView;
    }



    private String getGeneralInfo() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append("Manufacturer")
                .append(" : ")
                .append(Build.MANUFACTURER)
                .append("\n\n");
        stringBuilder
                .append("Model")
                .append(" : ")
                .append(Build.MODEL)
                .append("\n\n");
        stringBuilder
                .append("Product")
                .append(" : ")
                .append(Build.PRODUCT)
                .append("\n\n");
        stringBuilder
                .append("Hardware")
                .append(" : ")
                .append(Build.HARDWARE)
                .append("\n\n");
        stringBuilder
                .append("Serial")
                .append(" : ")
                .append(Build.SERIAL);


        return stringBuilder.toString();
    }



    private void calculateDeviceSizeInInches() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mWidthInches = mWidthPixels / displayMetrics.densityDpi;
        mHeightInches = mHeightPixels / displayMetrics.densityDpi;
    }



    private void setRealDeviceSizeInPixels() {
        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);


        // since SDK_INT = 1;
        mWidthPixels = displayMetrics.widthPixels;
        mHeightPixels = displayMetrics.heightPixels;

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                mWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                mHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception ignored) {
            }
        }

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                mWidthPixels = realSize.x;
                mHeightPixels = realSize.y;
            }
            catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
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



    private String getSenorsInfo() {
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        StringBuilder stringBuilder = new StringBuilder();

        for(Sensor sensor : sensors) {
            stringBuilder.append("Name: ").append(sensor.getName()).append("\n");
            stringBuilder.append("Vendor: ").append(sensor.getVendor()).append("\n");
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }


    //some crazy stuff
    private static Map<String, String> getFullCameraParameters (Camera cam) {
        Map<String, String> result = new HashMap<String, String>(64);
        final String TAG = "CameraParametersRetrieval";

        try {
            Class camClass = cam.getClass();

            //Internally, Android goes into native code to retrieve this String of values
            Method getNativeParams = camClass.getDeclaredMethod("native_getParameters");
            getNativeParams.setAccessible(true);

            //Boom. Here's the raw String from the hardware
            String rawParamsStr = (String) getNativeParams.invoke(cam);

            //But let's do better. Here's what Android uses to parse the
            //String into a usable Map -- a simple ';' StringSplitter, followed
            //by splitting on '='
            //
            //Taken from Camera.Parameters unflatten() method
            TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(';');
            splitter.setString(rawParamsStr);

            for (String kv : splitter) {
                int pos = kv.indexOf('=');
                if (pos == -1) {
                    continue;
                }
                String k = kv.substring(0, pos);
                String v = kv.substring(pos + 1);
                result.put(k, v);
            }

            //And voila, you have a map of ALL supported parameters
            return result;
        } catch (NoSuchMethodException ex) {
            Log.e(TAG, ex.toString());
        } catch (IllegalAccessException ex) {
            Log.e(TAG, ex.toString());
        } catch (InvocationTargetException ex) {
            Log.e(TAG, ex.toString());
        }

        //If there was any error, just return an empty Map
        Log.e(TAG, "Unable to retrieve parameters from Camera.");
        return result;
    }



    private String getStringFromMap(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry entry : map.entrySet()) {
            stringBuilder
                    .append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue())
                    .append(";")
                    .append("\n\n");
        }

        return stringBuilder.toString();
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
        a.setDuration((int)(targetHeight / v.getContext()
                .getResources()
                .getDisplayMetrics().density));
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
                    v.getLayoutParams().height = initialHeight -
                            (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext()
                .getResources()
                .getDisplayMetrics().density));
        v.startAnimation(a);
    }

}
