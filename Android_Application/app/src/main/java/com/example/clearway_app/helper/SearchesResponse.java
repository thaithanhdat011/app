package com.example.clearway_app.helper;


import com.example.clearway_app.model.Address;
import com.example.clearway_app.model.FavouriteLocation;

import java.util.ArrayList;

public class SearchesResponse {
    ArrayList<String> html_attributions;
    ArrayList<Address> results;

    public SearchesResponse(ArrayList<String> html_attributions, ArrayList<Address> results) {
        this.html_attributions = html_attributions;
        this.results = results;
    }

    public FavouriteLocation getFirstFavouriteLocation(){
        Address address = results.get(0);
        return new FavouriteLocation(address.getName(),
                address.getLongitudes(),
                address.getLatitudes(),
                address.getFormatted_address(),
                address.getFirstImage(),
                address.getPlace_id());
    }

    public ArrayList<Address> getResults() {
        return results;
    }
}
