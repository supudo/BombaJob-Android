package net.supudo.apps.aBombaJob.Offers;

import net.supudo.apps.aBombaJob.R;
import net.supudo.apps.aBombaJob.Database.DataHelper;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class SearchJobs extends TabActivity {

	private DataHelper dbHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchjobs);
		setTitle(R.string.searchJobs);

		if (dbHelper == null)
			dbHelper = new DataHelper(this);

		Resources res = getResources();
		TabHost tabHost = getTabHost();

    	tabHost.clearAllTabs();
    	tabHost.setup();

		Intent intentCategories = new Intent(this, Categories.class);
		intentCategories.putExtra("humanyn", false);
		String titleCat = getString(R.string.headerCategories);
		TabSpec tabCategories = tabHost.newTabSpec("Categories").setIndicator(titleCat, res.getDrawable(R.drawable.tbcategories)).setContent(intentCategories);
		tabHost.addTab(tabCategories);

		Intent intentOffers = new Intent(this, JobOffers.class);
		intentOffers.putExtra("humanyn", false);
		String titleOffers = getString(R.string.headerOffers);
		TabSpec tabOffers = tabHost.newTabSpec("Offers").setIndicator(titleOffers, res.getDrawable(R.drawable.tboffers)).setContent(intentOffers);
		tabHost.addTab(tabOffers);

		tabHost.getTabWidget().getChildTabViewAt(0).setVisibility(View.VISIBLE);
		tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);
		tabHost.setCurrentTab(0);
		tabHost.setBackgroundColor(Color.TRANSPARENT);
		tabHost.getTabWidget().setBackgroundColor(Color.TRANSPARENT);
	}
    
    @Override
    public void onStart() {
    	super.onStart();
    	RefreshTitles();
    }

    public void RefreshTitles() {
		TabHost tabHost = getTabHost();

		String titleCat = getString(R.string.headerCategories) + " (" + dbHelper.getCategoryCount(false) + ")";
		((TextView)tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title)).setText(titleCat);

		String titleOffers = getString(R.string.headerOffers) + " (" + dbHelper.getJobOffersCount(false) + ")";
		((TextView)tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title)).setText(titleOffers);
    }
}
