package com.supudo.net.apps.aBombaJob.Offers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.supudo.net.apps.aBombaJob.CommonSettings;
import com.supudo.net.apps.aBombaJob.MainActivity;
import com.supudo.net.apps.aBombaJob.R;
import com.supudo.net.apps.aBombaJob.Database.DataHelper;
import com.supudo.net.apps.aBombaJob.Database.Models.JobOfferModel;
import com.supudo.net.apps.aBombaJob.Facebook.BaseDialogListener;
import com.supudo.net.apps.aBombaJob.Facebook.BaseRequestListener;
import com.supudo.net.apps.aBombaJob.Facebook.SessionEvents;
import com.supudo.net.apps.aBombaJob.Facebook.SessionEvents.AuthListener;
import com.supudo.net.apps.aBombaJob.Facebook.SessionStore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class OfferDetails extends MainActivity {

	private Integer offerID;
	private DataHelper dbHelper;
	private JobOfferModel jobOffer;

    private Button fbPostButton, twitterPostButton, emailPostButton;
	private TextView txtTitle, txtCategory, txtDate, txtPositivismTitle, txtNegativismTitle;
	private TextView txtPositivism, txtNegativism, txtFreelanceTitle, txtFreelance;

    private Facebook engineFacebook;
    private AsyncFacebookRunner fbAsyncRunner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offerdetails);
		
		if (dbHelper == null)
			dbHelper = new DataHelper(this);

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

        fbPostButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	postOnWall();
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
				pDate += " " + (String) this.getResources().getText(this.getResources().getIdentifier(monthLabel, "string", "com.supudo.net.apps.aBombaJob"));
				
				pDate += " " + cal.get(Calendar.YEAR);
			}
			catch (ParseException e) {
				Log.d("OfferDetails", "Date parser failed - " + jobOffer.PublishDate);
			}
			txtDate.setText(pDate);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	engineFacebook.authorizeCallback(requestCode, resultCode, data);
    }

	/* ------------------------------------------
	 * 
	 * Facebook
	 * 
	 * ------------------------------------------
	 */
	public void postOnWall() {
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
                JSONObject json = Util.parseJson(response);
                String message = json.getString("message");
                Log.d("OfferDetails", "Facebook post success - " + message);
            }
            catch (JSONException e) {
            	Toast.makeText(OfferDetails.this, R.string.facebook_publisherror, Toast.LENGTH_LONG).show();
            }
            catch (FacebookError e) {
            	Toast.makeText(OfferDetails.this, R.string.facebook_publisherror, Toast.LENGTH_LONG).show();
            }
        	Toast.makeText(OfferDetails.this, R.string.facebook_publishok, Toast.LENGTH_LONG).show();
        }
    }

	/* ------------------------------------------
	 * 
	 * Twitter
	 * 
	 * ------------------------------------------
	 */
}
