package com.supudo.net.apps.aBombaJob;

import java.util.ArrayList;

import com.supudo.net.apps.aBombaJob.R;
import com.supudo.net.apps.aBombaJob.Database.DataHelper;
import com.supudo.net.apps.aBombaJob.Database.Models.SettingModel;
import com.supudo.net.apps.aBombaJob.Offers.NewestOffers;
import com.supudo.net.apps.aBombaJob.Synchronization.SyncManager;
import com.supudo.net.apps.aBombaJob.Synchronization.SyncManager.SyncManagerCallbacks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    }

    @Override
    public void onStart() {
    	super.onStart();

    	syncProgress = (ProgressBar)findViewById(R.id.sync_progress);
    	syncProgress.setMax(400);
    	syncProgress.setProgress(0);

		if (syncManager == null)
			syncManager = new SyncManager(this, this);
		
		isSynchronized = false;
		if (isSynchronized) {
			syncProgress.setVisibility(View.INVISIBLE);
			syncFinished();
		}
		else {
			syncProgress.setVisibility(View.VISIBLE);
			syncManager.synchronize();
		}
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
		syncProgress.setVisibility(View.INVISIBLE);
		isSynchronized = true;
		LoadSettings();
    	Intent myIntent = new Intent().setClass(BombaJobActivity.this, NewestOffers.class);
    	startActivityForResult(myIntent, 0);
		//Intent dbChanged = new Intent(BankomatiMapActivity.MapActivityAction);
		//sendBroadcast(dbChanged);
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
		String sName = "";
		boolean sValue = false;
		for (int i=0; i<listSettings.size(); i++) {
			sName = ((SettingModel)listSettings.get(i)).SName;
			sValue = Boolean.valueOf(((SettingModel)listSettings.get(i)).SValue);

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