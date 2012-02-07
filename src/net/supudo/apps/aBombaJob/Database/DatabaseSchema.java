package net.supudo.apps.aBombaJob.Database;

import android.provider.BaseColumns;

public class DatabaseSchema {

	public static final String TEXTCONTENT_TABLE_NAME = "textcontent";
	public static final String CATEGORY_TABLE_NAME = "categories";
	public static final String JOBOFFER_TABLE_NAME = "joboffers";

	public static final class TextContentColumns implements BaseColumns {
		private TextContentColumns(){};
		public static final String TITLE = "content";
		public static final String CONTENT = "title";
	}

	public static final class CategoriesColumns implements BaseColumns {
		private CategoriesColumns(){};
		public static final String TITLE = "title";
		public static final String OFFERS_COUNT = "offerscount";
	}

	public static final class JobOffersColumns implements BaseColumns {
		private JobOffersColumns(){};
		public static final String CATEGORY_ID = "cid";
		public static final String TITLE = "title";
		public static final String CATEGORY_TITLE = "categorytitle";
		public static final String EMAIL = "email";
		public static final String FREELANCE_YN = "freelanceyn";
		public static final String HUMAN_YN = "humanyn";
		public static final String NEGATIVISM = "negativism";
		public static final String POSITIVISM = "positivism";
		public static final String PUBLISH_DATE = "publishdate";
		public static final String PUBLISH_DATE_STAMP = "publishdatestamp";
		public static final String READ_YN = "readyn";
		public static final String SENTMESSAGE_YN = "sentmessageyn";
		public static final String GEO_LATITUDE = "glatitude";
		public static final String GEO_LONGITUDE = "glongitude";
	}
	
}
