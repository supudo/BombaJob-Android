package net.supudo.apps.aBombaJob.Offers;

import net.supudo.apps.aBombaJob.CommonSettings;
import net.supudo.apps.aBombaJob.MainActivity;
import net.supudo.apps.aBombaJob.Synchronization.SyncManager;
import net.supudo.apps.aBombaJob.Synchronization.SyncManager.SyncManagerCallbacks;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import net.supudo.apps.aBombaJob.R;

public class Search extends MainActivity implements Runnable, SyncManagerCallbacks {
	
	private EditText txtSearch;
	private Spinner cmbFreelance;
	private Button btnSearch;
	private SyncManager syncManager;
	private int sFreelance;
	private String sKeyword;
	private ProgressDialog loadingDialog;
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	loadingDialog.dismiss();
        	
        	if (msg.getData().isEmpty())
        		OpenResults();
        	else {
        		AlertDialog.Builder alertbox = new AlertDialog.Builder(Search.this);
        		alertbox.setMessage(msg.getData().getString("exception"));
        		alertbox.setNeutralButton(R.string.close_alertbox, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                alertbox.show();
        	}
        }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		setTitle(R.string.search);

		sFreelance = 0;
		cmbFreelance = (Spinner)findViewById(R.id.freelance);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.searchFreelance_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cmbFreelance.setAdapter(adapter);
		cmbFreelance.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				sFreelance = pos;
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		txtSearch = (EditText)findViewById(R.id.search_box);

		btnSearch = (Button)findViewById(R.id.btn_search);
		btnSearch.setOnClickListener(btnSearchListener);

		if (syncManager == null)
			syncManager = new SyncManager(this, this);
	}
	
	private OnClickListener btnSearchListener = new OnClickListener() {
		public void onClick(View v) {
			sKeyword = txtSearch.getText().toString();
        	
        	if (CommonSettings.stSearchOnline && CommonSettings.reloadSearch) {
    	     	loadingDialog = ProgressDialog.show(Search.this, "", getString(R.string.loading), true);
    	    	Thread thread = new Thread(Search.this);
    	        thread.run();
        	}
        	else
        		OpenResults();
        }
    };
	
	public void run() {
		syncManager.GetSearch(sKeyword, sFreelance);
	}

	@Override
	public void syncFinished() {
		CommonSettings.reloadSearch = false;
		OpenResults();
	}

	@Override
	public void onSyncProgress(int progress) {
	}

	@Override
	public void onSyncError(Exception ex) {
		Log.d("Search", ex.getMessage());
		Message msg = handler.obtainMessage(); 
        Bundle b = new Bundle();
        b.putString("exception", ex.getMessage()); 
        msg.setData(b); 
        handler.handleMessage(msg);
	}

	@Override
	protected void onPause() {
		if (syncManager != null) {
			syncManager.cancel();
			syncManager = null;
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (syncManager != null) {
			syncManager.cancel();
			syncManager = null;
		}
		super.onDestroy();
	}
	
	private void OpenResults() {
    	Intent myIntent = new Intent().setClass(Search.this, SearchResults.class);
    	myIntent.putExtra("sKeyword", sKeyword);
    	myIntent.putExtra("sFreelance", sFreelance);
    	startActivityForResult(myIntent, 0);
	}

}
