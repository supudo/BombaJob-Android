package net.supudo.apps.aBombaJob.Post;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import net.supudo.apps.aBombaJob.CommonSettings;
import net.supudo.apps.aBombaJob.MainActivity;
import net.supudo.apps.aBombaJob.DataAdapters.SpinnerCategoriesAdapter;

import net.supudo.apps.aBombaJob.R;
import net.supudo.apps.aBombaJob.Database.DataHelper;
import net.supudo.apps.aBombaJob.Database.Models.CategoryModel;
import net.supudo.apps.aBombaJob.Synchronization.SyncManager;
import net.supudo.apps.aBombaJob.Synchronization.SyncManager.SyncManagerCallbacks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Post extends MainActivity implements Runnable, SyncManagerCallbacks {

	private DataHelper dbHelper;
	private SyncManager syncManager;
	private ArrayList<CategoryModel> itemsCategories;
	private ProgressDialog loadingDialog;

	private Spinner cmbHumanYn, cmbFreelanceYn, cmbCategory;
	private EditText txtTitle, txtEmail, txtPositiv, txtNegativ;
	private TextView lblTitle, lblEmail, lblPositiv, lvlNegativ;
	private Button btnPost;

	private boolean pHumanYn;
	private int pFreelance, pCategoryID;
	private String valTitle, valEmail, valPositiv, valNegativ;
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	loadingDialog.dismiss();
        	
        	if (msg.getData().getString("exception").equals("true"))
        		goBack();
        	else {
        		try {
	    			String errorLabel = msg.getData().getString("exception");
	    			String errorMessage = (String) Post.this.getResources().getText(Post.this.getResources().getIdentifier(errorLabel, "string", "net.supudo.apps.aBombaJob"));
	        		AlertDialog.Builder alertbox = new AlertDialog.Builder(Post.this);
	        		alertbox.setMessage(errorMessage);
	        		alertbox.setNeutralButton(R.string.close_alertbox, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface arg0, int arg1) {
	                    }
	                });
	                alertbox.show();
        		}
        		catch (Exception e) {
	        		AlertDialog.Builder alertbox = new AlertDialog.Builder(Post.this);
	        		alertbox.setMessage(e.getMessage());
	        		alertbox.setNeutralButton(R.string.close_alertbox, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface arg0, int arg1) {
	                    }
	                });
	                alertbox.show();
        		}
        	}
        }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post);
		
		cmbHumanYn = (Spinner)findViewById(R.id.human_yn);
		cmbFreelanceYn = (Spinner)findViewById(R.id.freelance);
		cmbCategory = (Spinner)findViewById(R.id.category);
		txtTitle = (EditText)findViewById(R.id.txt_title);
		txtEmail = (EditText)findViewById(R.id.txt_email);
		txtPositiv = (EditText)findViewById(R.id.txt_positiv);
		txtNegativ = (EditText)findViewById(R.id.txt_negativ);
		lblTitle = (TextView)findViewById(R.id.lbl_title);
		lblEmail = (TextView)findViewById(R.id.lbl_email);
		lblPositiv = (TextView)findViewById(R.id.lbl_positiv);
		lvlNegativ = (TextView)findViewById(R.id.lbl_negativ);

		btnPost = (Button)findViewById(R.id.btn_post);
		btnPost.setOnClickListener(btnPostListener);
		
		if (dbHelper == null)
			dbHelper = new DataHelper(this);

		if (syncManager == null)
			syncManager = new SyncManager(this, this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		itemsCategories = dbHelper.selectAllCategory();

		LoadDropdowns();
		LoadLabels();
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
		try {
			JSONObject res = (new JSONObject(ex.getMessage())).getJSONObject("postNewJob");
			Log.d("Post", res.toString());
			Message msg = handler.obtainMessage(); 
	        Bundle b = new Bundle();
	        b.putString("exception", res.get("result").toString()); 
	        msg.setData(b);
	        handler.handleMessage(msg);
		}
		catch (JSONException e) {
			e.printStackTrace();
			handler.handleMessage(handler.obtainMessage());
		}
	}
	
	public void run() {
		syncManager.PostOffer(pHumanYn, pFreelance, pCategoryID, valTitle, valEmail, valPositiv, valNegativ);
	}
	
	private OnClickListener btnPostListener = new OnClickListener() {
		public void onClick(View v) {
			valTitle = txtTitle.getText().toString().trim();
			valEmail = txtEmail.getText().toString().trim();
			valPositiv = txtPositiv.getText().toString().trim();
			valNegativ = txtNegativ.getText().toString().trim();
			
			boolean validationError = true;
			if (valTitle.equals(""))
				txtTitle.setError(((pHumanYn) ? getString(R.string.post_error_Human_Title) : getString(R.string.post_error_Company_Title)));
			else if (valEmail.equals(""))
				txtEmail.setError(((pHumanYn) ? getString(R.string.post_error_Human_Email) : getString(R.string.post_error_Company_Email)));
			else if (valNegativ.equals(""))
				txtNegativ.setError(((pHumanYn) ? getString(R.string.post_error_Human_Negativ) : getString(R.string.post_error_Company_Negativ)));
			else if (valPositiv.equals(""))
				txtPositiv.setError(((pHumanYn) ? getString(R.string.post_error_Human_Positiv) : getString(R.string.post_error_Company_Positiv)));
			else
				validationError = false;
			
			if (pCategoryID == 0)
				validationError = true;

			if (validationError)
				Toast.makeText(getApplicationContext(), R.string.post_MissingReqFields, Toast.LENGTH_SHORT).show();
			else {
				loadingDialog = ProgressDialog.show(Post.this, "", getString(R.string.loading), true);
		     	Thread thread = new Thread(Post.this);
		        thread.run();
			}
        }
    };
	
	private void LoadDropdowns() {
    	if (CommonSettings.stStorePrivateData)
    		txtEmail.setText((dbHelper.GetSetting("PrivateData_Email")).SValue);

		ArrayAdapter<CharSequence> _adapterHumanCompany = ArrayAdapter.createFromResource(this, R.array.post_HumanCompany_array, android.R.layout.simple_spinner_item);
		_adapterHumanCompany.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cmbHumanYn.setAdapter(_adapterHumanCompany);
		cmbHumanYn.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				pHumanYn = ((pos == 0) ? true : false);
				LoadLabels();
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		ArrayAdapter<CharSequence> _adapterFreelance = ArrayAdapter.createFromResource(this, R.array.searchFreelance_array, android.R.layout.simple_spinner_item);
		_adapterFreelance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cmbFreelanceYn.setAdapter(_adapterFreelance);
		//cmbFreelanceYn.setPromptId(R.string.searchFreelance);
		cmbFreelanceYn.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				pFreelance = pos;
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		//TODO : add prompt
		SpinnerCategoriesAdapter _catsAdapter = new SpinnerCategoriesAdapter(Post.this, itemsCategories);
		cmbCategory.setAdapter(_catsAdapter);
		cmbCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                View view, int pos, long id) {
                CategoryModel cat = (CategoryModel)parent.getItemAtPosition(pos);
                pCategoryID = cat.CategoryID;
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
	}
	
	private void LoadLabels() {
		if (pHumanYn) {
			cmbCategory.setPromptId(R.string.post_Category_Human);
			lblTitle.setText(R.string.post_Human_Title);
			lblEmail.setText(R.string.post_Human_Email);
			lblPositiv.setText(R.string.post_Human_Positiv);
			lvlNegativ.setText(R.string.post_Human_Negativ);
		}
		else {
			cmbCategory.setPromptId(R.string.post_Category_Company);
			lblTitle.setText(R.string.post_Company_Title);
			lblEmail.setText(R.string.post_Company_Email);
			lblPositiv.setText(R.string.post_Company_Positiv);
			lvlNegativ.setText(R.string.post_Company_Negativ);
		}
	}
    
    private void goBack() {
    	if (CommonSettings.stStorePrivateData)
    		dbHelper.SetSetting("PrivateData_Email", valEmail);
    	Toast.makeText(getApplicationContext(), R.string.post_OfferSuccess, Toast.LENGTH_SHORT).show();
    	Timer timer = new Timer();
        timer.schedule( new TimerTask(){
           public void run() { 
       			Post.this.onBackPressed();
            }
         }, 2000);
    }

}
