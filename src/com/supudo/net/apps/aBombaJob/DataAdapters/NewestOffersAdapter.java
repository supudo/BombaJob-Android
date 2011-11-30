package com.supudo.net.apps.aBombaJob.DataAdapters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.supudo.net.apps.aBombaJob.R;
import com.supudo.net.apps.aBombaJob.Database.Models.JobOfferModel;

public class NewestOffersAdapter extends ArrayAdapter<JobOfferModel> {

	private LayoutInflater mInflater;
	private ArrayList<JobOfferModel> items;
	private Context context;

	public NewestOffersAdapter(Context context, int textViewResourceId, ArrayList<JobOfferModel> items) {
		super(context, textViewResourceId, items);
		mInflater = LayoutInflater.from(context);
		this.items = items;
		this.context = context;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_icon_subtitled, parent, false);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		JobOfferModel off = (JobOfferModel)items.get(position);
		
		holder.title.setTag(off.OfferID);

		if (off.Title.length() > 35)
			holder.title.setText(off.Title.substring(0, 35) + "...");
		else
			holder.title.setText(off.Title);
		
		if (off.ReadYn)
			holder.title.setTypeface(null, Typeface.NORMAL);
		else
			holder.title.setTypeface(null, Typeface.BOLD);

		String osubtitle = ((off.HumanYn) ? this.context.getString(R.string.human_short) : this.context.getString(R.string.company_short));
		try {
			DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			Date date = (Date)formatter.parse(off.PublishDate);
			int month = date.getMonth() + 1;

			String weekdayLabel = "weekday_" + date.getDay();
			osubtitle += " // " + (String) this.context.getResources().getText(this.context.getResources().getIdentifier(weekdayLabel, "string", "com.supudo.net.apps.aBombaJob"));

			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			osubtitle += ", " + cal.get(Calendar.DAY_OF_MONTH);

			String monthLabel = "monthsShort_" + month;
			osubtitle += " " + (String) this.context.getResources().getText(this.context.getResources().getIdentifier(monthLabel, "string", "com.supudo.net.apps.aBombaJob"));
		}
		catch (ParseException e) {
			Log.d("NewestOffers", "Date parser failed - " + off.PublishDate);
		}
		holder.subtitle.setText(osubtitle);

		if (off.HumanYn)
			holder.icon.setImageResource(R.drawable.iconperson);
		else
			holder.icon.setImageResource(R.drawable.iconcompany);

  		return convertView;
 	}

  	static class ViewHolder {
 		ImageView icon;
 		TextView title;
 		TextView subtitle;
 		Integer oid;
 	}
 }
