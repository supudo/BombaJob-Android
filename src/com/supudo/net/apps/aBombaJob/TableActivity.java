package com.supudo.net.apps.aBombaJob;

import com.supudo.net.apps.aBombaJob.Misc.About;
import com.supudo.net.apps.aBombaJob.Misc.Settings;
import com.supudo.net.apps.aBombaJob.Offers.JobOffers;
import com.supudo.net.apps.aBombaJob.Offers.NewestOffers;
import com.supudo.net.apps.aBombaJob.Offers.Search;
import com.supudo.net.apps.aBombaJob.Offers.SearchJobs;
import com.supudo.net.apps.aBombaJob.Offers.SearchPeople;
import com.supudo.net.apps.aBombaJob.Post.Post;

import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
//import android.widget.Toast;

public class TableActivity extends ListActivity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	Intent mIntent;
        switch (item.getItemId()) {
	        case R.id.newestoffers:
	        	mIntent = new Intent().setClass(this, NewestOffers.class);
            	startActivityForResult(mIntent, 0);
	        	//Toast.makeText(this, R.string.title_NewestOffers, Toast.LENGTH_LONG).show();
	        	break;
	        case R.id.jobs:
	        	if (CommonSettings.stShowCategories)
	        		mIntent = new Intent().setClass(this, SearchJobs.class);
	        	else {
	        		mIntent = new Intent().setClass(this, JobOffers.class);
	        		mIntent.putExtra("humanyn", false);
	        	}
            	startActivityForResult(mIntent, 0);
            	//Toast.makeText(this, R.string.title_Jobs, Toast.LENGTH_LONG).show();
	        	break;
	        case R.id.people:
	        	if (CommonSettings.stShowCategories)
	        		mIntent = new Intent().setClass(this, SearchPeople.class);
	        	else {
	        		mIntent = new Intent().setClass(this, JobOffers.class);
	        		mIntent.putExtra("humanyn", true);
	        	}
            	startActivityForResult(mIntent, 0);
            	//Toast.makeText(this, R.string.title_People, Toast.LENGTH_LONG).show();
	        	break;
	        case R.id.search:
	        	mIntent = new Intent().setClass(this, Search.class);
            	startActivityForResult(mIntent, 0);
            	//Toast.makeText(this, R.string.title_Search, Toast.LENGTH_LONG).show();
	        	break;
	        case R.id.post:
	        	mIntent = new Intent().setClass(this, Post.class);
            	startActivityForResult(mIntent, 0);
            	//Toast.makeText(this, R.string.title_PostOffer, Toast.LENGTH_LONG).show();
	        	break;
	        case R.id.settings:
	        	mIntent = new Intent().setClass(this, Settings.class);
            	startActivityForResult(mIntent, 0);
            	//Toast.makeText(this, R.string.title_Settings, Toast.LENGTH_LONG).show();
	        	break;
	        case R.id.syncagain:
	        	CommonSettings.lastSyncDate = null;
	        	mIntent = new Intent().setClass(this, BombaJobActivity.class);
            	startActivityForResult(mIntent, 0);
	        	break;
	        case R.id.about:
	        	mIntent = new Intent().setClass(this, About.class);
            	startActivityForResult(mIntent, 0);
            	//Toast.makeText(this, R.string.title_AboutBombaJob, Toast.LENGTH_LONG).show();
	        	break;
        }
        return true;
	}
}
