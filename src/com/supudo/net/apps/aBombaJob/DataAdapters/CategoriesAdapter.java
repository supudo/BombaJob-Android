package com.supudo.net.apps.aBombaJob.DataAdapters;

import java.util.ArrayList;

import com.supudo.net.apps.aBombaJob.R;
import com.supudo.net.apps.aBombaJob.Database.Models.CategoryModel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CategoriesAdapter  extends ArrayAdapter<CategoryModel> {

	private LayoutInflater mInflater;
	private ArrayList<CategoryModel> items;

	public CategoriesAdapter(Context context, int textViewResourceId, ArrayList<CategoryModel> items) {
		super(context, textViewResourceId, items);
		mInflater = LayoutInflater.from(context);
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_simple, parent, false);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		CategoryModel cat = (CategoryModel)items.get(position);
		
		holder.title.setTag(cat.CategoryID);
		holder.title.setText(cat.Title);
		holder.cid = cat.CategoryID;

  		return convertView;
 	}

  	static class ViewHolder {
 		TextView title;
 		Integer cid;
 	}

}
