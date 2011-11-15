package com.supudo.net.apps.aBombaJob.URLHelpers;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class URLHelper implements AsyncURLConnectionCallbacks {

	private Integer serviceId;
	private URLHelperCallbacks mDelegate;
	private AsyncURLConnection conn = null;

	public URLHelper(URLHelperCallbacks delegate) {
		mDelegate = delegate;
	}
	
	public boolean isOnline(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public void cancel() {
		if (conn != null) {
			conn.cancel(true);
			conn = null;
		}
	}

	public void loadURLString(String url, Integer serviceId) throws MalformedURLException {
		if (conn != null) {
			conn.cancel(true);
			conn = null;
		}
		this.serviceId = serviceId;
		conn = new AsyncURLConnection(this);
		conn.execute(new URL(url));
	}

	public void loadURLString(String url, Integer serviceId, int timeout) throws MalformedURLException {
		if (conn != null) {
			conn.cancel(true);
			conn = null;
		}
		this.serviceId = serviceId;
		conn = new AsyncURLConnection(this, timeout);
		conn.execute(new URL(url));
	}

	@Override
	public void onSuccess(ByteArrayBuffer baf) {
		try {
			Log.d("URLHelper", EncodingUtils.getString(baf.toByteArray(), "utf8"));
			JSONObject obj = new JSONObject(EncodingUtils.getString(baf.toByteArray(), "utf8"));
			mDelegate.updateModelWithJSONObject(obj, this.serviceId);
		}
		catch (Exception e) {
			e.printStackTrace();
			mDelegate.connectionFailed(this.serviceId);
		}
	}

	@Override
	public void onFail() {
		mDelegate.connectionFailed(this.serviceId);
	}

}
