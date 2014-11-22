package com.satansoft.android.androidtaskmanager;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class PagerActivity extends FragmentActivity {

    private static final String TAG = "PagerActivity";

    private String[] mFragments = { "MemoryInfoFragment",
                                    "HardwareInfoFragment",
                                    "ProcessListFragment",
                                    "AppsListFragment"};

    private Resources mResources;



    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        mResources = getResources();

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =
                new DemoCollectionPagerAdapter(
                        getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(5);                //wrong!
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
    }


    public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

        private String[] titles = { mResources.getString(R.string.memory_info_text),
                mResources.getString(R.string.hardware_info_text),
                mResources.getString(R.string.processes_info_text),
                mResources.getString(R.string.apps_info_text) };

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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}
