package com.satansoft.android.androidtaskmanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public abstract class SingleFragmentActivity extends FragmentActivity {
	
	protected abstract Fragment createFragment();



    protected int getLayoutRedId() {
        return R.layout.activity_fragment;
    }



	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRedId());
        
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        
        if (fragment == null) {
        	fragment = createFragment();
        	fm.beginTransaction()
        		.add(R.id.fragment_container, fragment)
        		.commit();
        }
    }

}
