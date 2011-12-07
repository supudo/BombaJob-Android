package net.supudo.apps.aBombaJob.Database;

import net.supudo.apps.aBombaJob.Database.DatabaseSchema.CategoriesColumns;
import net.supudo.apps.aBombaJob.Database.DatabaseSchema.JobOffersColumns;
import net.supudo.apps.aBombaJob.Database.DatabaseSchema.SettingsColumns;
import net.supudo.apps.aBombaJob.Database.DatabaseSchema.TextContentColumns;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseModel extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "BombaJob";
	private static final int DATABASE_VERSION = 1;

	private static final String SETTINGS_TABLE_CREATE =
		"CREATE TABLE " + DatabaseSchema.SETTINGS_TABLE_NAME + " (" +
		SettingsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		SettingsColumns.EDITABLE_YN + " TINYINT," +
		SettingsColumns.SNAME + " VARCHAR," +
		SettingsColumns.SVALUE + " VARCHAR);";

	private static final String TEXTCONTENT_TABLE_CREATE =
		"CREATE TABLE " + DatabaseSchema.TEXTCONTENT_TABLE_NAME + " (" +
		TextContentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		TextContentColumns.TITLE + " VARCHAR," +
		TextContentColumns.CONTENT + " VARCHAR);";

	private static final String CATEGORY_TABLE_CREATE =
		"CREATE TABLE " + DatabaseSchema.CATEGORY_TABLE_NAME + " (" +
		CategoriesColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		CategoriesColumns.TITLE + " VARCHAR," +
		CategoriesColumns.OFFERS_COUNT + " INTEGER);";

	private static final String JOBOFFER_TABLE_CREATE =
		"CREATE VIRTUAL TABLE " + DatabaseSchema.JOBOFFER_TABLE_NAME + " USING FTS3 (" +
		JobOffersColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		JobOffersColumns.CATEGORY_ID + " INTEGER UNSIGNED REFERENCES " + DatabaseSchema.CATEGORY_TABLE_NAME + "(" + DatabaseSchema.CategoriesColumns._ID + ") ON UPDATE CASCADE ON DELETE CASCADE," +
		JobOffersColumns.TITLE + " VARCHAR," +
		JobOffersColumns.CATEGORY_TITLE + " VARCHAR," +
		JobOffersColumns.EMAIL + " VARCHAR," +
		JobOffersColumns.NEGATIVISM + " VARCHAR," +
		JobOffersColumns.POSITIVISM + " VARCHAR," +
		JobOffersColumns.FREELANCE_YN + " TINYINT," +
		JobOffersColumns.HUMAN_YN + " TINYINT," +
		JobOffersColumns.READ_YN + " TINYINT," +
		JobOffersColumns.SENTMESSAGE_YN + " TINYINT," +
		JobOffersColumns.PUBLISH_DATE + " VARCHAR," +
		JobOffersColumns.PUBLISH_DATE_STAMP + " INTEGER," +
		JobOffersColumns.GEO_LATITUDE + " VARCHAR," +
		JobOffersColumns.GEO_LONGITUDE + " VARCHAR);";

	public DatabaseModel(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("PRAGMA foreign_keys = ON;");
		db.execSQL(SETTINGS_TABLE_CREATE);
		db.execSQL(TEXTCONTENT_TABLE_CREATE);
		db.execSQL(CATEGORY_TABLE_CREATE);
		db.execSQL(JOBOFFER_TABLE_CREATE);
		InitSettings(db);
		Log.e("DatabaseModel", "Database created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("DatabaseModel", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + DatabaseSchema.SETTINGS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + DatabaseSchema.TEXTCONTENT_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + DatabaseSchema.JOBOFFER_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + DatabaseSchema.CATEGORY_TABLE_NAME);
		onCreate(db);
		InitSettings(db);
	}
	
	private void InitSettings(SQLiteDatabase db) {
		String[] settings = new String[8];
		settings[0] = "StorePrivateData|TRUE|1";
		settings[1] = "SendGeo|FALSE|1";
		settings[2] = "InitSync|TRUE|1";
		settings[3] = "OnlineSearch|TRUE|1";
		settings[4] = "InAppEmail|FALSE|1";
		settings[5] = "ShowCategories|TRUE|1";
		settings[6] = "lastSyncDate| |0";
		settings[7] = "PrivateData_Email| |0";

		ContentValues cv;
		for (int i=0; i<settings.length; i++) {
			cv = new ContentValues();
			String[] starr = settings[i].split("\\|");
			cv.put(DatabaseSchema.SettingsColumns.SNAME, starr[0]);
			cv.put(DatabaseSchema.SettingsColumns.SVALUE, starr[1]);
			cv.put(DatabaseSchema.SettingsColumns.EDITABLE_YN, starr[2]);
			db.insert(DatabaseSchema.SETTINGS_TABLE_NAME, null, cv);
		}
		Log.e("DatabaseModel", "Settings initiated");
	}

}
