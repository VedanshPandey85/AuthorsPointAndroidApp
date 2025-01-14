package com.example.bookapp;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterCategory extends Filter {

    //arraylist in which we want to search
    ArrayList<ModelCategory> filterList;
    //adapter in which filter is to be implemented

    AdapterCategory adapterCategory;

    //constructor


    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if(constraint!=null && constraint.length()>0){

        //change to upper case or lowercase
        constraint = constraint.toString().toUpperCase();
        ArrayList<ModelCategory> filteredModels = new ArrayList<>();
        for(int i=0;i<filterList.size();i++){

                    //validate
                    if(filterList.get(i).getCategory().toUpperCase().contains(constraint)) {

                        //add to filtered list
                        filteredModels.add(filterList.get(i));

                    }
        }
        results.count = filteredModels.size();
        results.values = filteredModels;

        }
        else {

                results.count = filterList.size();
                results.values = filterList;

        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults results) {

        //apply filter changes

        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>)results.values;

        //notify changes
        adapterCategory.notifyDataSetChanged();



    }
}
