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
import com.supudo.net.apps.aBombaJob.DataAdapters.CategoriesAdapter;
import com.supudo.net.apps.aBombaJob.Database.DataHelper;
import com.supudo.net.apps.aBombaJob.Database.Models.CategoryModel;

public class Categories extends TableActivity {
	
	private ArrayList<CategoryModel> listItems;
	private AdView adView;
	private TextView txtEmpty;
	private DataHelper dbHelper;
	private boolean humanYn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.categories);
		setTitle(R.string.headerCategories);

		txtEmpty = (TextView)this.getListView().getEmptyView();
		txtEmpty.setText("");

		adView = new AdView(this, AdSize.BANNER, CommonSettings.GoogleAddsAppID);
		LinearLayout layout = (LinearLayout)findViewById(R.id.categories_layout);
		layout.addView(adView);
		AdRequest request = new AdRequest();
		request.addTestDevice(AdRequest.TEST_EMULATOR);
		adView.loadAd(request);
		
		if (dbHelper == null)
			dbHelper = new DataHelper(this);
	}
    
    @Override
    public void onStart() {
    	super.onStart();

		Intent starting_intent = getIntent();
		Bundle extra = starting_intent.getExtras();
		if (extra != null)
			humanYn = extra.getBoolean("humanyn");

		if (humanYn)
			setTitle(R.string.searchPeople);
		else
			setTitle(R.string.searchJobs);

		LoadCategories();
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
		listItems = dbHelper.selectCategories(humanYn);
	}
	
	private void LoadCategories() {
		reloadItems();

		if (listItems.size() == 0)
			txtEmpty.setText(getString(R.string.no_offers));
		else {
			setListAdapter(new CategoriesAdapter(Categories.this, R.layout.list_item, listItems));

			ListView lv = getListView();
    		lv.setTextFilterEnabled(true);

			lv.setTextFilterEnabled(true);
			lv.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				Toast.makeText(getApplicationContext(), ((TextView)view.findViewById(R.id.title)).getText(), Toast.LENGTH_SHORT).show();
    				Intent intent = new Intent().setClass(Categories.this, JobOffers.class);
    				intent.putExtra("humanyn", humanYn);
    				intent.putExtra("cid", (Integer)((TextView)view.findViewById(R.id.title)).getTag());
    				startActivity(intent);
    			}
    		});
		}
	}

}
