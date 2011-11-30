package net.supudo.apps.aBombaJob.Offers;

import java.util.ArrayList;

import net.supudo.apps.aBombaJob.CommonSettings;
import net.supudo.apps.aBombaJob.TableActivity;
import net.supudo.apps.aBombaJob.DataAdapters.NewestOffersAdapter;
import net.supudo.apps.aBombaJob.Database.DataHelper;
import net.supudo.apps.aBombaJob.Database.Models.JobOfferModel;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.supudo.net.apps.aBombaJob.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewestOffers extends TableActivity {
	
	private ArrayList<JobOfferModel> listItems;
	private AdView adView;
	private TextView txtEmpty;
	private DataHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newestoffers);
		setTitle(R.string.newestOffers);

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

		reloadItems();

		if (listItems.size() == 0)
			txtEmpty.setText(getString(R.string.no_offers));
		else {
			setListAdapter(new NewestOffersAdapter(NewestOffers.this, R.layout.list_item, listItems));

			ListView lv = getListView();
    		lv.setTextFilterEnabled(true);

			lv.setTextFilterEnabled(true);
			lv.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				Toast.makeText(getApplicationContext(), ((TextView)view.findViewById(R.id.title)).getText(), Toast.LENGTH_SHORT).show();
    				Intent intent = new Intent().setClass(NewestOffers.this, OfferDetails.class);
    				intent.putExtra("offerid", (Integer)((TextView)view.findViewById(R.id.title)).getTag());
    				startActivity(intent);
    			}
    		});
		}
	}
    
    @Override
    public void onStart() {
    	super.onStart();
    	reloadItems();
    	setListAdapter(new NewestOffersAdapter(NewestOffers.this, R.layout.list_item, listItems));
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
		listItems = dbHelper.selectNewestJobOffers();
	}
}
