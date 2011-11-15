package com.supudo.net.apps.aBombaJob.Offers;

import java.util.ArrayList;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.supudo.net.apps.aBombaJob.CommonSettings;
import com.supudo.net.apps.aBombaJob.R;
import com.supudo.net.apps.aBombaJob.TableActivity;
import com.supudo.net.apps.aBombaJob.Database.DataHelper;
import com.supudo.net.apps.aBombaJob.Database.Models.JobOfferModel;
import com.supudo.net.apps.aBombaJob.Synchronization.SyncManager;
import com.supudo.net.apps.aBombaJob.Synchronization.SyncManager.SyncManagerCallbacks;
import com.supudo.net.apps.aBombaJob.Offers.Adapters.SearchPeopleAdapter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchPeople extends TableActivity implements Runnable, SyncManagerCallbacks {
	
	private ArrayList<JobOfferModel> listItems;
	private AdView adView;
	private ProgressDialog loadingDialog;
	private SyncManager syncManager;
	private TextView txtEmpty;
	private DataHelper dbHelper;
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	loadingDialog.dismiss();
        	
        	if (msg.getData().isEmpty())
        		LoadOffers();
        	else
        		ProgressDialog.show(SearchPeople.this, "", msg.getData().getString("exception"), true);
        }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchpeople);
		setTitle(R.string.searchPeople);
		
		txtEmpty = (TextView)this.getListView().getEmptyView();
		txtEmpty.setText("");
		
		if (dbHelper == null)
			dbHelper = new DataHelper(this);
		reloadItems();

		if (adView != null) {
			adView = new AdView(this, AdSize.BANNER, CommonSettings.GoogleAddsAppID);
			LinearLayout layout = (LinearLayout)findViewById(R.id.searchpeople_layout);
			layout.addView(adView);
			AdRequest request = new AdRequest();
			request.addTestDevice(AdRequest.TEST_EMULATOR);
			adView.loadAd(request);
		}

		if (syncManager == null)
			syncManager = new SyncManager(this, this);

		if (CommonSettings.reloadSearchPeople) {
	     	loadingDialog = ProgressDialog.show(this, "", getString(R.string.loading), true);
	    	Thread thread = new Thread(this);
	        thread.start();
		}
		else
			LoadOffers();
	}
    
    @Override
    public void onStart() {
    	super.onStart();
    	reloadItems();
    	setListAdapter(new SearchPeopleAdapter(SearchPeople.this, R.layout.list_item, listItems)); 
    }

	@Override
	protected void onPause() {
		DestroySyncManager();
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		if (adView != null)
			adView.destroy();
		DestroySyncManager();
		super.onDestroy();
	}
	
	public void run() {
		syncManager.GetSearchPeople();
	}
	
	private void DestroySyncManager() {
		if (syncManager != null) {
			syncManager.cancel();
			syncManager = null;
		}
	}

	@Override
	public void syncFinished() {
		CommonSettings.reloadSearchPeople = false;
        handler.sendEmptyMessage(0);
	}

	@Override
	public void onSyncProgress(int progress) {
	}

	@Override
	public void onSyncError(Exception ex) {
		Log.d("SearchPeople", ex.getMessage());
		Message msg = handler.obtainMessage(); 
        Bundle b = new Bundle();
        b.putString("exception", ex.getMessage()); 
        msg.setData(b); 
        handler.handleMessage(msg);
	}
	
	private void reloadItems() {
		listItems = dbHelper.selectSearchPeople();
	}
	
	private void LoadOffers() {
		listItems = dbHelper.selectSearchPeople();
		
		if (listItems.size() == 0)
			txtEmpty.setText(getString(R.string.no_offers));
		else {
    		setListAdapter(new SearchPeopleAdapter(SearchPeople.this, R.layout.list_item, listItems));

    		ListView lv = getListView();
    		lv.setTextFilterEnabled(true);

    		lv.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				Toast.makeText(getApplicationContext(), ((TextView)view.findViewById(R.id.title)).getText(), Toast.LENGTH_SHORT).show();
    				Intent intent = new Intent().setClass(SearchPeople.this, OfferDetails.class);
    				intent.putExtra("offerid", (Integer)((TextView)view.findViewById(R.id.title)).getTag());
    				startActivity(intent);
    			}
    		});
		}
	}

}
