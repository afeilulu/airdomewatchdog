package com.afeilulu.airdomewatchdog.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    
	    String action = intent.getAction();

        if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            return;
        }

        boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false); 
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo aNetworkInfo = connectivityManager.getActiveNetworkInfo();                     

        if (!noConnectivity)
        {
            if ((aNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) ||
                (aNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI))
            {
                //Handle connected case
            }
        }
        else
        {
            if ((aNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) ||
                    (aNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI))
            {
                //Handle disconnected case
            }
        }
	}

}
