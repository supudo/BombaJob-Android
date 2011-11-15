package com.supudo.net.apps.aBombaJob.Database.Models;

public final class CategoryModel {
	public final Integer CategoryID;
	public final String Title;
	public final Integer OffersCount;
	
	public CategoryModel(Integer cid, String title, Integer offersCount) {
		this.CategoryID = cid;
		this.Title = title;
		this.OffersCount = offersCount;
	}
}
