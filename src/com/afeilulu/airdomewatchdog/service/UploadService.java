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
import android.content.Intent;
import android.content.SharedPreferences;
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
 * need 3G to run this service
 */
public class UploadService 
extends ALongRunningNonStickyBroadcastService
{
	public static String TAG = "UploadService";
	public NotificationManager mNM;
	
	//Required by IntentService
	public UploadService()
	{
		super("com.afeilulu.airdomewatchdog.service.UploadService");
	}

	/*
	 * Perform long running operations in this method.
	 * This is executed in a separate thread. 
	 */
	@Override
	protected void handleBroadcastIntent(Intent broadcastIntent) 
	{
		String message = 
			broadcastIntent.getStringExtra("project_id");
		Log.d(TAG,message);
		Log.d(TAG,broadcastIntent.getStringExtra("verify_code"));
		Log.d(TAG,broadcastIntent.getStringExtra("webservice_url"));
		Log.d(TAG,broadcastIntent.getStringExtra("period_modbus"));
		
		String url = broadcastIntent.getStringExtra("webservice_url");
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
        // show the icon in the status bar
        showNotification();

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
//        Thread thr = new Thread(null, mTask, "Test15SecBCRService");
//        thr.start();
        
        
        // Normally we would do some work here...  for our sample, we will
        // just sleep for 30 seconds.
        /*long endTime = System.currentTimeMillis() + 15*1000;
        while (System.currentTimeMillis() < endTime) {
            synchronized (mBinder) {
                try {
                    mBinder.wait(endTime - System.currentTimeMillis());
                    Log.d(tag,"after wait");
                } catch (Exception e) {
                }
            }
        }*/
        
        try{
        	SharedPreferences prefs =
        			PreferenceManager.getDefaultSharedPreferences(this);
        	
        	boolean is_modbus_running = prefs.getBoolean("is_modbus_running", true);
        	while (is_modbus_running) {
				Utils.sleepForInSecs(5);
				Log.d(TAG, "modbus scan is running");
				is_modbus_running = prefs.getBoolean("is_modbus_running", true);
			}
        	
        	// in order to upload file through 3G, we need to disable wifi 
            if (Utils.disableWifi(this)){
            	
	        	String response = null;
	        	InputStream is = openFileInput("upload.xml");
	        	if (is != null && url != null)
	        		response = Delivery.executeMultipartPost(url,is);
	        	
	        	is.close();
	        	
	        	if (response != null)
	        		Log.d(TAG,response);
	        	
	        	// read preference
	        	// TODO
	        	
	        	// save preference into sharedpreference
	        	// TODO
	        	
	        	// re-send broadcast
	        	// this.sendBroadcast(intent);
            }
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
	
	/**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
        public void run() {
            // Normally we would do some work here...  for our sample, we will
            // just sleep for 30 seconds.
            long endTime = System.currentTimeMillis() + 15*1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }

            // Done with our work...  stop the service!
            UploadService.this.stopSelf();
        }
    };
	
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
