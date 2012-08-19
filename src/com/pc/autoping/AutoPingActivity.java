package com.pc.autoping;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AutoPingActivity extends Activity {
	private AutoPingReceiver APReceiver;
	boolean serviceRunning = false;
	ToggleButton pingButton;
	Spinner repeatSpinner;
	Spinner intervalSpinner;
	
	ArrayList<Integer> pastFivePings;
	ArrayList<Integer> pastPings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_ping);
        
        pastFivePings = new ArrayList<Integer>();
        pastPings = new ArrayList<Integer>();        
        for(int i = 0; i < 100; ++i)
        {
        	if (i < 5) {
        		pastFivePings.add(-1);
        	}
	        pastPings.add(0);
        }
        
        repeatSpinner = (Spinner) findViewById(R.id.repeat_spinner);
        intervalSpinner = (Spinner) findViewById(R.id.interval_spinner);
        ArrayAdapter<CharSequence> repeatAdapter = CustomArrayAdapter.createFromResource(this, R.array.repeat_array, R.layout.spinner_text);
        ArrayAdapter<CharSequence> intervalAdapter = CustomArrayAdapter.createFromResource(this, R.array.interval_array, R.layout.spinner_text);
        repeatSpinner.setAdapter(repeatAdapter);
        intervalSpinner.setAdapter(intervalAdapter);
        repeatSpinner.setSelection(3);
        intervalSpinner.setSelection(1);
        
        
        pingButton = (ToggleButton) findViewById(R.id.ping_button);
        pingButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if (pingButton.isChecked()) {
        			startPinging(v);
        		}
        		else{
        			stopPinging(v);
        		}
        	}
        });
        
        APReceiver = new AutoPingReceiver();
        registerReceiver(APReceiver, new IntentFilter("com.pc.autoping.pingResponse"));
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_auto_ping, menu);
        return true;
    }
    
    public void startPinging(View view) {
    	Intent pingIntent = new Intent(this, AutoPingService.class);
    	
    	EditText mDestAddress = (EditText)findViewById(R.id.dest_address);
    	InputMethodManager imm = (InputMethodManager)getSystemService(
    			Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(mDestAddress.getWindowToken(),0);
    	String destAddress = mDestAddress.getText().toString();
    	
    	pingIntent.putExtra("hostName", destAddress);
    	
    	int repeatIndex = repeatSpinner.getSelectedItemPosition();
    	switch(repeatIndex) {
    	case 0: pingIntent.putExtra("repeat", 5); break;
    	case 1: pingIntent.putExtra("repeat", 10); break;
    	case 2: pingIntent.putExtra("repeat", 50); break;
    	case 3: pingIntent.putExtra("repeat", -1); break;
    	}
    	
    	int intervalIndex = intervalSpinner.getSelectedItemPosition();
    	switch(intervalIndex) {
    	case 0: pingIntent.putExtra("interval", (long)1000); break;
    	case 1: pingIntent.putExtra("interval", (long)5000); break;
    	case 2: pingIntent.putExtra("interval", (long)10000); break;
    	case 3: pingIntent.putExtra("interval", (long)30000); break;
    	case 4: pingIntent.putExtra("interval", (long)60000); break;
    	case 5: pingIntent.putExtra("interval", (long)300000); break;
    	case 6: pingIntent.putExtra("interval", (long)600000); break;
    	}
    	
    	if (!serviceRunning) {
    		startService(pingIntent);
    	}
        serviceRunning = true;
    }
    
    public void stopPinging(View view) {
    	stopService(new Intent(this, AutoPingService.class));
    	serviceRunning = false;
    }
    
    private void updateDisplays(int newPing)
    {
    	if (newPing != -1)
    	{
	    	pastFivePings.remove(0);
	    	pastFivePings.add(newPing);
	    	pastPings.remove(0);
	    	pastPings.add(newPing);
	    	int totalPing = 0;
	    	int totalValid = 0;
	    	String historyText = "";
	    	for (int i = 0; i < pastFivePings.size(); ++i)
	    	{
	    		if (pastFivePings.get(i) != -1)
	    		{
	    			totalPing += pastFivePings.get(i);
	    			++totalValid;
	    			historyText += (Double.toString(pastFivePings.get(i)) + "   "); 
	    		}
	    	}
	    	int pingAverage = totalPing / totalValid;
	    	TextView mAvgText = (TextView)findViewById(R.id.average_ping);
	    	if (pingAverage < 50)
	    	{
	    		mAvgText.setTextColor(Color.GREEN);
	    	}
	    	else if (pingAverage < 150)
	    	{
	    		mAvgText.setTextColor(Color.YELLOW);
	    	}
	    	else
	    	{
	    		mAvgText.setTextColor(Color.RED);
	    	}
	    	mAvgText.setText(Double.toString(pingAverage));
	    	TextView mHistText = (TextView)findViewById(R.id.ping_history);
	    	mHistText.setText(historyText);
    	}
    	else
    	{
			TextView mAvgText = (TextView)findViewById(R.id.average_ping);
			mAvgText.setText("error");
    	}
    }
    
    
    private class AutoPingReceiver extends BroadcastReceiver
    {
    	@Override
    	public void onReceive(Context context, Intent intent)
    	{
    		if (intent.getAction().equals("com.pc.autoping.pingResponse"))
    		{
    			int pingTime = intent.getIntExtra("pingTime", -1);
    			updateDisplays(pingTime);
    		}
    	}
    }
    
    static class CustomArrayAdapter<T> extends ArrayAdapter<T>
    {
    	public CustomArrayAdapter(Context ctx, T [] objects)
    	{
    		super(ctx, android.R.layout.simple_spinner_item, objects);
    	}
    	
    	
    	@Override
    	public View getDropDownView(int position, View convertView, ViewGroup parent)
    	{
    		View view = super.getView(position, convertView, parent);
    		
    		TextView text = (TextView)view.findViewById(android.R.id.text1);
    		text.setTextColor(Color.WHITE);
    		text.setTextSize(20);
    		text.setBackgroundColor(Color.BLACK);
    		return view;
    	}
    }

    
}

