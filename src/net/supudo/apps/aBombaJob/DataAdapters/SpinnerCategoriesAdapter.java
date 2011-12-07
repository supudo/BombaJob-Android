package net.supudo.apps.aBombaJob.DataAdapters;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import net.supudo.apps.aBombaJob.R;
import net.supudo.apps.aBombaJob.Database.Models.CategoryModel;

public class SpinnerCategoriesAdapter implements SpinnerAdapter {

    private ArrayList<CategoryModel> data;
    private Context context;

    public SpinnerCategoriesAdapter(Context context, ArrayList<CategoryModel> data){
    	this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return android.R.layout.simple_spinner_dropdown_item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView v = new TextView(context);
        v.setTextColor(Color.BLACK);
        v.setText(data.get(position).Title);
        v.setTextColor(context.getResources().getColor(R.color.bjOrange));
        return v;
    }

    @Override
    public int getViewTypeCount() {
        return android.R.layout.simple_spinner_dropdown_item;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return this.getView(position, convertView, parent);
    }

}
