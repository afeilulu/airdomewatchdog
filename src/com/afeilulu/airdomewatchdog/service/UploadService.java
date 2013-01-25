package com.afeilulu.airdomewatchdog.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afeilulu.airdomewatchdog.MainActivity;
import com.afeilulu.airdomewatchdog.R;
import com.afeilulu.airdomewatchdog.Utils.Md5Digest;
import com.afeilulu.airdomewatchdog.Utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
	
	private boolean mFileParsed;
	private HashMap<String, Object> params = new HashMap<String, Object>();
	private int mTotal;
	
	private int mCount;
	private URL mPostUrl;
	private int TIMEOUT_VALUE = 10000;
	private int REPEAT_POST_MAX = 3;
	private String mUserAgent;
	private Intent broadcastIntent = new Intent("com.afeilulu.airdomewatchdog.log");
	
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
		String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
		sendLog("3G上传服务启动:" + currentDateTimeString);
		
		sendLog("关闭飞行模式");
		Utils.disableFlyMode(getApplicationContext());
		
		Log.d(TAG,broadcastIntent.getStringExtra("project_id"));
		Log.d(TAG,broadcastIntent.getStringExtra("password"));
		Log.d(TAG,broadcastIntent.getStringExtra("webservice_url"));
		Log.d(TAG,broadcastIntent.getStringExtra("interval"));
		Log.d(TAG,broadcastIntent.getStringExtra("upload_interval"));
		
		String project_id = broadcastIntent.getStringExtra("project_id");
		String password = broadcastIntent.getStringExtra("password");
		try {
			mPostUrl = new URL(broadcastIntent.getStringExtra("webservice_url"));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			sendLog("上传服务器地址不正确:" + broadcastIntent.getStringExtra("webservice_url"));
			return;
		}
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
        showNotification("读取Ftp文件完成。开始解析文件...");
        Map sensor = new HashMap();
        try {
        	sensor = GetMapFromFile("report.txt");
        	if (sensor == null || sensor.isEmpty())
        		mFileParsed = false;
        	else
        		mFileParsed = true;
		} catch (IOException e) {
			e.printStackTrace();
			mFileParsed = false;
		}
        
        if (mFileParsed){
        	// get json from map
            params.put("ID", project_id);

            Calendar rightNow = Calendar.getInstance(TimeZone.getDefault(),
           		 Locale.CHINA); 
            SimpleDateFormat dateformatter = new SimpleDateFormat("yyyyMMddHHmm"); 
            String originalText = project_id + password + dateformatter.format(rightNow.getTime());
           	sendLog("original text : \n" + originalText);
           	
           	String md5Password = Md5Digest.MD5String(originalText);
           	sendLog("md5 : \n" + md5Password);
            
            params.put("Password", md5Password);
            params.put("ReportTime", sensor.get("ReportTime"));
            sensor.remove("ReportTime");
            params.put("Report", sensor);
            params.put("Total", mTotal);
            
            String json = new Gson().toJson(params, Map.class);
            Log.d(TAG,"json = " + json);
            sendLog(json);
            
            showNotification("文件解析完成。关闭wifi...");
            if (Utils.disableWifi(this))
            	showNotification("切换为3G网络成功...");
            else {
            	showNotification("切换为3G网络失败!");
            	return;
            }
            
            mCount = 1;
            postToServer(json, mPostUrl);
            
//		        sendLog("空闲时间，保持wifi关闭3G");
//				Utils.enableWifi(this, null,null);
            sendLog("空闲时间，启动飞行模式");
			Utils.enableFlyMode(getApplicationContext());
			
        } else {
        	showNotification("文件解析失败！");
        }
		
	}
	
	
	@Override
    public void onDestroy() {
        // Cancel the notification -- we use the same ID that we had used to start it
        mNM.cancel(R.string.alarm_service_notification_id);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.upload_service_finished, Toast.LENGTH_SHORT).show();
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
	
	private void showNotification(String text) {
		sendLog(text);
		
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.alarm_notification_title),
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
    
    private void sendLog(String message){
    	broadcastIntent.putExtra("log", message);
    	sendBroadcast(broadcastIntent);
    }
    
    public Map GetMapFromFile(String filename) throws IOException{
	    Map<String, String> params = new HashMap<String, String>();
	    Log.d(TAG,"filepath = " + getFilesDir() + "/" + filename);
	    FileInputStream is = new FileInputStream(getFilesDir() + "/" + filename);
        char[] buffer = new char[1024];
        try {
        	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
            	if (line.length() > 1){
	            	line = line.substring(1, line.length()-1);
	                if (count == 0){
	                	params.put("ReportTime", line);
	                } else {
	                	String[] strs = line.split("=");
	                	if (strs != null && strs.length == 2)
	                		params.put(strs[0], strs[1]);
	                }
	                
	                count++;
            	}
            }
            mTotal = count -1;
            reader.close();
        } finally {
            is.close();
        }

    	return params;
    }
    
    private void postToServer(String json, URL url){
    	byte[] postJsonBytes = json.getBytes();

		try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIMEOUT_VALUE);
            urlConnection.setReadTimeout(TIMEOUT_VALUE);
            urlConnection.setRequestProperty("User-Agent", mUserAgent);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.setFixedLengthStreamingMode(postJsonBytes.length);

            Log.d(TAG, "Posting to URL: " + url);
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(postJsonBytes);
            out.flush();

            urlConnection.connect();
            throwErrors(urlConnection);
            String jsonBack = readInputStream(urlConnection.getInputStream());

            Log.d(TAG,"returned content = " + jsonBack);
            showNotification("文件上传完成。");
            sendLog("返回:" + jsonBack);
            
            out.close();
            urlConnection.disconnect();
            
            handleBackJson(jsonBack);
            
		} catch (SocketTimeoutException e) {
			sendLog("文件上传超时!尝试次数:" + mCount);
			mCount++;
			if (mCount <= REPEAT_POST_MAX){
				sendLog("重新上传:" + mCount + "/" + REPEAT_POST_MAX);
				postToServer(json,url);
			} else {
				sendLog("文件上传失败!");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			sendLog("文件上传失败!");
			sendLog(e.getMessage());
		}
    }
    
    private void handleBackJson(String json){
    	if (json != null && !TextUtils.isEmpty(json)){
        	Map<String,Object> back = new Gson().fromJson(json, Map.class);
        	int returnResult =  ((Double) back.get("return")).intValue();
        	if (returnResult > 0){
        		String project_id = back.get("ID").toString();
        		String newPassword = back.get("NewPass").toString();
        		int interval = ((Double) back.get("interval")).intValue();
        		String nexttime = back.get("nexttime").toString();
        		String verify_code =  back.get("Password").toString();
        		SharedPreferences prefs =
            			PreferenceManager.getDefaultSharedPreferences(this);
        		
        		boolean isChanged = false;
        		if (project_id != null 
        				&& !TextUtils.isEmpty(project_id)
        				&& !project_id.equalsIgnoreCase(prefs.getString("project_id", null))){
        			prefs.edit().putString("project_id", project_id).commit();
        		}
        		if (newPassword != null 
        				&& !TextUtils.isEmpty(newPassword)
        				&& !newPassword.equalsIgnoreCase(prefs.getString("password", null))){
        			prefs.edit().putString("password", newPassword).commit();
        		}
        		if (interval > 0 
        				&& !String.valueOf(interval).equalsIgnoreCase(prefs.getString("interval", null))){
        			prefs.edit().putString("interval", String.valueOf(interval)).commit();
        			isChanged = true;
        		}
        		if (nexttime != null
        				&& !TextUtils.isEmpty(nexttime)
        				&& !nexttime.equalsIgnoreCase(prefs.getString("next_time", null))){
        			
        			prefs.edit().putString("next_time", nexttime).commit();
        			isChanged = true;
        		}
        		if (isChanged){
        			// start broadcast
        			String action = "com.afeilulu.airdomewatchdog.intent.action.PREFERENCE_CHANGED";
        			Intent intent = new Intent(action);
        			this.sendBroadcast(intent);
        			
        			// new valve has been set
            		sendLog("已重新设定服务");
        		}
        		
        	}
        }
    }
    
    private static String readInputStream(InputStream inputStream)
            throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String responseLine;
        StringBuilder responseBuilder = new StringBuilder();
        while ((responseLine = bufferedReader.readLine()) != null) {
            responseBuilder.append(responseLine);
        }
        return responseBuilder.toString();
    }
	
	private void throwErrors(HttpURLConnection urlConnection) throws IOException {
        final int status = urlConnection.getResponseCode();
        sendLog("服务器返回status = " + status);
        if (status < 200 || status >= 300) {
            String errorMessage = null;
            try {
            	InputStream errorStream = urlConnection.getErrorStream();
            	if (errorStream != null){
	                String errorContent = readInputStream(errorStream);
	                Log.d(TAG, "Error content: " + errorContent);
	                sendLog("服务器返回错误:" + errorContent);
	//                ErrorResponse errorResponse = new Gson().fromJson(
	//                        errorContent, ErrorResponse.class);
	//                errorMessage = errorResponse.error.message;
	                errorMessage = errorContent;
            	} else
            		sendLog("服务器返回错误:null");
            } catch (IOException ignored) {
            	Log.d(TAG, "IOException : " + ignored.getMessage());
                sendLog("IOException:" + ignored.getMessage());
            } catch (JsonSyntaxException ignored) {
            	Log.d(TAG, "JsonSyntaxException : " + ignored.getMessage());
                sendLog("JsonSyntaxException:" + ignored.getMessage());
            }

            String exceptionMessage = "Error response "
                    + status + " "
                    + urlConnection.getResponseMessage()
                    + (errorMessage == null ? "" : (": " + errorMessage))
                    + " for " + urlConnection.getURL();

            // TODO: the API should return 401, and we shouldn't have to parse the message
//            throw (errorMessage != null && errorMessage.toLowerCase().contains("auth"))
//                    ? new HandlerException.UnauthorizedException(exceptionMessage)
//                    : new HandlerException(exceptionMessage);
            throw(new IOException(exceptionMessage));
        }
    }
}
