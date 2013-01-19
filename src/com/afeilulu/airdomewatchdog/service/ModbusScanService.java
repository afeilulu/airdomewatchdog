package com.afeilulu.airdomewatchdog.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.afeilulu.airdomewatchdog.MainActivity;
import com.afeilulu.airdomewatchdog.R;
import com.afeilulu.airdomewatchdog.Utils.Utils;
import com.afeilulu.airdomewatchdog.moudle.Delivery;
import com.afeilulu.airdomewatchdog.moudle.ModbusSample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/*
 * Uses IntentService as the base class
 * to make this work on a separate thread.
 * 
 * need wifi to run this service
 */
public class ModbusScanService 
extends ALongRunningNonStickyBroadcastService
{
	public static String tag = "ModbusScanService";
	public NotificationManager mNM;
	
	//Required by IntentService
	public ModbusScanService()
	{
		super("com.afeilulu.airdomewatchdog.service.ModbusScanService");
	}

	/*
	 * Perform long running operations in this method.
	 * This is executed in a separate thread. 
	 */
	@Override
	protected void handleBroadcastIntent(Intent broadcastIntent) 
	{
		Log.d(tag,broadcastIntent.getStringExtra("project_id"));
		Log.d(tag,broadcastIntent.getStringExtra("password"));
		Log.d(tag,broadcastIntent.getStringExtra("webservice_url"));
		Log.d(tag,broadcastIntent.getStringExtra("interval"));
		
		String project_id = broadcastIntent.getStringExtra("project_id");
		String password = broadcastIntent.getStringExtra("password");
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		SharedPreferences prefs =
    			PreferenceManager.getDefaultSharedPreferences(this);
		
		prefs.edit().putBoolean("is_modbus_running", true).commit();
		
		// check and enable wifi
		if (!Utils.enableWifi(this, "TP-LINK_387C14","87669955")){
			prefs.edit().putBoolean("is_modbus_running", false).commit();
			return;
		}
		
        // show the icon in the status bar
        showNotification();
        
        try{
        	File file = new File(this.getFilesDir() + File.separator + "upload.xml");
            if (file.exists()) file.delete();
          
            FileOutputStream fOut = openFileOutput("upload.xml", MODE_WORLD_READABLE);
            ModbusSample.GenerateXml(project_id, password, fOut);
            
            prefs.edit().putBoolean("is_modbus_running", false).commit();
        } catch (Exception e){
        	
        }
        
        // Done with our work...  stop the service!
        this.stopSelf();
		
	}
	
	
	@Override
    public void onDestroy() {
        // Cancel the notification -- we use the same ID that we had used to start it
        mNM.cancel(R.string.alarm_service_notification_id);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.alarm_service_finished, Toast.LENGTH_SHORT).show();
    }
	
	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.alarm_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.alarm_service_notification_id, notification);
    }
	
	private final IBinder mBinder = new Binder() {
        @Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
		        int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
