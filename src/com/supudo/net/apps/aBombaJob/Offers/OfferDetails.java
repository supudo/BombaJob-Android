package com.supudo.net.apps.aBombaJob.Offers;

import com.supudo.net.apps.aBombaJob.MainActivity;
import com.supudo.net.apps.aBombaJob.R;
import com.supudo.net.apps.aBombaJob.Database.DataHelper;

import android.content.Intent;
import android.os.Bundle;

public class OfferDetails extends MainActivity {
	
	private Integer offerID;
	private DataHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offerdetails);
		
		if (dbHelper == null)
			dbHelper = new DataHelper(this);
		
		Intent starting_intent = getIntent();
		Bundle extra = starting_intent.getExtras();
		if (extra != null) {
			offerID = extra.getInt("offerid");
			dbHelper.setOfferReadYn(offerID);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
