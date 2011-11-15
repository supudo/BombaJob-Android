package com.supudo.net.apps.aBombaJob.Synchronization;

import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.supudo.net.apps.aBombaJob.CommonSettings;
import com.supudo.net.apps.aBombaJob.Database.DatabaseModel;
import com.supudo.net.apps.aBombaJob.Database.DatabaseSchema;
import com.supudo.net.apps.aBombaJob.URLHelpers.URLHelperCallbacks;
import com.supudo.net.apps.aBombaJob.URLHelpers.URLHelper;

public class SyncManager implements URLHelperCallbacks {

	public static interface SyncManagerCallbacks {
		public void syncFinished();
		public void onSyncProgress(int progress);
		public void onSyncError(Exception ex);
	}

	public static final class ServicesNames {
		private ServicesNames () {}
		public static final String CONFIGURATION_SERVICE = "?action=getConfig";
		public static final String POSTOFFER_SERVICE = "?action=postNewJob";
		public static final String POSTMESSAGE_SERVICE = "?action=postMessage";
		public static final String CATEGORIES_SERVICE = "?action=getCategories";
		public static final String NEWOFFERS_SERVICE = "?action=getNewJobs";
		public static final String SEARCHJOBS_SERVICE = "?action=searchJobs";
		public static final String SEARCHPEOPLE_SERVICE = "?action=searchPeople";
		public static final String TEXTCONTENT_SERVICE = "?action=getTextContent";
		public static final String SEARCH_SERVICE = "?action=searchOffers";
		public static final String SENDEMAIL_SERVICE = "?action=sendEmailMessage";
	}

	public static final class WebServiceID {
		private WebServiceID () {}
		public static final Integer CONFIGURATION_SERVICE = 0;
		public static final Integer POSTOFFER_SERVICE = 1;
		public static final Integer POSTMESSAGE_SERVICE = 2;
		public static final Integer CATEGORIES_SERVICE = 3;
		public static final Integer NEWOFFERS_SERVICE = 4;
		public static final Integer SEARCHJOBS_SERVICE = 5;
		public static final Integer SEARCHPEOPLE_SERVICE = 6;
		public static final Integer TEXTCONTENT_SERVICE = 7;
		public static final Integer SEARCH_SERVICE = 8;
		public static final Integer SENDEMAIL_SERVICE = 9;
	}

	private SyncManagerCallbacks mDelegate;

	private Context mCtx;

	private URLHelper urlHelper;
	private DatabaseModel dbModel;
	private SQLiteDatabase db = null;
	private boolean finished = false;

	private String state = ServicesNames.CONFIGURATION_SERVICE;

	public String getState() {
		return state;
	}

	public boolean isFinished() {
		return finished;
	}

	public SyncManager(Context context, SyncManagerCallbacks delegate) {
		mDelegate = delegate;
		mCtx = context;
		urlHelper = new URLHelper(this);
		dbModel = new DatabaseModel(mCtx);
	}

	public void clearDatabase() {
		SQLiteDatabase db_tmp = dbModel.getWritableDatabase();
		db_tmp.execSQL("PRAGMA foreign_keys = OFF;");
		db_tmp.execSQL("DELETE FROM " + DatabaseSchema.SETTINGS_TABLE_NAME);
		db_tmp.execSQL("DELETE FROM " + DatabaseSchema.TEXTCONTENT_TABLE_NAME);
		db_tmp.execSQL("DELETE FROM " + DatabaseSchema.JOBOFFER_TABLE_NAME);
		db_tmp.execSQL("DELETE FROM " + DatabaseSchema.CATEGORY_TABLE_NAME);
		db_tmp.close();
		Log.d("Sync", "Database cleared");
	}

	public void cancel() {
		if (urlHelper != null) {
			urlHelper.cancel();
			urlHelper = null;
		}
		if (db != null) {
			if (db.isOpen())
				db.close();
			db = null;
		}
		mCtx = null;
	}
	
	public void synchronize() {
		synchronize(ServicesNames.CONFIGURATION_SERVICE);
	}

	public void synchronize(String serviceName) {
		try {
			db = dbModel.getWritableDatabase();
			db.execSQL("PRAGMA foreign_keys = ON;");
			Log.d("Sync", "Synchronizing ... " + serviceName);
			if (serviceName.equals(ServicesNames.CONFIGURATION_SERVICE))
				this.loadConfigurationUrl();
			else if (serviceName.equals(ServicesNames.CATEGORIES_SERVICE))
				this.loadCategoriesUrl();
			else if (serviceName.equals(ServicesNames.TEXTCONTENT_SERVICE))
				this.loadTextContentUrl();
			else if (serviceName.equals(ServicesNames.NEWOFFERS_SERVICE))
				this.loadNewOffersUrl();
			else if (serviceName.equals(ServicesNames.SEARCHJOBS_SERVICE))
				this.loadSearchJobsUrl();
			else if (serviceName.equals(ServicesNames.SEARCHPEOPLE_SERVICE))
				this.loadSearchPeopleUrl();
		}
		catch (NotFoundException e) {
			e.printStackTrace();
			Log.d("Sync", "Synchronizing error - " + e.getMessage());
			mDelegate.onSyncError(e);
		}
		catch (MalformedURLException e) {
			Log.d("Sync", "Synchronizing error - " + e.getMessage());
			e.printStackTrace();
			mDelegate.onSyncError(e);
		}
	}

	@Override
	public void updateModelWithJSONObject(JSONObject object, Integer serviceId) {
		try {
			if (serviceId == WebServiceID.CONFIGURATION_SERVICE) {
				Log.d("Sync", "updateModelWithJSONObject ... configuration");
				handleConfiguration(object);
				this.loadCategoriesUrl();
				mDelegate.onSyncProgress(100);
			}
			else if (serviceId == WebServiceID.CATEGORIES_SERVICE) {
				Log.d("Sync", "updateModelWithJSONObject ... categories");
				handleCategories(object);
				this.loadTextContentUrl();
				mDelegate.onSyncProgress(200);
			}
			else if (serviceId == WebServiceID.TEXTCONTENT_SERVICE) {
				Log.d("Sync", "updateModelWithJSONObject ... text content");
				handleTextContent(object);
				this.loadNewOffersUrl();
				mDelegate.onSyncProgress(300);
			}
			else if (serviceId == WebServiceID.NEWOFFERS_SERVICE) {
				Log.d("Sync", "updateModelWithJSONObject ... new offers");
				this.handleNewOffers(object);
				mDelegate.onSyncProgress(400);
			}
			else if (serviceId == WebServiceID.SEARCHJOBS_SERVICE) {
				Log.d("Sync", "updateModelWithJSONObject ... search jobs");
				this.handleSearchJobs(object);
			}
			else if (serviceId == WebServiceID.SEARCHPEOPLE_SERVICE) {
				Log.d("Sync", "updateModelWithJSONObject ... search people");
				this.handleSearchPeople(object);
			}
			else if (serviceId == WebServiceID.SEARCH_SERVICE) {
				Log.d("Sync", "updateModelWithJSONObject ... search");
				this.handleOffers(object, "searchOffers", WebServiceID.SEARCH_SERVICE);
			}
		}
		catch (NotFoundException e) {
			e.printStackTrace();
			mDelegate.onSyncError(e);
			this.connectionFailed(serviceId);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			mDelegate.onSyncError(e);
			this.connectionFailed(serviceId);
		}
	}

	@Override
	public void connectionFailed(Integer serviceId) {
		mDelegate.syncFinished();
		if (db != null) {
			db.close();
			db = null;
		}
	}

	private void finishSync() {
		db.close();
		finished = true;
		mDelegate.syncFinished();
	}
	
	/* ------------------------------------------
	 * 
	 * Public services
	 * 
	 * ------------------------------------------
	 */
	public void GetSearchJobs() {
		try {
			db = dbModel.getWritableDatabase();
			db.execSQL("PRAGMA foreign_keys = ON;");
			Log.d("Sync", "GetSearchJobs ... ");
			this.loadSearchJobsUrl();
		}
		catch (NotFoundException e) {
			e.printStackTrace();
			Log.d("Sync", "GetSearchJobs error - " + e.getMessage());
			mDelegate.onSyncError(e);
		}
		catch (MalformedURLException e) {
			Log.d("Sync", "GetSearchJobs error - " + e.getMessage());
			e.printStackTrace();
			mDelegate.onSyncError(e);
		}
	}

	public void GetSearchPeople() {
		try {
			db = dbModel.getWritableDatabase();
			db.execSQL("PRAGMA foreign_keys = ON;");
			Log.d("Sync", "GetSearchPeople ... ");
			this.loadSearchPeopleUrl();
		}
		catch (NotFoundException e) {
			e.printStackTrace();
			Log.d("Sync", "GetSearchPeople error - " + e.getMessage());
			mDelegate.onSyncError(e);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			Log.d("Sync", "GetSearchPeople error - " + e.getMessage());
			mDelegate.onSyncError(e);
		}
	}

	public void GetSearch(String searchKeyword, int searchForFreelance) {
		try {
			db = dbModel.getWritableDatabase();
			db.execSQL("PRAGMA foreign_keys = ON;");
			Log.d("Sync", "GetSearch ... ");
			this.loadSearchUrl(searchKeyword, searchForFreelance);
		}
		catch (NotFoundException e) {
			e.printStackTrace();
			Log.d("Sync", "GetSearch error - " + e.getMessage());
			mDelegate.onSyncError(e);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			Log.d("Sync", "GetSearch error - " + e.getMessage());
			mDelegate.onSyncError(e);
		}
	}
	
	/* ------------------------------------------
	 * 
	 * Synchronization URLs
	 * 
	 * ------------------------------------------
	 */
	private void loadConfigurationUrl() throws MalformedURLException, NotFoundException {
		this.state = ServicesNames.CONFIGURATION_SERVICE;
		Log.d("Sync", CommonSettings.BASE_SERVICES_URL + ServicesNames.CONFIGURATION_SERVICE);
		urlHelper.loadURLString(CommonSettings.BASE_SERVICES_URL + ServicesNames.CONFIGURATION_SERVICE, WebServiceID.CONFIGURATION_SERVICE);
	}

	private void loadCategoriesUrl() throws MalformedURLException, NotFoundException {
		this.state = ServicesNames.CATEGORIES_SERVICE;
		Log.d("Sync", CommonSettings.BASE_SERVICES_URL + ServicesNames.CATEGORIES_SERVICE);
		urlHelper.loadURLString(CommonSettings.BASE_SERVICES_URL + ServicesNames.CATEGORIES_SERVICE, WebServiceID.CATEGORIES_SERVICE);
	}

	private void loadTextContentUrl() throws MalformedURLException, NotFoundException {
		this.state = ServicesNames.TEXTCONTENT_SERVICE;
		Log.d("Sync", CommonSettings.BASE_SERVICES_URL + ServicesNames.TEXTCONTENT_SERVICE);
		urlHelper.loadURLString(CommonSettings.BASE_SERVICES_URL + ServicesNames.TEXTCONTENT_SERVICE, WebServiceID.TEXTCONTENT_SERVICE);
	}

	private void loadNewOffersUrl() throws MalformedURLException, NotFoundException {
		this.state = ServicesNames.NEWOFFERS_SERVICE;
		Log.d("Sync", CommonSettings.BASE_SERVICES_URL + ServicesNames.NEWOFFERS_SERVICE);
		urlHelper.loadURLString(CommonSettings.BASE_SERVICES_URL + ServicesNames.NEWOFFERS_SERVICE, WebServiceID.NEWOFFERS_SERVICE);
	}

	private void loadSearchJobsUrl() throws MalformedURLException, NotFoundException {
		this.state = ServicesNames.SEARCHJOBS_SERVICE;
		Log.d("Sync", CommonSettings.BASE_SERVICES_URL + ServicesNames.SEARCHJOBS_SERVICE);
		urlHelper.loadURLString(CommonSettings.BASE_SERVICES_URL + ServicesNames.SEARCHJOBS_SERVICE, WebServiceID.SEARCHJOBS_SERVICE);
	}

	private void loadSearchPeopleUrl() throws MalformedURLException, NotFoundException {
		this.state = ServicesNames.SEARCHPEOPLE_SERVICE;
		Log.d("Sync", CommonSettings.BASE_SERVICES_URL + ServicesNames.SEARCHPEOPLE_SERVICE);
		urlHelper.loadURLString(CommonSettings.BASE_SERVICES_URL + ServicesNames.SEARCHPEOPLE_SERVICE, WebServiceID.SEARCHPEOPLE_SERVICE);
	}

	private void loadSearchUrl(String searchKeyword, int searchForFreelance) throws MalformedURLException, NotFoundException {
		this.state = ServicesNames.SEARCH_SERVICE;
		Log.d("Sync", CommonSettings.BASE_SERVICES_URL + ServicesNames.SEARCH_SERVICE + "&keyword=" + searchKeyword + "&freelance=" + searchForFreelance);
		urlHelper.loadURLString(CommonSettings.BASE_SERVICES_URL + ServicesNames.SEARCH_SERVICE + "&keyword=" + searchKeyword + "&freelance=" + searchForFreelance, WebServiceID.SEARCH_SERVICE);
	}
	
	/* ------------------------------------------
	 * 
	 * Synchronization Data
	 * 
	 * ------------------------------------------
	 */
	private void handleConfiguration(JSONObject object) {
		try {
			CommonSettings.AppVersion = object.getJSONObject("getConfig").getString("version");
			CommonSettings.ShowBanners = object.getJSONObject("getConfig").getBoolean("showBanners");
			Log.d("Sync", "handleConfiguration ... done");
		}
		catch (Exception e) {
			e.printStackTrace();
			this.connectionFailed(WebServiceID.CONFIGURATION_SERVICE);
			mDelegate.onSyncError(e);
		}
	}

	private void handleCategories(JSONObject object) {
		try {
			JSONArray categories = object.getJSONArray("getCategories");
			for (int i=0; i<categories.length(); ++i) {
				JSONObject ent = categories.getJSONObject(i);

				ContentValues cv = new ContentValues();
				cv.put(DatabaseSchema.CategoriesColumns._ID, Integer.parseInt(ent.getString("id")));
				cv.put(DatabaseSchema.CategoriesColumns.OFFERS_COUNT, Integer.parseInt(ent.getString("offerCount")));
				cv.put(DatabaseSchema.CategoriesColumns.TITLE, ent.getString("name"));

				Cursor c = db.rawQuery("SELECT * FROM " + DatabaseSchema.CATEGORY_TABLE_NAME + " WHERE " + DatabaseSchema.CategoriesColumns._ID + " = ?;", new String[] {ent.getString("id")});
				if (c.getCount() == 0)
					db.insert(DatabaseSchema.CATEGORY_TABLE_NAME, null, cv);
				else
					db.update(DatabaseSchema.CATEGORY_TABLE_NAME, cv, DatabaseSchema.CategoriesColumns._ID + " = ?", new String[]{ent.getString("id")});
				c.close();
			}
			Log.d("Sync", "handleCategories ... done");
		}
		catch (Exception e) {
			e.printStackTrace();
			this.connectionFailed(WebServiceID.CATEGORIES_SERVICE);
			mDelegate.onSyncError(e);
		}
	}

	private void handleTextContent(JSONObject object) {
		try {
			JSONArray tc = object.getJSONArray("getTextContent");
			for (int i=0; i<tc.length(); ++i) {
				JSONObject ent = tc.getJSONObject(i);

				ContentValues cv = new ContentValues();
				cv.put(DatabaseSchema.TextContentColumns._ID, Integer.parseInt(ent.getString("id")));
				cv.put(DatabaseSchema.TextContentColumns.TITLE, ent.getString("title"));
				cv.put(DatabaseSchema.TextContentColumns.CONTENT, ent.getString("content"));

				Cursor c = db.rawQuery("SELECT * FROM " + DatabaseSchema.TEXTCONTENT_TABLE_NAME + " WHERE " + DatabaseSchema.TextContentColumns._ID + " = ?;", new String[] {ent.getString("id")});
				if (c.getCount() == 0)
					db.insert(DatabaseSchema.TEXTCONTENT_TABLE_NAME, null, cv);
				else
					db.update(DatabaseSchema.TEXTCONTENT_TABLE_NAME, cv, DatabaseSchema.TextContentColumns._ID + " = ?", new String[]{ent.getString("id")});
				c.close();
			}
			Log.d("Sync", "handleTextContent ... done");
		}
		catch (Exception e) {
			e.printStackTrace();
			this.connectionFailed(WebServiceID.TEXTCONTENT_SERVICE);
			mDelegate.onSyncError(e);
		}
	}

	private void handleNewOffers(JSONObject object) {
		try {
			JSONArray offers = object.getJSONArray("getNewJobs");
			for (int i=0; i<offers.length(); ++i) {
				JSONObject ent = offers.getJSONObject(i);

				ContentValues cv = new ContentValues();
				cv.put(DatabaseSchema.JobOffersColumns._ID, Integer.parseInt(ent.getString("id")));
				cv.put(DatabaseSchema.JobOffersColumns.CATEGORY_ID, Integer.parseInt(ent.getString("cid")));
				cv.put(DatabaseSchema.JobOffersColumns.TITLE, ent.getString("title"));
				cv.put(DatabaseSchema.JobOffersColumns.EMAIL, ent.getString("email"));
				cv.put(DatabaseSchema.JobOffersColumns.CATEGORY_TITLE, ent.getString("category"));
				cv.put(DatabaseSchema.JobOffersColumns.POSITIVISM, ent.getString("positivism"));
				cv.put(DatabaseSchema.JobOffersColumns.NEGATIVISM, ent.getString("negativism"));
				cv.put(DatabaseSchema.JobOffersColumns.GEO_LATITUDE, ent.getString("glat"));
				cv.put(DatabaseSchema.JobOffersColumns.GEO_LONGITUDE, ent.getString("glong"));
				cv.put(DatabaseSchema.JobOffersColumns.FREELANCE_YN, ent.getInt("fyn"));
				cv.put(DatabaseSchema.JobOffersColumns.HUMAN_YN, ent.getInt("hm"));
				cv.put(DatabaseSchema.JobOffersColumns.PUBLISH_DATE, ent.getString("date"));
				cv.put(DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP, ent.getString("datestamp"));

				Cursor c = db.rawQuery("SELECT * FROM " + DatabaseSchema.JOBOFFER_TABLE_NAME + " WHERE " + DatabaseSchema.JobOffersColumns._ID + " = ?;", new String[] {ent.getString("id")});
				if (c.getCount() == 0) {
					cv.put(DatabaseSchema.JobOffersColumns.SENTMESSAGE_YN, 0);
					cv.put(DatabaseSchema.JobOffersColumns.READ_YN, 0);
					db.insert(DatabaseSchema.JOBOFFER_TABLE_NAME, null, cv);
				}
				else
					db.update(DatabaseSchema.JOBOFFER_TABLE_NAME, cv, DatabaseSchema.JobOffersColumns._ID + " = ?", new String[]{ent.getString("id")});
				c.close();
			}
			Log.d("Sync", "getNewJobs ... done");
			finishSync();
		}
		catch (Exception e) {
			e.printStackTrace();
			this.connectionFailed(WebServiceID.NEWOFFERS_SERVICE);
			mDelegate.onSyncError(e);
		}
	}

	private void handleSearchJobs(JSONObject object) {
		handleOffers(object, "searchJobs", WebServiceID.SEARCHJOBS_SERVICE);
	}

	private void handleSearchPeople(JSONObject object) {
		handleOffers(object, "searchPeople", WebServiceID.SEARCHPEOPLE_SERVICE);
	}

	private void handleOffers(JSONObject object, String jsonArray, int webServiceID) {
		try {
			JSONArray offers = object.getJSONArray(jsonArray);
			for (int i=0; i<offers.length(); ++i) {
				JSONObject ent = offers.getJSONObject(i);

				ContentValues cv = new ContentValues();
				cv.put(DatabaseSchema.JobOffersColumns._ID, Integer.parseInt(ent.getString("id")));
				cv.put(DatabaseSchema.JobOffersColumns.CATEGORY_ID, Integer.parseInt(ent.getString("cid")));
				cv.put(DatabaseSchema.JobOffersColumns.TITLE, ent.getString("title"));
				cv.put(DatabaseSchema.JobOffersColumns.EMAIL, ent.getString("email"));
				cv.put(DatabaseSchema.JobOffersColumns.CATEGORY_TITLE, ent.getString("category"));
				cv.put(DatabaseSchema.JobOffersColumns.POSITIVISM, ent.getString("positivism"));
				cv.put(DatabaseSchema.JobOffersColumns.NEGATIVISM, ent.getString("negativism"));
				cv.put(DatabaseSchema.JobOffersColumns.GEO_LATITUDE, ent.getString("glat"));
				cv.put(DatabaseSchema.JobOffersColumns.GEO_LONGITUDE, ent.getString("glong"));
				cv.put(DatabaseSchema.JobOffersColumns.FREELANCE_YN, ent.getInt("fyn"));
				cv.put(DatabaseSchema.JobOffersColumns.HUMAN_YN, ent.getInt("hm"));
				cv.put(DatabaseSchema.JobOffersColumns.PUBLISH_DATE, ent.getString("date"));
				cv.put(DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP, ent.getString("datestamp"));

				Cursor c = db.rawQuery("SELECT * FROM " + DatabaseSchema.JOBOFFER_TABLE_NAME + " WHERE " + DatabaseSchema.JobOffersColumns._ID + " = ?;", new String[] {ent.getString("id")});
				if (c.getCount() == 0) {
					cv.put(DatabaseSchema.JobOffersColumns.SENTMESSAGE_YN, 0);
					cv.put(DatabaseSchema.JobOffersColumns.READ_YN, 0);
					db.insert(DatabaseSchema.JOBOFFER_TABLE_NAME, null, cv);
				}
				else
					db.update(DatabaseSchema.JOBOFFER_TABLE_NAME, cv, DatabaseSchema.JobOffersColumns._ID + " = ?", new String[]{ent.getString("id")});
				c.close();
			}
			Log.d("Sync", jsonArray + " ... done");
			finishSync();
		}
		catch (Exception e) {
			e.printStackTrace();
			this.connectionFailed(webServiceID);
			mDelegate.onSyncError(e);
		}
	}

}
