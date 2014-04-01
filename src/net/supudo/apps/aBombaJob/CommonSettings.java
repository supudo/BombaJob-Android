package net.supudo.apps.aBombaJob;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public enum CommonSettings {
	INSTANCE;
	
	public static final String BASE_SERVICES_URL = "http://bombajob.supudo.net/_mob_service_json.php";
	public static final String HOST_NAME = "bombajob.supudo.net";
	public static final String PREFS_FILE_NAME = "BombaJobPreferences";
	public static String AppCallbackURI = "bombajobandroid";
	
	public static String AppVersion = "";
	public static boolean ShowBanners = false;
	public static String DefaultDateFormat = "yyyy-MM-dd HH:mm:ss";
	
	public static String GoogleAddsAppID = "a14eb7e357a4b8c";
	public static String FacebookAppID = "162884250446512";
	public static String TwitterConsumerKey = "uSlctvG45nI6JawDFzGHw", TwitterConsumerSecret = "RGLWAZDxDBfqnw4i0LHTEEnfRPaYzAZQXZriSI1cI";
	public static String TwitterCallbackURI = AppCallbackURI + "://twitter";
	
	public static final String IEXTRA_AUTH_URL = "auth_url";
	public static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";
	public static final String IEXTRA_OAUTH_TOKEN = "oauth_token";
	public static String PREFERENCE_NAME = "twitter_oauth";
	public static final String PREF_KEY_SECRET = "oauth_token_secret";
	public static final String PREF_KEY_TOKEN = "oauth_token";
	public static final String PREF_KEY_CONNECTED = "connected";
	
	public static boolean reloadNewestOffers = true, reloadSearchJobs = true;
	public static boolean reloadSearchPeople = true, reloadSearch = true;

	public static boolean stSearchOnline = true, stStorePrivateData = true, stSendGeo = true;
	public static boolean stInitSync = true, stInAppEmail = true, stShowCategories = true;
	public static String stPrivateData_Email = "";
	
	public enum AppSettings {
		stSearchOnline,
		stStorePrivateData,
		stSendGeo,
		stInitSync,
		stInAppEmail,
		stShowCategories
	}
	
	public static Date lastSyncDate;
	
	public static boolean GetSetting(Context _ctx, AppSettings _setting) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_ctx);
		boolean _result = false;
		switch (_setting) {
			case stSearchOnline:
				CommonSettings.stSearchOnline = sharedPrefs.getBoolean("SearchOnline", false);
				_result = sharedPrefs.getBoolean("SearchOnline", false);
				break;
			case stStorePrivateData:
				CommonSettings.stStorePrivateData = sharedPrefs.getBoolean("StorePrivateData", false);
				_result = sharedPrefs.getBoolean("StorePrivateData", false);
				break;
			case stSendGeo:
				CommonSettings.stSendGeo = sharedPrefs.getBoolean("SendGeo", false);
				_result = sharedPrefs.getBoolean("SendGeo", false);
				break;
			case stInitSync:
				CommonSettings.stInitSync = sharedPrefs.getBoolean("InitSync", false);
				_result = sharedPrefs.getBoolean("InitSync", false);
				break;
			case stInAppEmail:
				CommonSettings.stInAppEmail = sharedPrefs.getBoolean("InAppEmail", false);
				_result = sharedPrefs.getBoolean("InAppEmail", false);
				break;
			case stShowCategories:
				CommonSettings.stShowCategories = sharedPrefs.getBoolean("ShowCategories", false);
				_result = sharedPrefs.getBoolean("ShowCategories", false);
				break;
		}
		return _result;
		
	}
}
