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
import net.supudo.apps.aBombaJob.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
	private ListView listView;

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

			listView = getListView();
			listView.setTextFilterEnabled(true);
    		registerForContextMenu(listView);

    		listView.setTextFilterEnabled(true);
    		listView.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				Integer oid = (Integer)((TextView)view.findViewById(R.id.title)).getTag();
    				String title = ((TextView)view.findViewById(R.id.title)).getText().toString(); 
    				ViewOffer(oid, title);
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
	
	private void reloadItems() {
		listItems = dbHelper.selectNewestJobOffers();
	}
	
	private void ViewOffer(Integer oid, String title) {
		Toast.makeText(getApplicationContext(), title, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent().setClass(NewestOffers.this, OfferDetails.class);
		intent.putExtra("offerid", oid);
		startActivity(intent);
	}
	
	private void SendMessage(Integer oid, String title) {
		Toast.makeText(getApplicationContext(), title, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent().setClass(NewestOffers.this, SendMessage.class);
		intent.putExtra("offerid", oid);
		startActivity(intent);
	}
	
	private void MarkAsRead(Integer oid) {
		dbHelper.setOfferReadYn(oid);
    	reloadItems();
    	setListAdapter(new NewestOffersAdapter(NewestOffers.this, R.layout.list_item, listItems));
	}
}
