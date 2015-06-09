package com.example.traveldiaries;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/* class to show the detailed directions  */
public class DirectionsExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private JSONArray legs;
    private ArrayList<String> placesNames;

    //get the route once places rae selected
    public DirectionsExpandableListAdapter(Context context, JSONObject route, ArrayList<String> placesNames) {
        this.context = context;
        this.placesNames = placesNames;
        try {
            this.legs = route.getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getGroupCount() {
        return legs.length();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int count=0;
        try {
            count = legs.getJSONObject(groupPosition).getJSONArray("steps").length();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public Object getGroup(int groupPosition) {
        JSONObject group = null;
        try {
            group = legs.getJSONObject(groupPosition);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return group;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        JSONObject child = null;
        try {
            child = legs.getJSONObject(groupPosition).getJSONArray("steps").getJSONObject(childPosition);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return child;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    //adds the directions to a collapsible list
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        JSONObject header = (JSONObject) getGroup(groupPosition);

        if(convertView == null) {
            LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.header_item, null);
        }

        TextView legHeader = (TextView) convertView.findViewById(R.id.legHeader);
        TextView  legDetails = (TextView) convertView.findViewById(R.id.legDetails);
        try {

            String dist = header.getJSONObject("distance").getString("text");
            String duration = header.getJSONObject("duration").getString("text");
            legHeader.setText(placesNames.get(groupPosition)+" -> "+placesNames.get(groupPosition+1));
            legDetails.setText(dist+", "+duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        JSONObject step = (JSONObject) getChild(groupPosition, childPosition);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_item, null);
        }

        TextView legStep = (TextView) convertView.findViewById(R.id.legStep);
        try {
            String instructions = cleanInstructions(step.getString("html_instructions"));
            legStep.setText(instructions);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private String cleanInstructions(String htmlInstructions) {
        Spanned html = Html.fromHtml(htmlInstructions);
        return html.toString();
    }
}
