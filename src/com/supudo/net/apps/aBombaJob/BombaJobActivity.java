package com.supudo.net.apps.aBombaJob;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.supudo.net.apps.aBombaJob.R;
import com.supudo.net.apps.aBombaJob.Database.DataHelper;
import com.supudo.net.apps.aBombaJob.Database.Models.SettingModel;
import com.supudo.net.apps.aBombaJob.Offers.NewestOffers;
import com.supudo.net.apps.aBombaJob.Synchronization.SyncManager;
import com.supudo.net.apps.aBombaJob.Synchronization.SyncManager.SyncManagerCallbacks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

public class BombaJobActivity extends MainActivity implements SyncManagerCallbacks {

	private ProgressBar syncProgress;
	public static boolean isSynchronized = false;
	private SyncManager syncManager;
	private DataHelper dbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		setTitle(R.string.app_name);		
		isSynchronized = false;
    }

    @Override
    public void onStart() {
    	super.onStart();

    	isSynchronized = checkLastSyncDate();
 
    	syncProgress = (ProgressBar)findViewById(R.id.sync_progress);
    	syncProgress.setIndeterminate(true);

		if (syncManager == null)
			syncManager = new SyncManager(this, this);

		if (isSynchronized)
			syncFinished();
		else
			syncManager.synchronize();
    }

	@Override
	protected void onPause() {
		if (syncManager != null) {
			syncManager.cancel();
			syncManager = null;
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (syncManager != null) {
			syncManager.cancel();
			syncManager = null;
		}
		super.onDestroy();
	}

	@Override
	public void syncFinished() {
		isSynchronized = true;
		LoadSettings();
		CommonSettings.lastSyncDate = Calendar.getInstance().getTime();
    	Intent myIntent = new Intent().setClass(BombaJobActivity.this, NewestOffers.class);
    	startActivityForResult(myIntent, 0);
	}

	@Override
	public void onSyncProgress(int progress) {
		syncProgress.setProgress(progress);
	}

	@Override
	public void onSyncError(Exception ex) {
		Log.d("Sync", "Error - " + ex.getMessage());
	}
	
	private void LoadSettings() {
		if (dbHelper == null)
			dbHelper = new DataHelper(this);
		
		ArrayList<SettingModel> listSettings = dbHelper.selectAllSettings();
		SettingModel _model = null;
		String sName = "";
		boolean sValue = false;
		for (int i=0; i<listSettings.size(); i++) {
			_model = (SettingModel)listSettings.get(i);
			if (_model.EditableYn) {
				sName = _model.SName;
				sValue = Boolean.valueOf(_model.SValue);
	
				if (sName.equalsIgnoreCase("StorePrivateData"))
					CommonSettings.stStorePrivateData = sValue;
				else if (sName.equalsIgnoreCase("SendGeo"))
					CommonSettings.stSendGeo = sValue;
				else if (sName.equalsIgnoreCase("InitSync"))
					CommonSettings.stInitSync = sValue;
				else if (sName.equalsIgnoreCase("OnlineSearch"))
					CommonSettings.stSearchOnline = sValue;
				else if (sName.equalsIgnoreCase("InAppEmail"))
					CommonSettings.stInAppEmail = sValue;
				else if (sName.equalsIgnoreCase("ShowCategories"))
					CommonSettings.stShowCategories = sValue;
			}
		}
	}
	
	private boolean checkLastSyncDate() {
		if (dbHelper == null)
			dbHelper = new DataHelper(this);

		SettingModel settLastSyncDate = dbHelper.GetSetting("lastSyncDate");
		SimpleDateFormat df = new SimpleDateFormat(CommonSettings.DefaultDateFormat);

		if (CommonSettings.lastSyncDate == null && (settLastSyncDate == null || settLastSyncDate.SValue.trim().equalsIgnoreCase(""))) {
			CommonSettings.lastSyncDate = Calendar.getInstance().getTime();
			dbHelper.SetSetting("lastSyncDate", df.format(CommonSettings.lastSyncDate));
			Log.d("Sync", "Scheduling synchronization now ...");
			return false;
		}
		else {
			DateFormat formatter = new SimpleDateFormat(CommonSettings.DefaultDateFormat);
			Date ldt = null;
			try {
				ldt = (Date)formatter.parse(settLastSyncDate.SValue);
			}
			catch (ParseException ex) {
				ldt = Calendar.getInstance().getTime();
				Log.e("BombaJobActivity", "lastSyncDate parse failed!");
			}
			long diffInSeconds = (Calendar.getInstance().getTime().getTime() - ldt.getTime()) / 1000;
			Log.d("Sync", "Skipping synchronization - last @ " + (diffInSeconds * 60));
			if (diffInSeconds >= 3600) {
				CommonSettings.lastSyncDate = Calendar.getInstance().getTime();
				dbHelper.SetSetting("lastSyncDate", df.format(CommonSettings.lastSyncDate));
				return false;
			}
			else {
				if (CommonSettings.lastSyncDate == null)
					CommonSettings.lastSyncDate = ldt;
				return true;
			}
		}
	}
}