package com.example.covid_tracker.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_tracker.Models.DistrictWiseModel;
import com.example.covid_tracker.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistrictWiseAdapter extends RecyclerView.Adapter<DistrictWiseAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<DistrictWiseModel> districtWiseModelArrayList;

    private String searchText="";
    private SpannableStringBuilder sb;

    public DistrictWiseAdapter(Context mContext, ArrayList<DistrictWiseModel> districtWiseModelArrayList) {
        this.mContext = mContext;
        this.districtWiseModelArrayList = districtWiseModelArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_state_wise, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        DistrictWiseModel currentItem = districtWiseModelArrayList.get(position);
        String districtName = currentItem.getDistrict();
        String districtTotal = currentItem.getConfirmed();
        holder.tv_districtTotalCases.setText(NumberFormat.getInstance().format(Integer.parseInt(districtTotal)));
        //holder.tv_districtName.setText(districtName);
        if(searchText.length()>0){
            //color your text here
            int index = districtName.indexOf(searchText);
            sb = new SpannableStringBuilder(districtName);
            Pattern word = Pattern.compile(searchText.toLowerCase());
            Matcher match = word.matcher(districtName.toLowerCase());
            while(match.find()){
                ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(52, 195, 235)); //specify color here
                sb.setSpan(fcs, match.start(), match.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                //index = stateName.indexOf(searchText,index+1);

            }
            holder.tv_districtName.setText(sb);

        }else{
            holder.tv_districtName.setText(districtName);
        }


    }

    @Override
    public int getItemCount() {
        return districtWiseModelArrayList==null ? 0 : districtWiseModelArrayList.size();
    }

    public void filterList(ArrayList<DistrictWiseModel> filteredList, String search) {
        districtWiseModelArrayList = filteredList;
        this.searchText = search;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_districtName, tv_districtTotalCases;
        LinearLayout lin_district_wise;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_districtName = itemView.findViewById(R.id.statewise_layout_name_textview);
            tv_districtTotalCases = itemView.findViewById(R.id.statewise_layout_confirmed_textview);
            lin_district_wise = itemView.findViewById(R.id.layout_state_wise_lin);
        }
    }

}
