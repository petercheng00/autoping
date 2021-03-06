package com.pc.autoping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AutoPingService extends Service {
	
	private String TAG = "AutoPingService";
	
	private String currIPAddr;
	
	private Timer pingTimer;
    private long START_TIME = 10;
    private long RETRY_TIME = 5000;
    private int maxTimes = 0;
    private int numRuns = 0;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		currIPAddr = "";
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopPinging();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		String hostName = intent.getStringExtra("hostName");
		RETRY_TIME = intent.getLongExtra("interval", 5000);
		maxTimes = intent.getIntExtra("repeat", -1);
		try {
			currIPAddr = new NetTask().execute(hostName).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		numRuns = 0;
		startPinging();
		return START_NOT_STICKY;
	}
	
	private void startPinging()
	{
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new pingTask(), START_TIME, RETRY_TIME);
	}
	
	private void stopPinging()
	{
		pingTimer.cancel();
		currIPAddr = "";
	}
	
	private class pingTask extends TimerTask {
		public void run() {
			try {
				if ((numRuns >= maxTimes) && (maxTimes != -1))
				{
					pingTimer.cancel();
					return;
				}
		    	String pingCmd = "ping -c 1 " + currIPAddr;
		    	Runtime r = Runtime.getRuntime();
		    	Process p;
	
				p = r.exec(pingCmd);
		    	BufferedReader in = new BufferedReader(
		    			new InputStreamReader(p.getInputStream()));
		    	String pingResult = "";
		    	String currLine;
		    	while ((currLine = in.readLine()) != null) {
		    		pingResult += currLine;
		    	}
		    	int ind1 = pingResult.indexOf("time=");
		    	int ind2 = pingResult.indexOf("ms");
		    	int pingTime = -1;
		    	try
		    	{
		    		pingTime = (int) Double.parseDouble(pingResult.substring(ind1+5, ind2-1));
		    	}
		    	catch (NumberFormatException e)
		    	{
		    		//
		    	}
		    	in.close();
		    	Intent pingResponse = new Intent();
		    	pingResponse.setAction("com.pc.autoping.pingResponse");
		    	pingResponse.putExtra("pingTime", pingTime);
		    	sendBroadcast(pingResponse);
		    	++numRuns;
			} catch (IOException e) {
				Intent pingResponse = new Intent();
				pingResponse.setAction("com.pc.autoping.pingResponse");
				pingResponse.putExtra("pingTime", -1);
				sendBroadcast(pingResponse);
			}

		}
	}
}