package com.supudo.net.apps.aBombaJob.Offers;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.supudo.net.apps.aBombaJob.CommonSettings;
import com.supudo.net.apps.aBombaJob.R;
import com.supudo.net.apps.aBombaJob.TableActivity;
import com.supudo.net.apps.aBombaJob.DataAdapters.SearchAdapter;
import com.supudo.net.apps.aBombaJob.Database.DataHelper;
import com.supudo.net.apps.aBombaJob.Database.Models.JobOfferModel;

public class SearchResults extends TableActivity {
	
	private ArrayList<JobOfferModel> listItems;
	private AdView adView;
	private TextView txtEmpty;
	private DataHelper dbHelper;
	private String sKeyword = "";
	private int sFreelance = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newestoffers);
		setTitle(R.string.search_results);
		
		txtEmpty = (TextView)this.getListView().getEmptyView();
		txtEmpty.setText("");

		adView = new AdView(this, AdSize.BANNER, CommonSettings.GoogleAddsAppID);
		LinearLayout layout = (LinearLayout)findViewById(R.id.newestoffers_layout);
		layout.addView(adView);
		AdRequest request = new AdRequest();
		request.addTestDevice(AdRequest.TEST_EMULATOR);
		adView.loadAd(request);
		
		if (dbHelper == null)
			dbHelper = new DataHelper(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		Intent starting_intent = getIntent();
		Bundle extra = starting_intent.getExtras();
		if (extra != null) {
			sKeyword = extra.getString("sKeyword");
			sFreelance = extra.getInt("sFreelance");
			LoadOffers();
		}
		else {
	    	Intent myIntent = new Intent().setClass(SearchResults.this, Search.class);
	    	startActivityForResult(myIntent, 0);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		if (adView != null)
			adView.destroy();
		super.onDestroy();
	}
	
	private void reloadItems() {
		listItems = dbHelper.selectSearchedJobOffers(sKeyword, sFreelance);
	}
	
	private void LoadOffers() {
		reloadItems();
		
		if (listItems.size() == 0)
			txtEmpty.setText(getString(R.string.no_offers));
		else {
    		setListAdapter(new SearchAdapter(SearchResults.this, R.layout.list_item, listItems));

    		ListView lv = getListView();
    		lv.setTextFilterEnabled(true);

    		lv.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				Toast.makeText(getApplicationContext(), ((TextView)view.findViewById(R.id.title)).getText(), Toast.LENGTH_SHORT).show();
    				Intent intent = new Intent().setClass(SearchResults.this, OfferDetails.class);
    				intent.putExtra("offerid", (Integer)((TextView)view.findViewById(R.id.title)).getTag());
    				startActivity(intent);
    			}
    		});
		}
	}

}
