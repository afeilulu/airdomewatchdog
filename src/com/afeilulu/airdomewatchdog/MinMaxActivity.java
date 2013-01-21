package com.afeilulu.airdomewatchdog;

import java.util.HashMap;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MinMaxActivity extends Activity {
	
	private HashMap<String, Object> params = new HashMap<String, Object>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_min_max);
	}

	public void onAddButtonClick(View button) {
		Toast.makeText(this, "add", Toast.LENGTH_SHORT).show();
	}
	
	public void onDeleteButtonClick(View button) {
		Toast.makeText(this, "delete", Toast.LENGTH_SHORT).show();
	}

}
