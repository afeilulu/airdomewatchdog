package com.afeilulu.airdomewatchdog;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;

import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends SherlockFragmentActivity {
	
	private final static String TAG = "MainActivity";
	private ListView mListView;
	private TextView mEmptyView;
	private ArrayAdapter<String> mAdapter;
	private List<String> mList;
	
	public BroadcastReceiver mReceiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent){
			Bundle extras= intent.getExtras();
			
			if (mList.size() > 5000)
				mList.clear();
			
			mList.add(0,extras.getString("log"));
			mAdapter.notifyDataSetChanged();
			mListView.scrollTo(0, 0);
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(android.R.id.list);
        mEmptyView = (TextView) findViewById(android.R.id.empty);
        mList = new ArrayList<String>();
        
        mAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_expandable_list_item_1,mList);
        mListView.setAdapter(mAdapter);
        
        mListView.setEmptyView(mEmptyView);
        
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Toast.makeText(getApplicationContext(), mList.get(arg2), Toast.LENGTH_LONG).show();
				
			}
		});
    }
    
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new
				IntentFilter("com.afeilulu.airdomewatchdog.log");

		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy");
		super.onDestroy();
		mList.clear();
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		if (item.getItemId() == R.id.menu_settings){
			// Launch to our preferences screen.
			Intent intent = new Intent().setClass(this,PrefActivity.class);
			intent.setAction("com.afeilulu.airdomewatchdog.intent.action.preference");
			this.startActivity(intent);
		}
		
		if (item.getItemId() == R.id.menu_clear){
			mList.clear();
			mAdapter.notifyDataSetChanged();
		}
		
		return super.onOptionsItemSelected(item);
	}
    
}
