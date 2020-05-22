package com.hriportfolio.speaketh;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GroupAdapter extends ArrayAdapter<String> {

    private ArrayList<String> groupList;
    private Context context;

    public GroupAdapter(@NonNull Context context, int resource, ArrayList<String> groupList) {
        super(context, resource);

        this.context = context;
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_group_listview, null);

        TextView grpName = view.findViewById(R.id.grpName);
        grpName.setText(groupList.get(position));


        return view;
    }

    @Override
    public int getCount() {
        return groupList.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return groupList.get(position);
    }
}
