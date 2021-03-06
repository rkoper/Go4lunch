package com.m.sofiane.go4lunch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.m.sofiane.go4lunch.R;
import com.m.sofiane.go4lunch.activity.SubActivity;
import com.m.sofiane.go4lunch.models.pojoAutoComplete.Prediction;

import java.util.ArrayList;


public class SearchMapAdapter extends RecyclerView.Adapter<SearchMapAdapter.ViewHolder> {
    final Context mContext;
    final FragmentManager mFragmentManager;
    private final ArrayList<Prediction> listdataForSearch;

    public SearchMapAdapter(ArrayList<Prediction> listdataForSearch, FragmentManager mFragmentManager, Context mContext) {
        this.listdataForSearch = listdataForSearch;
        this.mFragmentManager = mFragmentManager;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public SearchMapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_search_map, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        String ld = listdataForSearch.get(i).getStructuredFormatting().getMainText();
        h.txtname.setText(ld);

        clickAndSendData(h, i);
    }

    private void clickAndSendData(ViewHolder h, int i) {
        h.mButton.setOnClickListener(view -> {
            @SuppressLint("RestrictedApi") Intent intent = new Intent(mContext, SubActivity.class);
            intent.putExtra("I", listdataForSearch.get(i).getPlaceId());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listdataForSearch.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtname;
        final Button mButton;

        public ViewHolder(View itemView) {
            super(itemView);
            txtname = itemView.findViewById(R.id.place_name_for_maps);
            mButton = itemView.findViewById(R.id.button_click_map_search);
        }
    }
}


