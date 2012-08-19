package com.pc.autoping;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.os.AsyncTask;

public class NetTask extends AsyncTask<String, Integer, String>
{
	@Override
	protected String doInBackground(String... params)
	{
		InetAddress addr = null;
		try
		{
			addr = InetAddress.getByName(params[0]);
			return addr.getHostAddress();
		}
		
		catch (UnknownHostException e)
		{
			return "";
		}		
	}

}
