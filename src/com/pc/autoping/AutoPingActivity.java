package com.pc.autoping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class AutoPingActivity extends Activity {
	
	private String currIPAddr;
	private boolean pinging;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_ping);
        currIPAddr = "";
        pinging = false;
        
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
    	try {
			String destIP = new NetTask().execute(destAddress).get();
			if (destIP != currIPAddr)
			{
				if (pinging)
				{
					//ask if want to cancel current pinging and start new one
				}
				else
				{
					currIPAddr = destIP;
					beginPinging(currIPAddr);
				}
			}
			else
			{
				addOutputText(destAddress + " is already being pinged!");
			}					
		} catch (Exception e1) {
			addOutputText("Could not resolve address!");
		}
    	
    }
    
    private void beginPinging(String addr) {
    	try {
	    	String pingCmd = "ping -c 1 " + addr;
	    	Runtime r = Runtime.getRuntime();
	    	Process p = r.exec(pingCmd);
	    	BufferedReader in = new BufferedReader(
	    			new InputStreamReader(p.getInputStream()));
	    	String pingResult = "";
	    	String currLine;
	    	while ((currLine = in.readLine()) != null) {
	    		pingResult += currLine;
	    	}
	    	int ind1 = pingResult.indexOf("time=");
	    	int ind2 = pingResult.indexOf("ms");
	    	addOutputText(pingResult.substring(ind1+5, ind2+2));
	    	in.close();
    	}
    	catch (IOException e) {
    		System.out.println(e);
    	}
    }
    
    private void addOutputText(String t) {
    	//TextView mOutputText = (TextView)findViewById(R.id.output_text);
    	//mOutputText.append(t + "\n");
    }
}
