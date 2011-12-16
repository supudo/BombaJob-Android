package net.supudo.apps.aBombaJob.Database;

import java.util.ArrayList;

import net.supudo.apps.aBombaJob.Database.Models.CategoryModel;
import net.supudo.apps.aBombaJob.Database.Models.JobOfferModel;
import net.supudo.apps.aBombaJob.Database.Models.SettingModel;
import net.supudo.apps.aBombaJob.Database.Models.TextContentModel;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataHelper {

	private Context mCtx;
	private DatabaseModel dbModel;

	public DataHelper(Context ctx) {
		mCtx = ctx;
		dbModel = new DatabaseModel(mCtx);
	}

	/* ------------------------------------------
	 * 
	 * Settings
	 * 
	 * ------------------------------------------
	 */
	public ArrayList<SettingModel> selectAllSettings() {
		return selectSettings(null, null);
	}

	public ArrayList<SettingModel> selectSettings(String selection, String[] args) {
		SQLiteDatabase db = dbModel.getReadableDatabase();
		Cursor c = db.query(DatabaseSchema.SETTINGS_TABLE_NAME, new String[] {
					DatabaseSchema.SettingsColumns._ID,
					DatabaseSchema.SettingsColumns.EDITABLE_YN,
					DatabaseSchema.SettingsColumns.SNAME,
					DatabaseSchema.SettingsColumns.SVALUE},
				selection, args, null, null, null);
		c.moveToFirst();
		ArrayList<SettingModel> resu = new ArrayList<SettingModel>();
		while (c.isAfterLast() == false)
		{
			boolean editableYn = (c.getInt(c.getColumnIndex(DatabaseSchema.SettingsColumns.EDITABLE_YN)) == 1);
			String sname = c.getString(c.getColumnIndex(DatabaseSchema.SettingsColumns.SNAME));
			String svalue = c.getString(c.getColumnIndex(DatabaseSchema.SettingsColumns.SVALUE));
			SettingModel model = new SettingModel(editableYn, sname, svalue);
			resu.add(model);
			c.moveToNext();
		}
		c.close();
		db.close();
		return resu;
	}

	public ArrayList<SettingModel> selectEditableSettings() {
		SQLiteDatabase db = dbModel.getReadableDatabase();
		Cursor c = db.query(DatabaseSchema.SETTINGS_TABLE_NAME, new String[] {
					DatabaseSchema.SettingsColumns._ID,
					DatabaseSchema.SettingsColumns.EDITABLE_YN,
					DatabaseSchema.SettingsColumns.SNAME,
					DatabaseSchema.SettingsColumns.SVALUE},
					DatabaseSchema.SettingsColumns.EDITABLE_YN + " = 1", null, null, null, null);
		c.moveToFirst();
		ArrayList<SettingModel> resu = new ArrayList<SettingModel>();
		while (c.isAfterLast() == false)
		{
			boolean editableYn = (c.getInt(c.getColumnIndex(DatabaseSchema.SettingsColumns.EDITABLE_YN)) == 1);
			String sname = c.getString(c.getColumnIndex(DatabaseSchema.SettingsColumns.SNAME));
			String svalue = c.getString(c.getColumnIndex(DatabaseSchema.SettingsColumns.SVALUE));
			SettingModel model = new SettingModel(editableYn, sname, svalue);
			resu.add(model);
			c.moveToNext();
		}
		c.close();
		db.close();
		return resu;
	}
	
	public SettingModel GetSetting(String sName) {
		ArrayList<SettingModel> ar = selectSettings(DatabaseSchema.SettingsColumns.SNAME + " = ?", new String[]{"" + sName});
		if (ar.size() > 0)
			return ar.get(0);
		return null;
	}
	
	public boolean SetSetting(String sName, String sValue) {
		try {
			SQLiteDatabase db = new DatabaseModel(mCtx).getReadableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(DatabaseSchema.SettingsColumns.SVALUE, sValue);
			db.update(DatabaseSchema.SETTINGS_TABLE_NAME, cv, DatabaseSchema.SettingsColumns.SNAME + " = ?", new String[]{"" + sName});
			db.close();
		}
		catch (Exception ex) {
			Log.d("DataHelper", "Setting save failed - " + sName + " : " + sValue);
			return false;
		}
		return true;
	}

	/* ------------------------------------------
	 * 
	 * Text Content
	 * 
	 * ------------------------------------------
	 */
	public ArrayList<TextContentModel> selectAllTextContent() {
		return selectTextContent(null, null);
	}

	public ArrayList<TextContentModel> selectTextContent(String selection, String[] args) {
		SQLiteDatabase db = dbModel.getReadableDatabase();
		Cursor c = db.query(DatabaseSchema.TEXTCONTENT_TABLE_NAME, new String[] {
					DatabaseSchema.TextContentColumns._ID,
					DatabaseSchema.TextContentColumns.TITLE,
					DatabaseSchema.TextContentColumns.CONTENT},
				selection, args, null, null, null);
		c.moveToFirst();
		ArrayList<TextContentModel> resu = new ArrayList<TextContentModel>();
		while (c.isAfterLast() == false) {
			Integer cid = c.getInt(c.getColumnIndex(DatabaseSchema.TextContentColumns._ID));
			String title = c.getString(c.getColumnIndex(DatabaseSchema.TextContentColumns.TITLE));
			String content = c.getString(c.getColumnIndex(DatabaseSchema.TextContentColumns.CONTENT));
			TextContentModel model = new TextContentModel(cid, title, content);
			resu.add(model);
			c.moveToNext();
		}
		c.close();
		db.close();
		return resu;
	}
	
	public TextContentModel GetTextContent(Integer cid) {
		ArrayList<TextContentModel> ar = selectTextContent(DatabaseSchema.TextContentColumns._ID + " = ?", new String[]{"" + cid});
		if (ar.size() > 0)
			return ar.get(0);
		return null;
	}
	
	public String GetTextContentForSetting(String sName) {
		if (sName.equalsIgnoreCase("StorePrivateData"))
			return GetTextContent(36).Content;
		else if (sName.equalsIgnoreCase("SendGeo"))
			return GetTextContent(37).Content;
		else if (sName.equalsIgnoreCase("InitSync"))
			return GetTextContent(38).Content;
		else if (sName.equalsIgnoreCase("OnlineSearch"))
			return GetTextContent(39).Content;
		else if (sName.equalsIgnoreCase("InAppEmail"))
			return GetTextContent(40).Content;
		else if (sName.equalsIgnoreCase("ShowCategories"))
			return GetTextContent(41).Content;
		else
			return "";
	}

	/* ------------------------------------------
	 * 
	 * Category
	 * 
	 * ------------------------------------------
	 */
	public ArrayList<CategoryModel> selectAllCategory() {
		return selectCategory(null, null);
	}

	public ArrayList<CategoryModel> selectCategories(boolean humanYn) {
		return selectCategory(DatabaseSchema.CategoriesColumns._ID + " IN (SELECT cid FROM joboffers WHERE humanyn = " + ((humanYn) ? "1" :"0" ) + ")", null);
	}

	public int getCategoryCount(boolean humanYn) {
		SQLiteDatabase db = dbModel.getReadableDatabase();
		Cursor c = db.query(DatabaseSchema.CATEGORY_TABLE_NAME, new String[] {
					DatabaseSchema.CategoriesColumns._ID,
					DatabaseSchema.CategoriesColumns.TITLE,
					DatabaseSchema.CategoriesColumns.OFFERS_COUNT},
					DatabaseSchema.CategoriesColumns._ID + " IN (SELECT cid FROM joboffers WHERE humanyn = " + ((humanYn) ? "1" :"0" ) + ")", null, null, null, null);
		int rowsCount = c.getCount();
		c.close();
		db.close(); 
		return rowsCount;
	}

	public ArrayList<CategoryModel> selectCategory(String selection, String[] args) {
		SQLiteDatabase db = dbModel.getReadableDatabase();
		Cursor c = db.query(DatabaseSchema.CATEGORY_TABLE_NAME, new String[] {
					DatabaseSchema.CategoriesColumns._ID,
					DatabaseSchema.CategoriesColumns.TITLE,
					DatabaseSchema.CategoriesColumns.OFFERS_COUNT},
				selection, args, null, null, null);
		c.moveToFirst();
		ArrayList<CategoryModel> resu = new ArrayList<CategoryModel>();
		while (c.isAfterLast() == false)
		{
			Integer cid = c.getInt(c.getColumnIndex(DatabaseSchema.CategoriesColumns._ID));
			String title = c.getString(c.getColumnIndex(DatabaseSchema.CategoriesColumns.TITLE));
			Integer offersCount = c.getInt(c.getColumnIndex(DatabaseSchema.CategoriesColumns.OFFERS_COUNT));
			CategoryModel model = new CategoryModel(cid, title, offersCount);
			resu.add(model);
			c.moveToNext();
		}
		c.close();
		db.close();
		return resu;
	}
	
	public CategoryModel GetCategory(Integer cid) {
		ArrayList<CategoryModel> ar = selectCategory(DatabaseSchema.CategoriesColumns._ID + " = ?", new String[]{"" + cid});
		if (ar.size() > 0)
			return ar.get(0);
		return null;
	}

	/* ------------------------------------------
	 * 
	 * Job Offers
	 * 
	 * ------------------------------------------
	 */
	public int getJobOffersCount(boolean humanYn) {
		SQLiteDatabase db = dbModel.getReadableDatabase();
		Cursor c = db.query(DatabaseSchema.JOBOFFER_TABLE_NAME, new String[] {
					DatabaseSchema.JobOffersColumns._ID, DatabaseSchema.JobOffersColumns.CATEGORY_ID},
				DatabaseSchema.JobOffersColumns.HUMAN_YN + " = " + ((humanYn) ? "1" :"0" ), null, null, null, null);
		int rowsCount = c.getCount();
		c.close();
		db.close(); 
		return rowsCount;
	}

	public ArrayList<JobOfferModel> selectAllJobOffers() {
		return selectJobOffers(null, null, null);
	}

	public ArrayList<JobOfferModel> selectNewestJobOffers() {
		return selectJobOffers(null, null, null);
	}

	public ArrayList<JobOfferModel> selectSearchJobs() {
		return selectJobOffers(DatabaseSchema.JobOffersColumns.HUMAN_YN + " = 0", null, null);
	}

	public ArrayList<JobOfferModel> selectSearchJobs(int categoryID) {
		String whereClause = DatabaseSchema.JobOffersColumns.HUMAN_YN + " = 0";
		if (categoryID > 0)
			whereClause += " AND " + DatabaseSchema.JobOffersColumns.CATEGORY_ID + " = " + categoryID;
		return selectJobOffers(whereClause, null, null);
	}

	public ArrayList<JobOfferModel> selectSearchPeople() {
		return selectJobOffers(DatabaseSchema.JobOffersColumns.HUMAN_YN + "  =  1", null, null);
	}

	public ArrayList<JobOfferModel> selectSearchPeople(int categoryID) {
		String whereClause = DatabaseSchema.JobOffersColumns.HUMAN_YN + " = 1";
		if (categoryID > 0)
			whereClause += " AND " + DatabaseSchema.JobOffersColumns.CATEGORY_ID + " = " + categoryID;
		return selectJobOffers(whereClause, null, null);
	}

	public ArrayList<JobOfferModel> selectJobOffers(String selection, String[] args, String queryLimit) {
		SQLiteDatabase db = dbModel.getReadableDatabase();
		Cursor c = db.query(DatabaseSchema.JOBOFFER_TABLE_NAME, new String[] {
					DatabaseSchema.JobOffersColumns._ID,
					DatabaseSchema.JobOffersColumns.CATEGORY_ID,
					DatabaseSchema.JobOffersColumns.TITLE,
					DatabaseSchema.JobOffersColumns.CATEGORY_TITLE,
					DatabaseSchema.JobOffersColumns.EMAIL,
					DatabaseSchema.JobOffersColumns.FREELANCE_YN,
					DatabaseSchema.JobOffersColumns.HUMAN_YN,
					DatabaseSchema.JobOffersColumns.READ_YN,
					DatabaseSchema.JobOffersColumns.SENTMESSAGE_YN,
					DatabaseSchema.JobOffersColumns.NEGATIVISM,
					DatabaseSchema.JobOffersColumns.POSITIVISM,
					DatabaseSchema.JobOffersColumns.PUBLISH_DATE,
					DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP,
					DatabaseSchema.JobOffersColumns.GEO_LATITUDE,
					DatabaseSchema.JobOffersColumns.GEO_LONGITUDE},
				selection, args, null, null, DatabaseSchema.JobOffersColumns.READ_YN + " ASC, " + DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP + " DESC", queryLimit);
		c.moveToFirst();
		ArrayList<JobOfferModel> resu = new ArrayList<JobOfferModel>();
		while (c.isAfterLast() == false) {
			Integer offerID = c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns._ID));
			Integer categoryID = c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.CATEGORY_ID));
			String title = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.TITLE));
			String categoryTitle = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.CATEGORY_TITLE));
			String email = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.EMAIL));
			String negativism = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.NEGATIVISM));
			String positivism = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.POSITIVISM));
			String publishDate = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.PUBLISH_DATE));
			Long publishDateStamp = c.getLong(c.getColumnIndex(DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP));
			boolean freelanceYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.FREELANCE_YN)) == 1);
			boolean humanYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.HUMAN_YN)) == 1);
			boolean readYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.READ_YN)) == 1);
			boolean sentMessageYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.SENTMESSAGE_YN)) == 1);
			String glat = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.GEO_LATITUDE));
			String glong = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.GEO_LONGITUDE));

			JobOfferModel model = new JobOfferModel(offerID, categoryID, title, categoryTitle, email, freelanceYn, humanYn, negativism, positivism, publishDate, publishDateStamp, readYn, sentMessageYn, glat, glong);
			resu.add(model);
			c.moveToNext();
		}
		c.close();
		db.close();
		return resu;
	}

	public ArrayList<JobOfferModel> selectSearchedJobOffers(String sKeyword, int sFreelance) {
		sKeyword = DatabaseUtils.sqlEscapeString(sKeyword);

		SQLiteDatabase db = dbModel.getReadableDatabase();
		String q = "";
		q += "SELECT " + DatabaseSchema.JobOffersColumns._ID + ", " +
				DatabaseSchema.JobOffersColumns.CATEGORY_ID + ", " + DatabaseSchema.JobOffersColumns.TITLE + ", " +
				DatabaseSchema.JobOffersColumns.CATEGORY_TITLE + ", " + DatabaseSchema.JobOffersColumns.EMAIL + ", " +
				DatabaseSchema.JobOffersColumns.FREELANCE_YN + ", " + DatabaseSchema.JobOffersColumns.HUMAN_YN + ", " +
				DatabaseSchema.JobOffersColumns.READ_YN + ", " + DatabaseSchema.JobOffersColumns.SENTMESSAGE_YN + ", " +
				DatabaseSchema.JobOffersColumns.NEGATIVISM + ", " + DatabaseSchema.JobOffersColumns.POSITIVISM + ", " +
				DatabaseSchema.JobOffersColumns.PUBLISH_DATE + ", " + DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP + ", " +
				DatabaseSchema.JobOffersColumns.GEO_LATITUDE + ", " + DatabaseSchema.JobOffersColumns.GEO_LONGITUDE + " ";
		q += "FROM " + DatabaseSchema.JOBOFFER_TABLE_NAME + " ";
		q += "WHERE " + DatabaseSchema.JOBOFFER_TABLE_NAME + " MATCH ? ";
		if (sFreelance > 0)
			q += " AND " + DatabaseSchema.JobOffersColumns.FREELANCE_YN + " = " + sFreelance + " ";
		q += "ORDER BY " + DatabaseSchema.JobOffersColumns.READ_YN + " ASC, " + DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP + " DESC ";

		Cursor c = db.rawQuery(q, new String[]{" *" + sKeyword + "* "});

		c.moveToFirst();
		ArrayList<JobOfferModel> resu = new ArrayList<JobOfferModel>();
		while (c.isAfterLast() == false) {
			Integer offerID = c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns._ID));
			Integer categoryID = c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.CATEGORY_ID));
			String title = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.TITLE));
			String categoryTitle = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.CATEGORY_TITLE));
			String email = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.EMAIL));
			String negativism = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.NEGATIVISM));
			String positivism = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.POSITIVISM));
			String publishDate = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.PUBLISH_DATE));
			Long publishDateStamp = c.getLong(c.getColumnIndex(DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP));
			boolean freelanceYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.FREELANCE_YN)) == 1);
			boolean humanYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.HUMAN_YN)) == 1);
			boolean readYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.READ_YN)) == 1);
			boolean sentMessageYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.SENTMESSAGE_YN)) == 1);
			String glat = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.GEO_LATITUDE));
			String glong = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.GEO_LONGITUDE));

			JobOfferModel model = new JobOfferModel(offerID, categoryID, title, categoryTitle, email, freelanceYn, humanYn, negativism, positivism, publishDate, publishDateStamp, readYn, sentMessageYn, glat, glong);
			resu.add(model);
			c.moveToNext();
		}
		c.close();
		db.close();
		return resu;
	}
	
	public JobOfferModel GetJobOffer(Integer id) {
		SQLiteDatabase db = dbModel.getReadableDatabase();
		String q = "SELECT " + DatabaseSchema.JobOffersColumns._ID + ", " +
			DatabaseSchema.JobOffersColumns.CATEGORY_ID + ", " + DatabaseSchema.JobOffersColumns.TITLE + ", " +
			DatabaseSchema.JobOffersColumns.CATEGORY_TITLE + ", " + DatabaseSchema.JobOffersColumns.EMAIL + ", " +
			DatabaseSchema.JobOffersColumns.FREELANCE_YN + ", " + DatabaseSchema.JobOffersColumns.HUMAN_YN + ", " +
			DatabaseSchema.JobOffersColumns.READ_YN + ", " + DatabaseSchema.JobOffersColumns.SENTMESSAGE_YN + ", " +
			DatabaseSchema.JobOffersColumns.NEGATIVISM + ", " + DatabaseSchema.JobOffersColumns.POSITIVISM + ", " +
			DatabaseSchema.JobOffersColumns.PUBLISH_DATE + ", " + DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP + ", " +
			DatabaseSchema.JobOffersColumns.GEO_LATITUDE + ", " + DatabaseSchema.JobOffersColumns.GEO_LONGITUDE + " ";
		q += "FROM " + DatabaseSchema.JOBOFFER_TABLE_NAME + " ";
		q += "WHERE _id = " + id;
		Cursor c = db.rawQuery(q, null);
		c.moveToFirst();

		JobOfferModel model = null;
		if (!c.isNull(c.getColumnIndex(DatabaseSchema.JobOffersColumns._ID))) {
			Integer offerID = c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns._ID));
			Integer categoryID = c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.CATEGORY_ID));
			String title = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.TITLE));
			String categoryTitle = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.CATEGORY_TITLE));
			String email = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.EMAIL));
			String negativism = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.NEGATIVISM));
			String positivism = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.POSITIVISM));
			String publishDate = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.PUBLISH_DATE));
			Long publishDateStamp = c.getLong(c.getColumnIndex(DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP));
			boolean freelanceYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.FREELANCE_YN)) == 1);
			boolean humanYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.HUMAN_YN)) == 1);
			boolean readYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.READ_YN)) == 1);
			boolean sentMessageYn = (c.getInt(c.getColumnIndex(DatabaseSchema.JobOffersColumns.SENTMESSAGE_YN)) == 1);
			String glat = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.GEO_LATITUDE));
			String glong = c.getString(c.getColumnIndex(DatabaseSchema.JobOffersColumns.GEO_LONGITUDE));
	
			model = new JobOfferModel(offerID, categoryID, title, categoryTitle, email, freelanceYn, humanYn, negativism, positivism, publishDate, publishDateStamp, readYn, sentMessageYn, glat, glong);
		}

		c.close();
		db.close();
		return model;
	}
	
	public boolean AddJobOffer(Integer offerID, Integer categoryID, String title, String categoryTitle, String email,
						boolean freelanceYn, boolean humanYn, String negativism, String positivism,
						String publishDate, Integer publishDateStamp, boolean readYn, boolean sentMessageYn) {
		try {
			SQLiteDatabase db = new DatabaseModel(mCtx).getReadableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(DatabaseSchema.JobOffersColumns._ID, offerID);
			cv.put(DatabaseSchema.JobOffersColumns.CATEGORY_ID, categoryID);
			cv.put(DatabaseSchema.JobOffersColumns.TITLE, title);
			cv.put(DatabaseSchema.JobOffersColumns.CATEGORY_TITLE, categoryTitle);
			cv.put(DatabaseSchema.JobOffersColumns.EMAIL, email);
			cv.put(DatabaseSchema.JobOffersColumns.FREELANCE_YN, ((freelanceYn) ? 1 : 0));
			cv.put(DatabaseSchema.JobOffersColumns.HUMAN_YN, ((humanYn) ? 1 : 0));
			cv.put(DatabaseSchema.JobOffersColumns.NEGATIVISM, negativism);
			cv.put(DatabaseSchema.JobOffersColumns.POSITIVISM, positivism);
			cv.put(DatabaseSchema.JobOffersColumns.PUBLISH_DATE, publishDate);
			cv.put(DatabaseSchema.JobOffersColumns.PUBLISH_DATE_STAMP, publishDateStamp);
			cv.put(DatabaseSchema.JobOffersColumns.READ_YN, 0);
			cv.put(DatabaseSchema.JobOffersColumns.SENTMESSAGE_YN, 0);
			db.insert(DatabaseSchema.JOBOFFER_TABLE_NAME, null, cv);
			db.close();
		}
		catch (Exception ex) {
			return false;
		}
		return true;
	}
	
	public void setOfferReadYn(Integer id) {
		SQLiteDatabase db = new DatabaseModel(mCtx).getReadableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DatabaseSchema.JobOffersColumns.READ_YN, 1);
		db.update(DatabaseSchema.JOBOFFER_TABLE_NAME, cv, DatabaseSchema.JobOffersColumns._ID + " = " + id, null);
		db.close();
	}
	
	public void setOfferSentMessageYn(Integer id) {
		SQLiteDatabase db = new DatabaseModel(mCtx).getReadableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DatabaseSchema.JobOffersColumns.SENTMESSAGE_YN, 1);
		db.update(DatabaseSchema.JOBOFFER_TABLE_NAME, cv, DatabaseSchema.JobOffersColumns._ID + " = " + id, null);
		db.close();
	}
}
