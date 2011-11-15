package com.supudo.net.apps.aBombaJob.Misc;

import java.util.ArrayList;

import com.supudo.net.apps.aBombaJob.R;
import com.supudo.net.apps.aBombaJob.TableActivity;
import com.supudo.net.apps.aBombaJob.Database.DataHelper;
import com.supudo.net.apps.aBombaJob.Database.Models.SettingModel;
import com.supudo.net.apps.aBombaJob.Offers.Adapters.SettingsAdapter;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class Settings extends TableActivity {

	private ArrayList<SettingModel> listSettings;
	private DataHelper dbHelper;
	private TextView txtEmpty;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		setTitle(R.string.settings);
		
		txtEmpty = (TextView)this.getListView().getEmptyView();
		txtEmpty.setText("");

		if (dbHelper == null)
			dbHelper = new DataHelper(this);

		listSettings = dbHelper.selectAllSettings();

		if (listSettings.size() == 0)
			txtEmpty.setText(getString(R.string.no_settings));
		else {
    		setListAdapter(new SettingsAdapter(Settings.this, R.layout.list_item, listSettings));

    		ListView lv = getListView();
    		lv.setTextFilterEnabled(true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
