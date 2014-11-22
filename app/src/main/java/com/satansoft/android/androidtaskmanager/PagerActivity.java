package com.satansoft.android.androidtaskmanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class PagerActivity extends FragmentActivity {

    private static final String TAG = "PagerActivity";

    private String[] mFragments = { "MemoryInfoFragment",
                                    "HardwareInfoFragment",
                                    "ProcessListFragment" };


    private static final String[] titles = { "Memory Info",
                                             "Hardware Info",
                                             "Running Processes" };

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =
                new DemoCollectionPagerAdapter(
                        getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(5);        //wrong!
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
    }


    public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {


        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            try {
                return (Fragment) Class.forName("com.satansoft.android.androidtaskmanager."
                        + mFragments[i]).newInstance();
            }
            catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }


}
