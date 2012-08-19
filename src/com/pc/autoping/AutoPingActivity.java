package com.pc.autoping;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class AutoPingActivity extends Activity {
	private AutoPingReceiver APReceiver;
	boolean serviceRunning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_ping);
        
        APReceiver = new AutoPingReceiver();
        registerReceiver(APReceiver, new IntentFilter("com.pc.autoping.pingResponse"));
    	//TextView mOutputText = (TextView)findViewById(R.id.output_text);
    	//mOutputText.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_auto_ping, menu);
        return true;
    }
    
    public void startPinging(View view) {
    	EditText mDestAddress = (EditText)findViewById(R.id.dest_address);
    	InputMethodManager imm = (InputMethodManager)getSystemService(
    			Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(mDestAddress.getWindowToken(),0);
    	String destAddress = mDestAddress.getText().toString();
    	
    	Intent pingIntent = new Intent(this, AutoPingService.class);
    	pingIntent.putExtra("hostName", destAddress);
    	
    	if (!serviceRunning) {
    		startService(pingIntent);
    	}
        serviceRunning = true;
    }
    
    public void stopPinging(View view) {
    	stopService(new Intent(this, AutoPingService.class));
    	serviceRunning = false;
    }
    
    private void addOutputText(String t) {
    	//TextView mOutputText = (TextView)findViewById(R.id.output_text);
    	//mOutputText.append(t + "\n");
    }
    
    private class AutoPingReceiver extends BroadcastReceiver
    {
    	@Override
    	public void onReceive(Context context, Intent intent)
    	{
    		if (intent.getAction().equals("com.pc.autoping.pingResponse"))
    		{
    			double pingTime = intent.getDoubleExtra("pingTime", -1);
    			TextView mOutputText = (TextView)findViewById(R.id.average_ping);
    			mOutputText.setText(Double.toString(pingTime));
    		}
    	}
    }
    
}
