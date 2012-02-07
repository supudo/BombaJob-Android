package net.supudo.apps.aBombaJob;

import net.supudo.apps.aBombaJob.Misc.About;
import net.supudo.apps.aBombaJob.Misc.BombaJobPreferences;
import net.supudo.apps.aBombaJob.Offers.JobOffers;
import net.supudo.apps.aBombaJob.Offers.NewestOffers;
import net.supudo.apps.aBombaJob.Offers.Search;
import net.supudo.apps.aBombaJob.Offers.SearchJobs;
import net.supudo.apps.aBombaJob.Offers.SearchPeople;
import net.supudo.apps.aBombaJob.Post.Post;

import net.supudo.apps.aBombaJob.R;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends Activity {
    
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
	        	break;
	        case R.id.jobs:
	        	if (CommonSettings.GetSetting(this, CommonSettings.AppSettings.stShowCategories))
	        		mIntent = new Intent().setClass(this, SearchJobs.class);
	        	else {
	        		mIntent = new Intent().setClass(this, JobOffers.class);
	        		mIntent.putExtra("humanyn", false);
	        	}
            	startActivityForResult(mIntent, 0);
	        	break;
	        case R.id.people:
	        	if (CommonSettings.GetSetting(this, CommonSettings.AppSettings.stShowCategories))
	        		mIntent = new Intent().setClass(this, SearchPeople.class);
	        	else {
	        		mIntent = new Intent().setClass(this, JobOffers.class);
	        		mIntent.putExtra("humanyn", true);
	        	}
            	startActivityForResult(mIntent, 0);
	        	break;
	        case R.id.search:
	        	mIntent = new Intent().setClass(this, Search.class);
            	startActivityForResult(mIntent, 0);
	        	break;
	        case R.id.post:
	        	mIntent = new Intent().setClass(this, Post.class);
            	startActivityForResult(mIntent, 0);
	        	break;
	        case R.id.settings:
	        	mIntent = new Intent().setClass(this, BombaJobPreferences.class);
            	startActivityForResult(mIntent, 0);
	        	break;
	        case R.id.syncagain:
	        	CommonSettings.lastSyncDate = null;
	        	mIntent = new Intent().setClass(this, BombaJobActivity.class);
	        	mIntent.putExtra("forceSync", true);
            	startActivityForResult(mIntent, 0);
	        	break;
	        case R.id.about:
	        	mIntent = new Intent().setClass(this, About.class);
            	startActivityForResult(mIntent, 0);
	        	break;
        }
        return true;
    }
}
