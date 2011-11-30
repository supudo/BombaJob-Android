package com.supudo.net.apps.aBombaJob.Offers;

import com.supudo.net.apps.aBombaJob.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost.TabSpec;
import android.widget.TabHost;

public class SearchPeople extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchpeople);
		setTitle(R.string.searchPeople);

		Resources res = getResources();
		TabHost tabHost = getTabHost();

    	tabHost.clearAllTabs();
    	tabHost.setup();

		Intent intentCategories = new Intent(this, Categories.class);
		intentCategories.putExtra("humanyn", true);
		TabSpec tabCategories = tabHost.newTabSpec("Categories").setIndicator("Categories", res.getDrawable(R.drawable.tbcategories)).setContent(intentCategories);
		tabHost.addTab(tabCategories);

		Intent intentOffers = new Intent(this, JobOffers.class);
		intentOffers.putExtra("humanyn", true);
		TabSpec tabOffers = tabHost.newTabSpec("Offers").setIndicator("Offers", res.getDrawable(R.drawable.tboffers)).setContent(intentOffers);
		tabHost.addTab(tabOffers);

		tabHost.getTabWidget().getChildTabViewAt(0).setVisibility(View.VISIBLE);
		tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);
		tabHost.setCurrentTab(0);
		tabHost.setBackgroundColor(Color.TRANSPARENT);
		tabHost.getTabWidget().setBackgroundColor(Color.TRANSPARENT);
	}
}
