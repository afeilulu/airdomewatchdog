package com.afeilulu.airdomewatchdog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

public class MinMaxActivity extends Activity {
	
	public static final String TAG = "MinMaxActivity";
	private ListView mListView;
	private ArrayAdapter<String> mAdapter;
	private List<String> mList;
	private HashMap<String, Object> params;
	private EditText sensorName;
	private EditText minValue;
	private EditText maxValue;
	
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_min_max);
		prefs =	PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		
		sensorName = (EditText) findViewById(R.id.sensor_name);
		minValue = (EditText) findViewById(R.id.min_value);
		maxValue = (EditText) findViewById(R.id.max_value);
		mListView = (ListView) findViewById(android.R.id.list);
		
		mList = getListFromPref();
        mAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_list_item_1,mList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				// show value into edittext
				params = (HashMap<String, Object>) new Gson().fromJson(mList.get(arg2), Map.class);
				sensorName.setText(params.get("name").toString());
				minValue.setText(params.get("min").toString());
				maxValue.setText(params.get("max").toString());
			}
		});
	}

	/**
	 * save edit text and show them in list view
	 * @param button
	 */
	public void onAddButtonClick(View button) {
		if (validateEditText()){
		
			int index = -1;
			for (int i = 0; i < mList.size(); i++) {
				if (mList.get(i).contains(sensorName.getText())){
					index = i;
					break;
				}
			}
			
			if (index >= 0){
				// already existed
				Log.d(TAG, mList.get(index));
				params = (HashMap<String, Object>) new Gson().fromJson(mList.get(index), Map.class);
				mList.remove(index);
			} else {
				// add a new one
				params = new HashMap<String, Object>();
			}	

			params.put("name", sensorName.getText().toString());
			params.put("min", minValue.getText().toString());
			params.put("max", maxValue.getText().toString());
			
			mList.add(new Gson().toJson(params, Map.class));
			
			mAdapter.notifyDataSetChanged();
			
			saveListToPres(mList);
		} else {
			Toast.makeText(this, "invalid value", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * delete current item and refresh list view
	 * @param button
	 */
	public void onDeleteButtonClick(View button) {
		
		int index = -1;
		if (!TextUtils.isEmpty(sensorName.getText())){
			for (int i = 0; i < mList.size(); i++) {
				if (mList.get(i).contains(sensorName.getText())){
					index = i;
					break;
				}
			}
		}
		
		if (index >= 0){
			mList.remove(index);
			mAdapter.notifyDataSetChanged();
			
			saveListToPres(mList);
		}
	}

	private List<String> getListFromPref(){
		List<String> list;
		String jsonString = prefs.getString(TAG, null);
		
		if (jsonString == null)
			return new ArrayList<String>();
		else
			list = new Gson().fromJson(jsonString, List.class);
		
		return list;
	}
	
	private void saveListToPres(List<String> list){
		prefs.edit().putString(TAG,new Gson().toJson(list, List.class)).commit();
	}
	
	private boolean validateEditText(){
		if (TextUtils.isEmpty(sensorName.getText().toString())) return false;
		if (TextUtils.isEmpty(minValue.getText().toString())) return false;
		if (TextUtils.isEmpty(maxValue.getText().toString())) return false;
		
		try{
			Double min = Double.valueOf(minValue.getText().toString());
			Double max = Double.valueOf(maxValue.getText().toString());
			if (min >= max) return false;
		} catch (NumberFormatException e){
			return false;
		}
		
		return true;
	}
}
