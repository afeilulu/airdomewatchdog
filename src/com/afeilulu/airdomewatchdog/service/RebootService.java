package com.afeilulu.airdomewatchdog.service;

import com.afeilulu.airdomewatchdog.Utils.Utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class RebootService extends ALongRunningNonStickyBroadcastService {
	
	public static String TAG = "RebootService";
	public NotificationManager mNM;

	public RebootService(String name) {
		super(name);
	}
	
	//Required by IntentService
	public RebootService()
	{
		super("com.afeilulu.airdomewatchdog.service.RebootService");
	}

	@Override
	protected void handleBroadcastIntent(Intent broadcastIntent) {
		
		SharedPreferences prefs =
    			PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		
		// waiting for upload service end.
		while(prefs.getBoolean("is_upload_running", true)){
			Utils.sleepForInSecs(5);
		}
		
		try {
	        Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", "reboot" });
	        proc.waitFor();
	    } catch (Exception ex) {
	        Log.i(TAG, "Could not reboot", ex);
	    }
		
//		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		pm.reboot(null);

	}
	
	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	private final IBinder mBinder = new Binder() {
        @Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
		        int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

}
