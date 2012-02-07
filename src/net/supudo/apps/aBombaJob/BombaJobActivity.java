package net.supudo.apps.aBombaJob;

import java.util.Calendar;
import java.util.Date;

import net.supudo.apps.aBombaJob.Offers.NewestOffers;
import net.supudo.apps.aBombaJob.Synchronization.SyncManager;
import net.supudo.apps.aBombaJob.Synchronization.SyncManager.SyncManagerCallbacks;

import net.supudo.apps.aBombaJob.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;

public class BombaJobActivity extends MainActivity implements SyncManagerCallbacks {

	private ProgressBar syncProgress;
	public static boolean isSynchronized = false;
	private SyncManager syncManager;
	private boolean forceSync;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		setTitle(R.string.app_name);
		isSynchronized = false;
		forceSync = false;
    }

    @Override
    public void onStart() {
    	super.onStart();
		LoadSettings();

    	Intent starting_intent = getIntent();
		Bundle extra = starting_intent.getExtras();
		if (extra != null)
			forceSync = extra.getBoolean("forceSync");

    	isSynchronized = shouldSkipSync();
    	forceSync = false;
 
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
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		CommonSettings.stStorePrivateData = sharedPrefs.getBoolean("StorePrivateData", false);
		CommonSettings.stSendGeo = sharedPrefs.getBoolean("SendGeo", false);
		CommonSettings.stInitSync = sharedPrefs.getBoolean("InitSync", false);
		CommonSettings.stSearchOnline = sharedPrefs.getBoolean("InitSync", false);
		CommonSettings.stInAppEmail = sharedPrefs.getBoolean("InAppEmail", false);
		CommonSettings.stShowCategories = sharedPrefs.getBoolean("ShowCategories", false);
		Log.d("Preferences", "lastSyncDate = " + sharedPrefs.getLong("lastSyncDate", 0));
		CommonSettings.lastSyncDate = new Date(sharedPrefs.getLong("lastSyncDate", 0));
		CommonSettings.stPrivateData_Email = sharedPrefs.getString("PrivateData_Email", "");
	}
	
	private boolean shouldSkipSync() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (forceSync || CommonSettings.lastSyncDate == null) {
			CommonSettings.lastSyncDate = Calendar.getInstance().getTime();
			sharedPrefs.edit().putLong("lastSyncDate", CommonSettings.lastSyncDate.getTime()).commit();
			Log.d("Sync", "Scheduling synchronization now ...");
			return false;
		}
		else {
			long diffInSeconds = (Calendar.getInstance().getTime().getTime() - CommonSettings.lastSyncDate.getTime()) / 1000;
			Log.d("Sync", "Skipping synchronization - last @ " + (diffInSeconds * 60));
			if (diffInSeconds >= 3600) {
				CommonSettings.lastSyncDate = Calendar.getInstance().getTime();
				sharedPrefs.edit().putLong("lastSyncDate", CommonSettings.lastSyncDate.getTime()).commit();
				return false;
			}
			else
				return true;
		}
	}
}