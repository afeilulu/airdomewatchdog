package com.afeilulu.airdomewatchdog.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.afeilulu.airdomewatchdog.MainActivity;
import com.afeilulu.airdomewatchdog.MinMaxActivity;
import com.afeilulu.airdomewatchdog.R;
import com.afeilulu.airdomewatchdog.Utils.Utils;
import com.google.gson.Gson;

/*
 * Uses IntentService as the base class
 * to make this work on a separate thread.
 * 
 * need wifi to run this service
 */
public class FtpFile2JsonService 
extends ALongRunningNonStickyBroadcastService
{
	public static String tag = "FtpFile2JsonService";
	public NotificationManager mNM;
	private boolean mFtpFileDownloaded;
	private boolean mFileParsed;
	private HashMap<String, Object> params = new HashMap<String, Object>();
	private int mTotal;
	private String mUserAgent;
	private Intent broadcastIntent = new Intent("com.afeilulu.airdomewatchdog.log");
	private int TIMEOUT_VALUE = 10000;
	private int REPEAT_POST_MAX = 3;
	private int mCount;
	private URL mPostUrl;
	private boolean useFtp;
	
	//Required by IntentService
	public FtpFile2JsonService()
	{
		super("com.afeilulu.airdomewatchdog.service.FtpFile2JsonService");
	}

	/*
	 * Perform long running operations in this method.
	 * This is executed in a separate thread. 
	 */
	@Override
	protected void handleBroadcastIntent(Intent broadcastIntent) 
	{	
		SharedPreferences prefs =
    			PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		prefs.edit().putBoolean("is_modbus_running", true).commit(); // service start
		
		Log.d(tag,broadcastIntent.getStringExtra("project_id"));
		Log.d(tag,broadcastIntent.getStringExtra("password"));
		Log.d(tag,broadcastIntent.getStringExtra("webservice_url"));
		Log.d(tag,broadcastIntent.getStringExtra("interval"));
		
		String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
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
		
		
		useFtp = prefs.getBoolean("ftp", false);
    	if (useFtp) {
			sendLog("Ftp读取服务启动:" + currentDateTimeString);
			
//			sendLog("关闭飞行模式");
//			Utils.disableFlyMode(getApplicationContext());
			
			sendLog("打开wifi");
			
			// check and enable wifi
			if (!Utils.enableWifi(this, "TP-LINK_387C14","87669955")){
				prefs.edit().putBoolean("is_modbus_running", false).commit();
				return;
			}
			
	        showNotification("开始读取Ftp文件...");
	        
	        try{  
	        	String ftphostname = prefs.getString("ftp_url", null);
	        	String ftpusername = prefs.getString("ftp_username", null);
	        	String ftppassword = prefs.getString("ftp_password", null);
	        	sendLog("host:" + ftphostname);
	        	sendLog("username:" + ftpusername);
	        	
	        	if (ftphostname == null || ftphostname.length() < 1){
	        		showNotification("Ftp地址设置错误，请检查设置！");
	        	} else {
	        		if (ftpusername == null || ftpusername.length() < 1){
	        			ftpusername = "anonymous";
	        			ftppassword = "";
	        		} else if(ftppassword == null || ftppassword.length() < 1){
	        			showNotification("Ftp登录密码设置错误，请检查设置！");
	        		}
	        	}
	        	
	        	// anaylize ftphostname
	        	String srcFileSpec = "";
	        	String relativeUrl;
	        	if (ftphostname.startsWith("ftp://"))
	        		relativeUrl = ftphostname.substring(6);
	        	else
	        		relativeUrl = ftphostname;
	        	
	        	String[] strs = relativeUrl.split("/");
	        	if (strs == null || strs.length < 2)
	        		showNotification("Ftp地址设置错误，请检查设置！");
	        	else{
	        		ftphostname = strs[0];
	        		for (int i = 1; i < strs.length; i++) {
	        			if (srcFileSpec.length() > 1)
	        				srcFileSpec = srcFileSpec + "/" + strs[i];
	        			else
	        				srcFileSpec = strs[i];
					}
	        	}
	        	
	        	mFtpFileDownloaded = GetFileFTP(srcFileSpec,this.getFilesDir().toString(),"report.txt",
	        			ftphostname, ftpusername, ftppassword);
	        	
	        	if (mFtpFileDownloaded)
	    	        showNotification("读取Ftp文件完成");
	        } catch (Exception e){
	        	e.printStackTrace();
	        }    
    	} else {
    		sendLog("直接读取文件");
    		mFtpFileDownloaded = true; // read file directly
    	}
    	
    	File targetFile = new File(getFilesDir() + "/report.txt");
    	if (!targetFile.exists()){
    		sendLog("文件不存在!");
    		mFtpFileDownloaded = false;
    	}
    	
    	if (mFtpFileDownloaded){
	        // start to check valve value
	        if (prefs.getBoolean("valve_enable", false)){
	        	
	        	// get valve
	        	String jsonValve = prefs.getString(MinMaxActivity.TAG, null);
	        	if (jsonValve != null){
	        		ArrayList<String> list = (ArrayList<String>) new Gson().fromJson(jsonValve, List.class);
	        		
	        		Map sensor = new HashMap();
	    	        try {
	    	        	sensor = GetMapFromFile("report.txt");
	    	        	if (sensor == null || sensor.isEmpty())
	    	        		mFileParsed = false;
	    	        	else{
	    	        		mFileParsed = true;
	    	        		
	    	        		// check valve value
	    	        		int index = -1;
	    	        		for (int i = 0; i < list.size(); i++) {
	    	        			Map itemMap = new Gson().fromJson(list.get(i), Map.class);
	    	        			String sensorName = itemMap.get("name").toString();
								if (sensor.containsKey(sensorName)){
									Double currentValue = Double.valueOf(sensor.get(sensorName).toString().trim());
									if (currentValue < Double.valueOf(itemMap.get("min").toString())
										|| currentValue > Double.valueOf(itemMap.get("max").toString())){
											// we found exception happen here
											index = i;
											break;
										}
								}
							}
	    	        		
	    	        		if (index >= 0){
	    	        			prefs.edit().putBoolean("exception_happened", true).commit();
								// start broadcast
//				        			String action = "com.afeilulu.airdomewatchdog.intent.action.PREFERENCE_CHANGED";
//				        			Intent intent = new Intent(action);
//				        			this.sendBroadcast(intent);

	    	        			prefs.edit().putBoolean("is_modbus_running", false).commit();
	    	        			// no need to reset service
	    	        			// otherwise start upload directly
	    	        			Intent intent = new Intent();
	    	        			intent.putExtra("project_id",prefs.getString("project_id", null));
	    	        	    	intent.putExtra("password",prefs.getString("password", null));
	    	        	    	intent.putExtra("webservice_url",prefs.getString("webservice_url", null));
	    	        	    	// original intent is refered in handleBroadcastIntent
	    	        	        Intent uploadIntent = new Intent(this,UploadService.class);
	    	        	        uploadIntent.putExtra("original_intent", intent); 
	    	        			startService(uploadIntent);
	    	        		} else 
	    	        			prefs.edit().putBoolean("exception_happened", false).commit();
	    	        	}
	    			} catch (IOException e) {
	    				e.printStackTrace();
	    				mFileParsed = false;
	    			}
	    	        
	        	}
	        } 
    	}
    	
    	prefs.edit().putBoolean("is_modbus_running", false).commit();
    	
    	if (useFtp) {
        	// enable fly-mode
//        	sendLog("空闲时间，启动飞行模式");
//        	Utils.enableFlyMode(getApplicationContext());
    		sendLog("空闲时间，关闭wifi");
    		Utils.disableWifi(getApplicationContext());
        }
                
        // Done with our work...  stop the service!
        this.stopSelf();
		
	}
	
	
	@Override
    public void onDestroy() {
        // Cancel the notification -- we use the same ID that we had used to start it
//        mNM.cancel(R.string.alarm_service_notification_id);

        // Tell the user we stopped.
		if (useFtp)
			Toast.makeText(this, R.string.ftp_service_finished, Toast.LENGTH_SHORT).show();
        
//        stopMonitoringConnection();
    }
	
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
    
    public boolean GetFileFTP(String srcFileSpec, String destpath, String destname, 
    		String ftphostname, String ftpusername, String ftppassword) {
        File pathSpec = new File(destpath);
        FTPClient client = new FTPClient();
        BufferedOutputStream fos = null;
        try {
        	// start download
            client.connect(InetAddress.getByName(ftphostname));
            client.login(ftpusername, ftppassword);

            client.enterLocalPassiveMode(); // important!
            client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            fos = new BufferedOutputStream(
                                  new FileOutputStream(pathSpec.toString()+"/"+destname));
            client.retrieveFile(srcFileSpec, fos);
            }//try 
            catch (IOException e) {
                Log.e("FTP", "Error Getting File");
                e.printStackTrace();
                sendLog(e.getMessage());
                return false;
                }//catch
            finally {
                try {
                    if (fos != null) fos.close();
                    client.disconnect();
                    }//try
                    catch (IOException e) {
                        Log.e("FTP", "Disconnect Error");
                        e.printStackTrace();
                        }//catch
                    }//finally
        
        Log.v("FTP", "Done");
        return true;
    }//getfileFTP
    
	public Map GetMapFromFile(String filename) throws IOException{
		
		File targetFile = new File(getFilesDir() + "/" + filename);
    	if (!targetFile.exists()){
    		sendLog(filename  + " 文件不存在!");
    		return null;
    	}
		
	    Map<String, String> params = new HashMap<String, String>();
	    Log.d(tag,"filepath = " + getFilesDir() + "/" + filename);
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
	
	/*private static String readInputStream(InputStream inputStream)
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
	                Log.d(tag, "Error content: " + errorContent);
	                sendLog("服务器返回错误:" + errorContent);
	//                ErrorResponse errorResponse = new Gson().fromJson(
	//                        errorContent, ErrorResponse.class);
	//                errorMessage = errorResponse.error.message;
	                errorMessage = errorContent;
            	} else
            		sendLog("服务器返回错误:null");
            } catch (IOException ignored) {
            	Log.d(tag, "IOException : " + ignored.getMessage());
                sendLog("IOException:" + ignored.getMessage());
            } catch (JsonSyntaxException ignored) {
            	Log.d(tag, "JsonSyntaxException : " + ignored.getMessage());
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
    }*/
	
	/**
     * Build and return a user-agent string that can identify this application
     * to remote servers. Contains the package name and version code.
     */
    private static String buildUserAgent(Context context) {
        String versionName = "unknown";
        int versionCode = 0;

        try {
            final PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return context.getPackageName() + "/" + versionName + " (" + versionCode + ") (gzip)";
    }
    
    private void sendLog(String message){
    	broadcastIntent.putExtra("log", message);
    	sendBroadcast(broadcastIntent);
    }

    /*private void postToServer(String json, URL url){
    	byte[] postJsonBytes = json.getBytes();

		try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIMEOUT_VALUE);
            urlConnection.setReadTimeout(TIMEOUT_VALUE);
            urlConnection.setRequestProperty("User-Agent", mUserAgent);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.setFixedLengthStreamingMode(postJsonBytes.length);

            Log.d(tag, "Posting to URL: " + url);
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(postJsonBytes);
            out.flush();

            urlConnection.connect();
            throwErrors(urlConnection);
            String jsonBack = readInputStream(urlConnection.getInputStream());

            Log.d(tag,"returned content = " + jsonBack);
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
        		}
        		
        		// new valve has been set
        		sendLog("已重新设定服务");
        	}
        }
    }*/
    
}
