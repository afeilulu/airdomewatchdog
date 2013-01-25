package com.afeilulu.airdomewatchdog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.afeilulu.airdomewatchdog.Utils.Md5Digest;
import com.afeilulu.airdomewatchdog.Utils.Utils;

public class PrefActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener{
	
	final static String TAG = "PrefActivity";
	boolean isChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate");
		
		if (savedInstanceState != null)
			isChanged = savedInstanceState.getBoolean(TAG);
		
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		getListView().setCacheColorHint(0);

        for(int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
        	initSummary(getPreferenceScreen().getPreference(i));
        	
        }
        
        Preference set_valve = (Preference) findPreference("set_valve");
        set_valve.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(PrefActivity.this,MinMaxActivity.class));
				return false;
			}});
	}

	@Override
	protected void onPause() {
		Log.d(TAG,"onPause");
		super.onPause();
		
//		getPreferenceScreen().getSharedPreferences()
//        .unregisterOnSharedPreferenceChangeListener(this);
		
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		Log.d(TAG,"onResume");
		super.onResume();
		
//		getPreferenceScreen().getSharedPreferences()
//        .registerOnSharedPreferenceChangeListener(this);
		
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG,"onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putBoolean(TAG, isChanged);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		Log.d(TAG,"onRestoreInstanceState");
		super.onRestoreInstanceState(state);
		if (state != null)
			isChanged = state.getBoolean(TAG);
	}

	@Override
	public void onBackPressed() {
		
		Log.d(TAG,"onBackPressed");
		if (isChanged){
			// save password in md5 string
			String password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", null);
			if (password != null && !TextUtils.isEmpty(password) && password.length() < 32){
				password = Md5Digest.MD5String(password);
				PreferenceManager
					.getDefaultSharedPreferences(this)
					.edit()
					.putString("password", password)
					.commit();
				Log.d(TAG, "password = " + password);
			}
			
			// save reboot time in milliseconds format
			String reboot_time = PreferenceManager.getDefaultSharedPreferences(this).getString("reboot_time", "23:00");
			if (reboot_time != null && !TextUtils.isEmpty(reboot_time)){
				long rebootTime = Utils.getTimeInMillis(reboot_time);
				PreferenceManager
					.getDefaultSharedPreferences(this)
					.edit()
					.putLong("reboot_time_long", rebootTime)
					.commit();
			}
			
			
			// start broadcast
			String action = "com.afeilulu.airdomewatchdog.intent.action.PREFERENCE_CHANGED";
			Intent intent = new Intent(action);
			this.sendBroadcast(intent);
			
			isChanged = false;
		}
		
		super.onBackPressed();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		isChanged = true;
		Log.d(TAG,"onSharedPreferenceChanged key = " + key);
		updatePrefSummary(findPreference(key));
	}
	
	private void initSummary(Preference p){
        if (p instanceof PreferenceCategory){
             PreferenceCategory pCat = (PreferenceCategory)p;
             for(int i=0;i<pCat.getPreferenceCount();i++){
                 initSummary(pCat.getPreference(i));
             }
         }else{
             updatePrefSummary(p);
         }

     }
	
	private void updatePrefSummary(Preference p){
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (!editTextPref.getKey().equalsIgnoreCase("password")
            		&& !editTextPref.getKey().equalsIgnoreCase("ftp_password"))
            	p.setSummary(editTextPref.getText()); 
        }
    }

}
