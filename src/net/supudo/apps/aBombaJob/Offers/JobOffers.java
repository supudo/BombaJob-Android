package net.supudo.apps.aBombaJob.Offers;

import java.util.ArrayList;

import net.supudo.apps.aBombaJob.CommonSettings;
import net.supudo.apps.aBombaJob.TableActivity;
import net.supudo.apps.aBombaJob.DataAdapters.JobOffersAdapter;
import net.supudo.apps.aBombaJob.Database.DataHelper;
import net.supudo.apps.aBombaJob.Database.Models.JobOfferModel;
import net.supudo.apps.aBombaJob.Synchronization.SyncManager;
import net.supudo.apps.aBombaJob.Synchronization.SyncManager.SyncManagerCallbacks;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.supudo.net.apps.aBombaJob.R;

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

public class JobOffers extends TableActivity implements Runnable, SyncManagerCallbacks {
	
	private ArrayList<JobOfferModel> listItems;
	private AdView adView;
	private ProgressDialog loadingDialog;
	private SyncManager syncManager;
	private TextView txtEmpty;
	private DataHelper dbHelper;
	private boolean humanYn;
	private int categoryID;
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	loadingDialog.dismiss();
        	
        	if (msg.getData().isEmpty())
        		LoadOffers();
        	else
        		ProgressDialog.show(JobOffers.this, "", msg.getData().getString("exception"), true);
        }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joboffers);
		setTitle(R.string.headerOffers);

		txtEmpty = (TextView)this.getListView().getEmptyView();
		txtEmpty.setText("");

		adView = new AdView(this, AdSize.BANNER, CommonSettings.GoogleAddsAppID);
		LinearLayout layout = (LinearLayout)findViewById(R.id.joboffers_layout);
		layout.addView(adView);
		AdRequest request = new AdRequest();
		request.addTestDevice(AdRequest.TEST_EMULATOR);
		adView.loadAd(request);
		
		if (dbHelper == null)
			dbHelper = new DataHelper(this);

		if (syncManager == null)
			syncManager = new SyncManager(this, this);
	}
    
    @Override
    public void onStart() {
    	super.onStart();

    	categoryID = 0;
		Intent starting_intent = getIntent();
		Bundle extra = starting_intent.getExtras();
		if (extra != null) {
			humanYn = extra.getBoolean("humanyn");
			categoryID = extra.getInt("cid"); 
		}

		if (humanYn)
			setTitle(R.string.searchPeople);
		else
			setTitle(R.string.searchJobs);

		if ((humanYn && CommonSettings.reloadSearchPeople) || (!humanYn && CommonSettings.reloadSearchJobs)) {
	     	loadingDialog = ProgressDialog.show(this, "", getString(R.string.loading), true);
	    	Thread thread = new Thread(this);
	    	///~ CRASH
	    	// on BACK button from offer details
	    	thread.run();
		}
		else
			LoadOffers();
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

	@Override
	public void syncFinished() {
		if (humanYn)
			CommonSettings.reloadSearchPeople = false;
		else
			CommonSettings.reloadSearchJobs = false;
        handler.sendEmptyMessage(0);
	}

	@Override
	public void onSyncProgress(int progress) {
	}

	@Override
	public void onSyncError(Exception ex) {
		if (humanYn)
			Log.d("SearchPeople", ex.getMessage());
		else
			Log.d("SearchJobs", ex.getMessage());
		Message msg = handler.obtainMessage(); 
        Bundle b = new Bundle();
        b.putString("exception", ex.getMessage()); 
        msg.setData(b); 
        handler.handleMessage(msg);
	}
	
	public void run() {
		if (humanYn)
			syncManager.GetSearchPeople();
		else
			syncManager.GetSearchJobs();
	}
	
	private void DestroySyncManager() {
		if (syncManager != null) {
			syncManager.cancel();
			syncManager = null;
		}
	}
	
	private void reloadItems() {
		if (listItems != null)
			listItems.clear();

		if (humanYn)
			listItems = dbHelper.selectSearchPeople(categoryID);
		else
			listItems = dbHelper.selectSearchJobs(categoryID);
	}
	
	private void LoadOffers() {
		reloadItems();
		
		if (listItems.size() == 0)
			txtEmpty.setText(getString(R.string.no_offers));
		else {
    		setListAdapter(new JobOffersAdapter(JobOffers.this, R.layout.list_item, listItems));

    		ListView lv = getListView();
    		lv.setTextFilterEnabled(true);

    		lv.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				Toast.makeText(getApplicationContext(), ((TextView)view.findViewById(R.id.title)).getText(), Toast.LENGTH_SHORT).show();
    				Intent intent = new Intent().setClass(JobOffers.this, OfferDetails.class);
    				intent.putExtra("offerid", (Integer)((TextView)view.findViewById(R.id.title)).getTag());
    				startActivity(intent);
    			}
    		});
		}
	}
}
