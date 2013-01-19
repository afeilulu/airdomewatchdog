package com.afeilulu.airdomewatchdog;

import java.util.Calendar;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.afeilulu.airdomewatchdog.Utils.Md5Digest;
import com.afeilulu.airdomewatchdog.Utils.Utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class PrefActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener{
	
	final static String TAG = "PrefActivity";
	boolean isChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        for(int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
        	initSummary(getPreferenceScreen().getPreference(i));
        }
	}

	@Override
	protected void onPause() {
		super.onPause();
		
//		getPreferenceScreen().getSharedPreferences()
//        .unregisterOnSharedPreferenceChangeListener(this);
		
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
		}
		
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isChanged = false;
		
//		getPreferenceScreen().getSharedPreferences()
//        .registerOnSharedPreferenceChangeListener(this);
		
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
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
