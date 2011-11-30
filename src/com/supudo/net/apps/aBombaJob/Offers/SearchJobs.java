package com.supudo.net.apps.aBombaJob.Offers;

import com.supudo.net.apps.aBombaJob.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class SearchJobs extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchjobs);
		setTitle(R.string.searchJobs);

		Resources res = getResources();
		TabHost tabHost = getTabHost();

    	tabHost.clearAllTabs();
    	tabHost.setup();

		Intent intentCategories = new Intent(this, Categories.class);
		intentCategories.putExtra("humanyn", false);
		TabSpec tabCategories = tabHost.newTabSpec("Categories").setIndicator("Categories", res.getDrawable(R.drawable.tbcategories)).setContent(intentCategories);
		tabHost.addTab(tabCategories);

		Intent intentOffers = new Intent(this, JobOffers.class);
		intentOffers.putExtra("humanyn", false);
		TabSpec tabOffers = tabHost.newTabSpec("Offers").setIndicator("Offers", res.getDrawable(R.drawable.tboffers)).setContent(intentOffers);
		tabHost.addTab(tabOffers);

		tabHost.getTabWidget().getChildTabViewAt(0).setVisibility(View.VISIBLE);
		tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);
		tabHost.setCurrentTab(0);
		tabHost.setBackgroundColor(Color.TRANSPARENT);
		tabHost.getTabWidget().setBackgroundColor(Color.TRANSPARENT);
	}
}
