package com.afeilulu.airdomewatchdog.Utils;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class Utils 
{
	private final static String TAG = "Utils";
	
	public static long getThreadId()
	{
		Thread t = Thread.currentThread();
		return t.getId();
	}
	public static String getThreadSignature()
	{
		Thread t = Thread.currentThread();
		long l = t.getId();
		String name = t.getName();
		long p = t.getPriority();
		String gname = t.getThreadGroup().getName();
		return (name + ":(id)" + l + ":(priority)" + p
				+ ":(group)" + gname);
	}
	public static void logThreadSignature(String tag)
	{
		Log.d(tag, getThreadSignature());
	}
	public static void sleepForInSecs(int secs)
	{
		try
		{
			Thread.sleep(secs * 1000);
		}
		catch(InterruptedException x)
		{
			throw new RuntimeException("interrupted",x);
		}
	}
	
	public static boolean enableWifi(Context context, String ssid,String password){
		
		boolean enabled = false;
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
//    	if (!wifi.isWifiEnabled()){
//			wifi.setWifiEnabled(true);
//    	}
		while(!wifi.setWifiEnabled(true))
			wifi.setWifiEnabled(true);
		
    	sleepForInSecs(10);
    	
//    	ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
//	    NetworkInfo activeNetInfo = null;
//	    while(activeNetInfo==null){
//	    	activeNetInfo= connectivityManager.getActiveNetworkInfo();
//	    	sleepForInSecs(5);
//	    	Log.d(TAG,"waiting for wifi enable ...");
//	    }
    	
    	/*	
    	WifiConfiguration wc = new WifiConfiguration();
//	    	wc.SSID = "\"SSIDName\"";
//	    	wc.preSharedKey  = "\"password\"";
    	wc.SSID = "\"" + ssid + "\"";
    	wc.preSharedKey  = "\"" + password + "\"";
    	wc.hiddenSSID = false;
    	wc.status = WifiConfiguration.Status.ENABLED;        
    	wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
    	wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
    	wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
    	wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
    	wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
    	wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
    	
    	boolean existed = false;
    	int networkId = -1;
    	List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
        for(WifiConfiguration config : configs) {
            if(config.SSID.equals(wc.SSID)) {
            	existed = true;
            	networkId = config.networkId;
            }
        }
    	
        if (!existed){
        	int res = wifi.addNetwork(wc);
        	Log.d("WifiPreference", "add Network returned " + res );
        
        	enabled = wifi.enableNetwork(res, true);        
        	Log.d("WifiPreference", "enableNetwork returned " + enabled );
        	
//        	wifi.saveConfiguration();
        } else {
        	enabled = wifi.enableNetwork(networkId, true);
        }*/
	    
	    return wifi.isWifiEnabled();
	}
	
	public static boolean disableWifi(Context context){
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		while(!wifi.setWifiEnabled(false))
			wifi.setWifiEnabled(false);
		
		sleepForInSecs(15);
		
		return !wifi.isWifiEnabled();
	}
	
	/**
     * time(format hh:mm) to time in milliseconds
     * @param formatted_time
     * @return
     */
    public static long getTimeInMillis(String formatted_time){
    	
    	long firstTime = System.currentTimeMillis();
    	int hour;
    	int minute;

    	if (formatted_time != null){
    		
    		String[] timeString = formatted_time.split(":");
        	if (timeString.length != 2)
        		return firstTime;
        	else{
        		hour = Integer.parseInt(timeString[0]);
        		minute = Integer.parseInt(timeString[1]);
        	}
        	
	    	Calendar cal = Calendar.getInstance();
	        cal.set(Calendar.HOUR_OF_DAY,hour);
	        cal.set(Calendar.MINUTE,minute);
	        cal.set(Calendar.SECOND,0);
	    	firstTime = cal.getTimeInMillis();
    	}
    	
    	return firstTime;
    }
	
    public static void switchFlyMode(Context context){
		boolean isEnabled = Settings.System.getInt(
		      context.getContentResolver(), 
		      Settings.System.AIRPLANE_MODE_ON, 0) == 1;

		// toggle airplane mode
		Settings.System.putInt(
		      context.getContentResolver(),
		      Settings.System.AIRPLANE_MODE_ON, isEnabled ? 0 : 1);

		// Post an intent to reload
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", !isEnabled);
		context.sendBroadcast(intent);
		
		Utils.sleepForInSecs(5);
	}
    
    public static void enableFlyMode(Context context){
		// enable airplane mode
		Settings.System.putInt(
		      context.getContentResolver(),
		      Settings.System.AIRPLANE_MODE_ON, 1);

		// Post an intent to reload
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", true);
		context.sendBroadcast(intent);
		
		Utils.sleepForInSecs(5);
	}
    
    public static void disableFlyMode(Context context){
		// enable airplane mode
		Settings.System.putInt(
		      context.getContentResolver(),
		      Settings.System.AIRPLANE_MODE_ON, 0);

		// Post an intent to reload
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", false);
		context.sendBroadcast(intent);
		
		Utils.sleepForInSecs(5);
	}
}
