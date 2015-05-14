package com.example.lasyaboddapati.traveldiaries;

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

/**
 * Created by lasyaboddapati on 5/10/15.
 */
public class DirectionsExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    //private JSONObject route;
    private JSONArray legs;

    public DirectionsExpandableListAdapter(Context context, JSONObject route) {
        this.context = context;
        //this.route = route;
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
            String startAddr = header.getString("start_address");
            String endAddr = header.getString("end_address");
            String dist = header.getJSONObject("distance").getString("text");
            String duration = header.getJSONObject("duration").getString("text");
            //String arrivalTime = "ETA: "+header.getJSONObject("arrival_time").getString("text");
            Log.d("HEADER", "AAAAAAAAAAAAAAAAAAA"+ startAddr+" -> "+endAddr);
            legHeader.setText(startAddr+" -> "+endAddr);
            legDetails.setText(dist+", "+duration); //+", "+arrivalTime);
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
        //Log.d("CLEAN INSTRUCTIONS", html.toString());
        return html.toString();
    }
}
