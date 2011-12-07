package net.supudo.apps.aBombaJob.Offers;

import java.util.Timer;
import java.util.TimerTask;

import net.supudo.apps.aBombaJob.MainActivity;
import net.supudo.apps.aBombaJob.R;
import net.supudo.apps.aBombaJob.Database.DataHelper;
import net.supudo.apps.aBombaJob.Database.Models.JobOfferModel;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SendMessage extends MainActivity implements Runnable, SyncManagerCallbacks {

	private Integer offerID;
	private DataHelper dbHelper;
	private JobOfferModel jobOffer;
	private EditText txtMessage;
	private Button btnSend;
	private SyncManager syncManager;
	private ProgressDialog loadingDialog;
	private String messageText;
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	loadingDialog.dismiss();
        	
        	if (msg.getData().isEmpty()) {
        		Toast.makeText(getApplicationContext(), R.string.message_sent, Toast.LENGTH_SHORT).show();
        		goBack();
        	}
        	else {
        		AlertDialog.Builder alertbox = new AlertDialog.Builder(SendMessage.this);
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
		setContentView(R.layout.send_message);

		txtMessage = (EditText)findViewById(R.id.offer_message);
		btnSend = (Button)findViewById(R.id.btn_send);
		btnSend.setOnClickListener(btnSendListener);

		if (dbHelper == null)
			dbHelper = new DataHelper(this);

		if (syncManager == null)
			syncManager = new SyncManager(this, this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		Intent starting_intent = getIntent();
		Bundle extra = starting_intent.getExtras();
		if (extra != null) {
			offerID = extra.getInt("offerid");
			jobOffer = dbHelper.GetJobOffer(offerID);
			setTitle(jobOffer.Title);
		}
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

	@Override
	public void syncFinished() {
		handler.handleMessage(handler.obtainMessage());
	}

	@Override
	public void onSyncProgress(int progress) {
	}

	@Override
	public void onSyncError(Exception ex) {
		Log.d("SendMessage", ex.getMessage());
		Message msg = handler.obtainMessage(); 
        Bundle b = new Bundle();
        b.putString("exception", ex.getMessage()); 
        msg.setData(b); 
        handler.handleMessage(msg);
	}
	
	public void run() {
		syncManager.PostMessage(jobOffer.OfferID, messageText);
	}
	
	private OnClickListener btnSendListener = new OnClickListener() {
		public void onClick(View v) {
			messageText = txtMessage.getText().toString();
			loadingDialog = ProgressDialog.show(SendMessage.this, "", getString(R.string.sending), true);
	    	Thread thread = new Thread(SendMessage.this);
	        thread.run();
        }
    };
    
    private void goBack() {
    	Timer timer = new Timer();
        timer.schedule( new TimerTask(){
           public void run() { 
       			SendMessage.this.onBackPressed();
            }
         }, 2000);
    }
}
