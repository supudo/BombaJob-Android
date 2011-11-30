package net.supudo.apps.aBombaJob.Database.Models;

public final class JobOfferModel {
	public final Integer OfferID;
	public final Integer CategoryID;
	public final String Title;
	public final String CategoryTitle;
	public final String Email;
	public final boolean FreelanceYn;
	public final boolean HumanYn;
	public final String Negativism;
	public final String Positivism;
	public final String PublishDate;
	public final Long PublishDateStamp;
	public final boolean ReadYn;
	public final boolean SentMessageYn;
	public final String GeoLatitude;
	public final String GeoLongitude;
	
	public JobOfferModel(Integer offerID, Integer categoryID, String title, String categoryTitle, String email,
						boolean freelanceYn, boolean humanYn, String negativism, String positivism,
						String publishDate, Long publishDateStamp,
						boolean readYn, boolean sentMessageYn, String geoLatitiude, String getLongitude) {
		this.OfferID = offerID;
		this.CategoryID = categoryID;
		this.Title = title;
		this.CategoryTitle = categoryTitle;
		this.Email = email;
		this.FreelanceYn = freelanceYn;
		this.HumanYn = humanYn;
		this.Negativism = negativism;
		this.Positivism = positivism;
		this.PublishDate = publishDate;
		this.PublishDateStamp = publishDateStamp;
		this.ReadYn = readYn;
		this.SentMessageYn = sentMessageYn;
		this.GeoLatitude = geoLatitiude;
		this.GeoLongitude = getLongitude;
	}
}
