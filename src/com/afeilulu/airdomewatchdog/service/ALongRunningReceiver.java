package com.afeilulu.airdomewatchdog.service;

import java.util.Calendar;

import com.afeilulu.airdomewatchdog.R;
import com.afeilulu.airdomewatchdog.Utils.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/*
 * This is a stand-in broadcast receiver
 * that delegates the work to a service
 * named by the derived class.
 * 
 * The original intent that the broadcast receiver
 * is invoked on is transferred to the 
 * delegated non-sticky service to be handled
 * as a parcellable.
 * 
 * On entry this will set up the 
 * lighted green room. Essentially making the device
 * on.
 * 
 * The service will do the same, if it were to 
 * be woken up due to pending intents.
 */
public abstract class  ALongRunningReceiver 
extends BroadcastReceiver
{
	private static final String tag = "ALongRunningReceiver"; 
	private static final long RebootIntervalInMilliseconds = 24 * 60 * 60 * 1000;
	private PendingIntent mAlarmSender;
	private PendingIntent mUploadIntent;
	private PendingIntent mRebootIntent;
	
    @Override
    public void onReceive(Context context, Intent intent) 
    {
    	Log.d(tag,"Receiver started");
    	LightedGreenRoom.setup(context);
    	startService(context,intent);
    	Log.d(tag,"Receiver finished");
    }
    private void startService(Context context, Intent intent)
    {
    	SharedPreferences prefs =
    			PreferenceManager.getDefaultSharedPreferences(context);

    	Intent serviceIntent;
    	boolean useFtp = prefs.getBoolean("ftp", false);
    	/*
    	if (useFtp)
    		serviceIntent = new Intent(context,getLRSFtp());
    	else
    		serviceIntent = new Intent(context,getLRSClass());
    	*/
    	serviceIntent = new Intent(context,getLRSFtp());
    	
    	intent.putExtra("project_id",prefs.getString("project_id", null));
    	intent.putExtra("password",prefs.getString("password", null));
    	intent.putExtra("webservice_url",prefs.getString("webservice_url", null));
    	intent.putExtra("interval",prefs.getString("interval", null));
    	intent.putExtra("upload_interval",prefs.getString("upload_interval", null));
    	intent.putExtra("next_time",prefs.getString("next_time", null));
    	
    	// initial value is false
    	prefs.edit().putBoolean("is_upload_running", false).commit();
    	
    	// original intent is refered in handleBroadcastIntent
    	serviceIntent.putExtra("original_intent", intent);  
    	
    	long interval = 0; // minutes
    	String next_time = null; //hh:mm
    	if (prefs.getString("interval", null) != null)
    		interval = Integer.parseInt(prefs.getString("interval", null));
    	if (prefs.getString("next_time", null) != null)
    		next_time = prefs.getString("next_time", null);
    	
    	long upload_interval = 0;
    	if (prefs.getString("upload_interval", null) != null)
    		upload_interval = Integer.parseInt(prefs.getString("upload_interval", null));
    	
    	long firstTime = 0;
    	if (next_time == null){
    		firstTime = System.currentTimeMillis();
    		next_time = "Now";
    	}
//    	else
//    		firstTime = Utils.getTimeInMillis(next_time);
    	
    	// schedule ModbusSampler service 
    	mAlarmSender = PendingIntent.getService(context,
                0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        
        am.setRepeating(AlarmManager.RTC_WAKEUP,
        		prefs.getBoolean("exception_happened", false)? firstTime + interval * 60 * 1000:firstTime,
                        interval * 60 * 1000,
                        mAlarmSender);
        
        // *********************************************************************************
        AlarmManager amUpload = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        // schedule xml upload service
        Intent uploadIntent = new Intent(context,getLRSUploadClass());
    	
    	// original intent is refered in handleBroadcastIntent
        uploadIntent.putExtra("original_intent", intent); 
        
        mUploadIntent = PendingIntent.getService(context,
                0, uploadIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        // Schedule the upload!
        long upload_interval_millisecond = upload_interval * 60 * 1000;
        long upload_firstTime = firstTime + upload_interval_millisecond;
        amUpload.setRepeating(AlarmManager.RTC_WAKEUP,
                        upload_firstTime, 
                        upload_interval_millisecond, mUploadIntent);
         
        // Tell the user about what we did.
        
        String schedule = context.getString(R.string.repeating_scheduled, next_time, interval, upload_interval_millisecond / 60000);
        Toast.makeText(context, schedule,
                Toast.LENGTH_LONG).show();
        // *********************************************************************************
        
        // schdule for reboot
        boolean rebootNeeded = prefs.getBoolean("reboot_needed", false);
        if (rebootNeeded){
        	Intent rebootIntent = new Intent(context,getRebootClass());
    		long rebootTime = prefs.getLong("reboot_time_long", System.currentTimeMillis());
    		
    		mRebootIntent = PendingIntent.getService(context,
                    1, rebootIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    		
    		if (rebootTime <= System.currentTimeMillis()){
    			rebootTime = rebootTime + RebootIntervalInMilliseconds;
    			prefs.edit().putLong("reboot_time_long", rebootTime).commit();
    		}
    		Toast.makeText(context,
    				"reboot millis : " + rebootTime
    				+ "\nSystem millis : " + System.currentTimeMillis(), Toast.LENGTH_LONG).show();
    		
            // Schedule the upload!
        	AlarmManager am2 = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am2.setRepeating(AlarmManager.RTC_WAKEUP,
            		rebootTime , RebootIntervalInMilliseconds, mRebootIntent);
        	
        }
    }
    /*
     * Override this methode to return the 
     * "class" object belonging to the 
     * nonsticky service class.
     */
    public abstract Class getLRSClass();
    
    public abstract Class getLRSUploadClass();
    
    public abstract Class getLRSFtp();
    
    public abstract Class getRebootClass();
    
}

