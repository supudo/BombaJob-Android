package net.supudo.apps.aBombaJob.Offers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.supudo.apps.aBombaJob.CommonSettings;
import net.supudo.apps.aBombaJob.Database.DataHelper;
import net.supudo.apps.aBombaJob.Database.Models.JobOfferModel;
import net.supudo.apps.aBombaJob.SocNet.Facebook.BaseDialogListener;
import net.supudo.apps.aBombaJob.SocNet.Facebook.BaseRequestListener;
import net.supudo.apps.aBombaJob.SocNet.Facebook.SessionEvents;
import net.supudo.apps.aBombaJob.SocNet.Facebook.SessionStore;
import net.supudo.apps.aBombaJob.SocNet.Facebook.SessionEvents.AuthListener;
import net.supudo.apps.aBombaJob.SocNet.Twitter.TwitterApp;
import net.supudo.apps.aBombaJob.Synchronization.SyncManager;
import net.supudo.apps.aBombaJob.Synchronization.SyncManager.SyncManagerCallbacks;
import net.supudo.apps.aBombaJob.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OfferDetails extends Activity implements Runnable, SyncManagerCallbacks {

	private Integer offerID;
	private DataHelper dbHelper;
	private JobOfferModel jobOffer;
	private SyncManager syncManager;
	private ProgressDialog loadingDialog;

    private Button fbPostButton, twitterPostButton, emailPostButton;
	private TextView txtTitle, txtCategory, txtDate, txtPositivismTitle, txtNegativismTitle;
	private TextView txtPositivism, txtNegativism, txtFreelanceTitle, txtFreelance;

    private Facebook engineFacebook;
    private AsyncFacebookRunner fbAsyncRunner;
    
	private Twitter engineTwitter;
	private RequestToken twitterRequestToken = null;
	private Context twitterContext;
    
    private String offerDate, fromEmail, toEmail;
    
    private enum SocNetOp {
    	SocNetOpEmail,
    	SocNetOpTwitter,
    	SocNetOpFacebook
    }
    private SocNetOp selectedOp;
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	loadingDialog.dismiss();
        	
        	if (msg.getData().isEmpty()) {
        		Toast.makeText(getApplicationContext(), R.string.message_sent, Toast.LENGTH_SHORT).show();
        		goBack();
        	}
        	else {
        		AlertDialog.Builder alertbox = new AlertDialog.Builder(OfferDetails.this);
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
		setContentView(R.layout.offerdetails);
		
		if (dbHelper == null)
			dbHelper = new DataHelper(this);

		if (syncManager == null)
			syncManager = new SyncManager(this, this);

		fbPostButton = (Button)findViewById(R.id.facebook_post);
		twitterPostButton = (Button)findViewById(R.id.twitter_post);
		emailPostButton = (Button)findViewById(R.id.email_post);

		txtTitle = (TextView)findViewById(R.id.title);
		txtCategory = (TextView)findViewById(R.id.category);
		txtDate = (TextView)findViewById(R.id.date);
		txtPositivism = (TextView)findViewById(R.id.positivism);
		txtNegativism = (TextView)findViewById(R.id.negativism);
		txtPositivismTitle = (TextView)findViewById(R.id.positivism_title);
		txtNegativismTitle = (TextView)findViewById(R.id.negativism_title);
		txtFreelanceTitle = (TextView)findViewById(R.id.freelance_title);
		txtFreelance = (TextView)findViewById(R.id.freelance);

		engineFacebook = new Facebook(CommonSettings.FacebookAppID);
		fbAsyncRunner = new AsyncFacebookRunner(engineFacebook);
        SessionStore.restore(engineFacebook, this);
        SessionEvents.addAuthListener(new FBAuthListener());

        emailPostButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	SendEmail();
            }
        });

        twitterPostButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	PostToTwitter();
            }
        });

        fbPostButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	PostOnFacebook();
            }
        });
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		Intent starting_intent = getIntent();
		Bundle extra = starting_intent.getExtras();
		if (extra != null) {
			offerID = extra.getInt("offerid");
			dbHelper.setOfferReadYn(offerID);
			jobOffer = dbHelper.GetJobOffer(offerID);
			setTitle(jobOffer.Title);
			txtTitle.setText(jobOffer.Title);
			txtCategory.setText(jobOffer.CategoryTitle);
			if (jobOffer.HumanYn) {
				txtPositivismTitle.setText(R.string.odetails_Human_Positiv);
				txtNegativismTitle.setText(R.string.odetails_Human_Negativ);
			}
			else {
				txtPositivismTitle.setText(R.string.odetails_Company_Positiv);
				txtNegativismTitle.setText(R.string.odetails_Company_Negativ);
			}
			txtPositivism.setText(jobOffer.Positivism);
			txtNegativism.setText(jobOffer.Negativism);

			txtFreelanceTitle.setText(getString(R.string.odetails_Freelance));
			if (jobOffer.FreelanceYn)
				txtFreelance.setText(getString(R.string.yes));
			else
				txtFreelance.setText(getString(R.string.no));
			
			String pDate = "";
			try {
				DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				Date date = (Date)formatter.parse(jobOffer.PublishDate);
				int month = date.getMonth() + 1;

				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				pDate += cal.get(Calendar.DAY_OF_MONTH);

				String monthLabel = "monthsLong_" + month;
				pDate += " " + (String) this.getResources().getText(this.getResources().getIdentifier(monthLabel, "string", "net.supudo.apps.aBombaJob"));
				
				pDate += " " + cal.get(Calendar.YEAR);
			}
			catch (ParseException e) {
				Log.d("OfferDetails", "Date parser failed - " + jobOffer.PublishDate);
			}
			offerDate = pDate;
			txtDate.setText(pDate);
		}
		else {
			Intent intent = new Intent().setClass(OfferDetails.this, NewestOffers.class);
			startActivity(intent);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (this.selectedOp == SocNetOp.SocNetOpFacebook)
    		engineFacebook.authorizeCallback(requestCode, resultCode, data);
    	else if (this.selectedOp == SocNetOp.SocNetOpTwitter) {
    		if (resultCode == RESULT_OK) {
    			AccessToken accessToken = null;
    			try {
    				accessToken = engineTwitter.getOAuthAccessToken(twitterRequestToken, data.getExtras().getString(CommonSettings.IEXTRA_OAUTH_VERIFIER));

    				SharedPreferences pref = getSharedPreferences(CommonSettings.PREFERENCE_NAME, MODE_PRIVATE);
    				SharedPreferences.Editor editor = pref.edit();
    				editor.putString(CommonSettings.PREF_KEY_TOKEN, accessToken.getToken());
    				editor.putString(CommonSettings.PREF_KEY_SECRET, accessToken.getTokenSecret());
    				editor.putBoolean(CommonSettings.PREF_KEY_CONNECTED, true);
    				editor.commit();

    				String tweet = "BombaJob.bg - " + jobOffer.Title;
    				tweet += " http://bombajob.bg/offer/" + jobOffer.OfferID;
    				tweet += " #bombajobbg";

    				engineTwitter.setOAuthAccessToken(accessToken);
    				engineTwitter.updateStatus(tweet);
    				Toast.makeText(OfferDetails.this, R.string.twitter_publishok, Toast.LENGTH_LONG).show();
    			}
    			catch (TwitterException e) {
    				if (e.getMessage().toString().contains("duplicate"))
    					Toast.makeText(OfferDetails.this, R.string.twitter_err_duplicate, Toast.LENGTH_LONG).show();
    				e.printStackTrace();
    			}
    		}
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.offer_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent starting_intent = getIntent();
		Bundle extra = starting_intent.getExtras();
		int offerID = 0;
		if (extra != null) {
			offerID = extra.getInt("offerid");
	    	Intent mIntent;
	        switch (item.getItemId()) {
		        case R.id.sendmessage:
		        	mIntent = new Intent().setClass(this, SendMessage.class);
		    		mIntent.putExtra("offerid", offerID);
		    		startActivity(mIntent);
		        	break;
		        case R.id.share_facebook:
	            	this.PostOnFacebook();
		        	break;
		        case R.id.share_twitter:
	            	this.PostToTwitter();
		        	break;
		        case R.id.share_email:
	            	this.SendEmail();
		        	break;
		        case R.id.goback:
	            	this.goBack();
		        	break;

	        }
		}
        return true;
    }

	/* ------------------------------------------
	 * 
	 * Web-services
	 * 
	 * ------------------------------------------
	 */
	@Override
	public void syncFinished() {
		handler.handleMessage(handler.obtainMessage());
	}

	@Override
	public void onSyncProgress(int progress) {
	}

	@Override
	public void onSyncError(Exception ex) {
		Log.d("OfferDetails", ex.getMessage());
		Message msg = handler.obtainMessage(); 
        Bundle b = new Bundle();
        b.putString("exception", ex.getMessage()); 
        msg.setData(b); 
        handler.handleMessage(msg);
	}
    
    private void goBack() {
    	Timer timer = new Timer();
        timer.schedule( new TimerTask(){
           public void run() { 
       			OfferDetails.this.onBackPressed();
            }
         }, 2000);
    }
	
	public void run() {
		syncManager.SendEmail(jobOffer.OfferID, fromEmail, toEmail);
	}

	/* ------------------------------------------
	 * 
	 * Email
	 * 
	 * ------------------------------------------
	 */
    public void SendEmail() {
    	this.selectedOp = SocNetOp.SocNetOpEmail;
    	if (CommonSettings.stInAppEmail) {
	    	try {
		        String emailBody = "";
		        emailBody += jobOffer.CategoryTitle + "<br /><br />";
		        emailBody += "<b>" + jobOffer.Title + "</b><br /><br />";
		        emailBody += "<i>" + offerDate + "</i><br /><br />";
		        emailBody += getString(R.string.odetails_Freelance) + " " + getString(((jobOffer.FreelanceYn) ? R.string.yes : R.string.no)) + "<br /><br />";
		        if (jobOffer.HumanYn) {
		            emailBody += "<b>" + getString(R.string.odetails_Human_Positiv) + "</b> " + jobOffer.Positivism + "<br /><br />";
		            emailBody += "<b>" + getString(R.string.odetails_Human_Negativ) + "</b> " + jobOffer.Negativism + "<br /><br />";
		        }
		        else {
		            emailBody += "<b>" + getString(R.string.odetails_Company_Positiv) + "</b> " + jobOffer.Positivism + "<br /><br />";
		            emailBody += "<b>" + getString(R.string.odetails_Company_Negativ) + "</b> " + jobOffer.Negativism + "<br /><br />";
		        }
		        emailBody += "<br /><br /> Sent from BombaJob ...";
		        
		        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
		        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
		        emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(emailBody));
		        startActivity(emailIntent);
	    	}
	    	catch (ActivityNotFoundException noEmailEx) {
	    		Toast.makeText(OfferDetails.this, R.string.email_noclient, Toast.LENGTH_LONG).show();
	    	}
	    	catch (Exception ex) {
	    		Toast.makeText(OfferDetails.this, R.string.generic_error, Toast.LENGTH_LONG).show();
	    	}
    	}
    	else {
    		LayoutInflater factory = LayoutInflater.from(this);
    		final View textEntryView = factory.inflate(R.layout.alert_send_message, null);
    		AlertDialog d = new AlertDialog.Builder(OfferDetails.this)
            	.setTitle(R.string.send_message)
            	.setView(textEntryView)
            	.setPositiveButton(R.string.message_btn_send, new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int whichButton) {
            			fromEmail = ((EditText)textEntryView.findViewById(R.id.fromEmail_edit)).getText().toString();
            			toEmail = ((EditText)textEntryView.findViewById(R.id.toEmail_edit)).getText().toString();
            			loadingDialog = ProgressDialog.show(OfferDetails.this, "", getString(R.string.sending), true);
            	    	Thread thread = new Thread(OfferDetails.this);
            	        thread.run();
            		}
            	})
            	.create();
    		d.show();
    	}
    }

	/* ------------------------------------------
	 * 
	 * Twitter
	 * 
	 * ------------------------------------------
	 */
	public void PostToTwitter() {
    	this.selectedOp = SocNetOp.SocNetOpTwitter;
		PostToTwitterApp();
	}

	private void PostToTwitterApp() {
		try {
			twitterContext = this;
			new TwitterConnectTask().execute();
		}
		catch (Exception e) {
			Log.e("OfferDetails", e.getMessage());
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void connectTwitter() {
		ConfigurationBuilder confbuilder = new ConfigurationBuilder();
		Configuration conf = confbuilder.setOAuthConsumerKey(CommonSettings.TwitterConsumerKey).setOAuthConsumerSecret(CommonSettings.TwitterConsumerSecret).build();

		engineTwitter = new TwitterFactory(conf).getInstance();
		engineTwitter.setOAuthAccessToken(null);

		try {
			twitterRequestToken = engineTwitter.getOAuthRequestToken(CommonSettings.TwitterCallbackURI);
			Intent intent = new Intent(this, TwitterApp.class);
			intent.putExtra(CommonSettings.IEXTRA_AUTH_URL, twitterRequestToken.getAuthorizationURL());
			this.startActivityForResult(intent, 0);
		}
		catch (TwitterException e) {
			Toast.makeText(OfferDetails.this, "Errror : " + e.getStatusCode(), Toast.LENGTH_LONG).show();
		}
	}

	private class TwitterConnectTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(twitterContext);
			progressDialog.setMessage(twitterContext.getString(R.string.twitter_initializing));
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... v) {
			connectTwitter();
			return (Void)null;
		}

		@Override
		protected void onProgressUpdate(Void... v) {
		}

		@Override
		protected void onPostExecute(Void v) {
			progressDialog.dismiss();
		}
	}
    
	/* ------------------------------------------
	 * 
	 * Facebook
	 * 
	 * ------------------------------------------
	 */
	public void PostOnFacebook() {
    	this.selectedOp = SocNetOp.SocNetOpFacebook;
         try {
        	 String response = engineFacebook.request("me");
        	 
        	 JSONObject attachment = new JSONObject();
        	 attachment.put("name", jobOffer.Title);
        	 attachment.put("href", "http://bombajob.bg/offer/" + jobOffer.OfferID);
        	 attachment.put("caption", jobOffer.Positivism);
        	 attachment.put("description", jobOffer.Negativism);

        	 Bundle params = new Bundle();
        	 params.putString("attachment", attachment.toString());

        	 JSONObject actionLink = new JSONObject();
        	 actionLink.put("text", "BombaJob.bg");
        	 actionLink.put("href", "http://bombajob.bg/");
        	 
        	 JSONArray jasonarray = new JSONArray().put(actionLink);
        	 params.putString("action_links", jasonarray.toString());
        	 
        	 engineFacebook.dialog(OfferDetails.this, "stream.publish", params, new FBDialogListener());
        	 if (response == null || response.equals("") || response.equals("false"))
        		 Log.v("Error", "Blank response");
        	 else
        		 Log.v("Error", "got response: " + response);
         }
         catch(Exception e) {
             e.printStackTrace();
         }
    }
	
	public class FBAuthListener implements AuthListener {
        public void onAuthSucceed() {
        	SessionStore.save(engineFacebook, OfferDetails.this);
        }
        public void onAuthFail(String error) {
        }
    }

    public class FBDialogListener extends BaseDialogListener {
        public void onComplete(Bundle values) {
            final String postId = values.getString("post_id");
            if (postId != null)
            	fbAsyncRunner.request(postId, new FBWallPostRequestListener());
        }
    }

    public class FBWallPostRequestListener extends BaseRequestListener {
        public void onComplete(final String response, final Object state) {
            try {
            	Log.d("OfferDetails", "Facebook response - " + response);
            	String message = "<empty>";
                JSONObject json = Util.parseJson(response);
                //message = json.getString("message");
                message = json.getString("id");
                Log.d("OfferDetails", "Facebook post success - " + message);
            }
            catch (JSONException e) {
            	e.printStackTrace();
            	OfferDetails.this.runOnUiThread(
        			new Runnable() {
        				public void run() {
        	            	Toast.makeText(OfferDetails.this, R.string.facebook_publisherror, Toast.LENGTH_LONG).show();
        				}
        			}
            	);
            }
            catch (FacebookError e) {
            	e.printStackTrace();
            	OfferDetails.this.runOnUiThread(
        			new Runnable() {
        				public void run() {
        					Toast.makeText(OfferDetails.this, R.string.facebook_responseerror, Toast.LENGTH_LONG).show();
        				}
        			}
            	);
            }
        	OfferDetails.this.runOnUiThread(
    			new Runnable() {
    				public void run() {
    		        	Toast.makeText(OfferDetails.this, R.string.facebook_publishok, Toast.LENGTH_LONG).show();
    				}
    			}
        	);
        }
    }
}
