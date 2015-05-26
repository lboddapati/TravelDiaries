package com.example.tonia.projecttraveldiaries;

/**
 * Created by Tonia on 5/14/15.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceJSONParser {

    /** Receives a JSONObject and returns a list */
    public List<HashMap<String,String>> parse(JSONObject jObject){

        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("results");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlaces(jPlaces);
    }
    public List<HashMap<String,Double>> parseLatLong(JSONObject jObject){

        List<HashMap<String, Double>> placesList = new ArrayList<HashMap<String,Double>>();
        HashMap<String, Double> place = new HashMap<String, Double>();
        JSONArray jPlaces = null;

        double lat;
        double longitude;
        try {
            /** Retrieves all the elements in the 'places' array */
            lat = (jObject.getJSONObject("result")).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            System.out.println(lat);
            place.put("latitude", lat);
            placesList.add(place);

            longitude = (jObject.getJSONObject("result")).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            place.put("longitude", longitude);
            placesList.add(place);



        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */

        return placesList;
    }


    /** Receives a JSONObject and returns a list */
    public List<HashMap<String,String>> parseAutoComplete(JSONObject jObject){

        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("predictions");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlacesAutoComplete(jPlaces);
    }

    private List<HashMap<String, String>> getPlacesAutoComplete(JSONArray jPlaces){
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> place = null;

        /** Taking each place, parses and adds to list object */
        for(int i=0; i<placesCount;i++){
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getPlaceAutocomplete((JSONObject)jPlaces.get(i));
                placesList.add(place);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    /** Parsing the Place JSON object */
    private HashMap<String, String> getPlaceAutocomplete(JSONObject jPlace){

        HashMap<String, String> place = new HashMap<String, String>();

        String description="";
        String PlaceID="";

        try {

            description = jPlace.getString("description");
            place.put("description", description);
            PlaceID = jPlace.getString("place_id");
            place.put("place_id", PlaceID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces){
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> place = null;

        /** Taking each place, parses and adds to list object */
        for(int i=0; i<placesCount;i++){
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getPlace((JSONObject)jPlaces.get(i));
                placesList.add(place);

            }

        catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    /** Parsing the Place JSON object */
    private HashMap<String, String> getPlace(JSONObject jPlace){

        HashMap<String, String> place = new HashMap<String, String>();
        String placeName = "-NA-";
        String vicinity="-NA-";
        String latitude="";
        String longitude="";

        try {
            // Extracting Place name, if available
            if(!jPlace.isNull("name")){
                placeName = jPlace.getString("name");
            }

            // Extracting Place Vicinity, if available
            if(!jPlace.isNull("vicinity")){
                vicinity = jPlace.getString("vicinity");
            }

            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");

            place.put("place_name", placeName);
            place.put("vicinity", vicinity);
            place.put("lat", latitude);
            place.put("lng", longitude);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }
}
