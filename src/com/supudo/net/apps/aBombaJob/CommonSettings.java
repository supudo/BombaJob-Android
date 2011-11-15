package com.supudo.net.apps.aBombaJob;

public class CommonSettings {
	
	private CommonSettings(){};
	
	public static final String BASE_SERVICES_URL = "http://www.bombajob.bg/_mob_service_json.php";
	public static final String HOST_NAME = "www.bombajob.bg";
	public static final String PREFS_FILE_NAME = "BombaJobPreferences";
	
	public static String AppVersion = "";
	public static boolean ShowBanners = false;
	
	public static String GoogleAddsAppID = "a14eb7e357a4b8c";
	
	public static boolean reloadNewestOffers = true, reloadSearchJobs = true;
	public static boolean reloadSearchPeople = true, reloadSearch = true;

	public static boolean stSearchOnline = true, stStorePrivateData = true, stSendGeo = true;
	public static boolean stInitSync = true, stInAppEmail = true, stShowCategories = true;
}
