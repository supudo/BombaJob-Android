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
import net.supudo.apps.aBombaJob.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
        	else {
        		AlertDialog.Builder alertbox = new AlertDialog.Builder(JobOffers.this);
        		alertbox.setMessage(msg.getData().getString("exception"));
        		alertbox.setNeutralButton(R.string.close_alertbox, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                alertbox.show();
        	}
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.offers_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		JobOfferModel off = (JobOfferModel)listItems.get((int)info.id);
		switch (item.getItemId()) {
			case R.id.view:
				ViewOffer(off.OfferID, off.Title);
				return true;
			case R.id.sendmessage:
				SendMessage(off.OfferID, off.Title);
				return true;
			case R.id.markread:
				MarkAsRead(off.OfferID);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
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

		try {
			if (humanYn) {
				SearchPeople parentActivity;
				parentActivity = (SearchPeople)this.getParent();
				parentActivity.RefreshTitles();
			}
			else {
				SearchJobs parentActivity;
				parentActivity = (SearchJobs)this.getParent();
				parentActivity.RefreshTitles();
			}
		}
		catch (Exception e) { }

		if (listItems.size() == 0)
			txtEmpty.setText(getString(R.string.no_offers));
		else {
    		setListAdapter(new JobOffersAdapter(JobOffers.this, R.layout.list_item, listItems));

    		ListView lv = getListView();
    		lv.setTextFilterEnabled(true);
    		registerForContextMenu(lv);

    		lv.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				Integer oid = (Integer)((TextView)view.findViewById(R.id.title)).getTag();
    				String title = ((TextView)view.findViewById(R.id.title)).getText().toString(); 
    				ViewOffer(oid, title);
    			}
    		});
		}
	}
	
	private void ViewOffer(Integer oid, String title) {
		Toast.makeText(getApplicationContext(), title, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent().setClass(JobOffers.this, OfferDetails.class);
		intent.putExtra("offerid", oid);
		startActivity(intent);
	}
	
	private void SendMessage(Integer oid, String title) {
		Toast.makeText(getApplicationContext(), title, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent().setClass(JobOffers.this, SendMessage.class);
		intent.putExtra("offerid", oid);
		startActivity(intent);
	}
	
	private void MarkAsRead(Integer oid) {
		dbHelper.setOfferReadYn(oid);
    	reloadItems();
    	setListAdapter(new JobOffersAdapter(JobOffers.this, R.layout.list_item, listItems));
	}
}
