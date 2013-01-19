package com.afeilulu.airdomewatchdog.moudle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.OpenableColumns;

/**
 * upload xml
 * @author chen
 *
 */
public class Delivery {
	
	public void upload(){
		
	}
	
	/**
	 * download command
	 * authenticate verify_code
	 * if verify_code is ok, run command from remote
	 */
	public void download(){
		
	}

	/**
	 * post xml file to some service
	 * @throws Exception
	 */
	public static String executeMultipartPost(String url,InputStream is) throws Exception
	{
		String result = null;
		BufferedReader in = null;
		if (!url.startsWith("http://"))
			url = "http://" + url.trim();
		
		try {
//			InputStream is =  this.getAssets().open("data.xml");
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(url);
			byte[] data = IOUtils.toByteArray(is);
			InputStreamBody isb = new InputStreamBody(new ByteArrayInputStream(data), "uploadedFile");
//			StringBody sb1 = new StringBody("some text goes here");
//			StringBody sb2 = new StringBody("some text goes here too");
			MultipartEntity multipartContent = new MultipartEntity();
			multipartContent.addPart("data", isb);
//			multipartContent.addPart("one", sb1);
//			multipartContent.addPart("two", sb2);
			postRequest.setEntity(multipartContent);
			HttpResponse response =httpClient.execute(postRequest);
			InputStream responseStream = response.getEntity().getContent();
			
			in = new BufferedReader(new InputStreamReader(responseStream));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while((line = in.readLine()) != null){
				sb.append(line + NL);
			}
			in.close();
//			responseStream.close();
			
			result = sb.toString();
		} catch (Throwable e)
		{
			e.printStackTrace();
		} finally {
			if (in != null){
				try{
					in.close();
				} catch (IOException ioe){
					ioe.printStackTrace();
				}
			}
		}
		
		return result;
	}

}
