package com.supudo.net.apps.aBombaJob.Offers;

import android.os.Bundle;

import com.supudo.net.apps.aBombaJob.MainActivity;
import com.supudo.net.apps.aBombaJob.R;

public class SearchResults extends MainActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchresults);
		setTitle(R.string.search_results);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
